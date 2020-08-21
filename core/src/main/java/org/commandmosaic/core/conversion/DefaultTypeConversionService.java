/*
 * Copyright (c) 2020 Peter G. Horvath, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 
package org.commandmosaic.core.conversion;

import org.commandmosaic.api.configuration.conversion.TypeConversion;
import org.commandmosaic.api.conversion.TypeConversionException;
import org.commandmosaic.api.conversion.TypeConversionService;
import org.commandmosaic.api.conversion.TypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

public class DefaultTypeConversionService implements TypeConversionService {

    private static final Logger log = LoggerFactory.getLogger(DefaultTypeConversionService.class);

    private static final List<TypeConversion<?, ?>> standardTypeTypeConversions = Arrays.asList(
            // -- to Short conversions
            new TypeConversion<>(String.class, Short.class, Short::valueOf),
            new TypeConversion<>(Integer.class, Short.class, Integer::shortValue),
            new TypeConversion<>(Long.class, Short.class, Long::shortValue),
            new TypeConversion<>(Double.class, Short.class, Double::shortValue),

            // -- to Integer conversions
            new TypeConversion<>(String.class, Integer.class, Integer::valueOf),
            new TypeConversion<>(Short.class, Integer.class, Short::intValue),
            new TypeConversion<>(Long.class, Integer.class, Long::intValue),
            new TypeConversion<>(Double.class, Integer.class, Double::intValue),

            // -- to Long conversions
            new TypeConversion<>(String.class, Long.class, Long::valueOf),
            new TypeConversion<>(Short.class, Long.class, Short::longValue),
            new TypeConversion<>(Integer.class, Long.class, Integer::longValue),
            new TypeConversion<>(Double.class, Long.class, Double::longValue),
            new TypeConversion<>(Date.class, Long.class, Date::getTime),

            // -- to Double conversions
            new TypeConversion<>(String.class, Double.class, Double::valueOf),
            new TypeConversion<>(Short.class, Double.class, Short::doubleValue),
            new TypeConversion<>(Integer.class, Double.class, Integer::doubleValue),
            new TypeConversion<>(Long.class, Double.class, Long::doubleValue),

            // -- to BigDecimal conversions
            new TypeConversion<>(String.class, BigDecimal.class, BigDecimal::new),
            new TypeConversion<>(Short.class, BigDecimal.class, num -> BigDecimal.valueOf((long)num)),
            new TypeConversion<>(Double.class, BigDecimal.class, BigDecimal::valueOf),
            new TypeConversion<>(Integer.class, BigDecimal.class, BigDecimal::valueOf),
            new TypeConversion<>(Long.class, BigDecimal.class, BigDecimal::valueOf),

            // -- to Date conversions
            new TypeConversion<>(Long.class, Date.class, Date::new)
            // NOTE: we intentionally DO NOT offer conversion between int and Date,
            // so as to discourage writing code that is prone to the Year 2038 problem
            // see https://en.wikipedia.org/wiki/Year_2038_problem
    );


    /**
     * Map keyed by <b>Source Type</b>, which contains Maps
     * keyed by <b>Target Type</b>, with values being the TypeConverter
     */
    private final Map<Class<?>, Map<Class<?>, TypeConverter<?, ?>>> typeConverters;

    public DefaultTypeConversionService() {
        this(Collections.emptyList());
    }

    public DefaultTypeConversionService(Collection<TypeConversion<?, ?>> userDefinedTypeConversions) {

        Map<Class<?>, Map<Class<?>, TypeConverter<?, ?>>> sourceToTargetAndConverterMap = new HashMap<>();

        log.debug("Configuring default TypeConversions");
        mapTypeConversions(sourceToTargetAndConverterMap, standardTypeTypeConversions);

        if (userDefinedTypeConversions != null) {
            log.debug("Configuring user-defined TypeConversions");
            // user defined conversions OVERRIDE the built-in conversions
            mapTypeConversions(sourceToTargetAndConverterMap, userDefinedTypeConversions);
        }

        this.typeConverters = Collections.unmodifiableMap(sourceToTargetAndConverterMap);
    }

    private static void mapTypeConversions(
            Map<Class<?>, Map<Class<?>, TypeConverter<?, ?>>> sourceToTargetAndConverterMap,
            Collection<TypeConversion<?, ?>> typeConversions) {

        for (TypeConversion<?, ?> tc : typeConversions) {
            Class<?> sourceType = tc.getSourceType();
            Class<?> targetType = tc.getTargetType();

            Map<Class<?>, TypeConverter<?, ?>> targetToTypeConverterMap =
                    sourceToTargetAndConverterMap.computeIfAbsent(sourceType, (t) -> new HashMap<>());

            targetToTypeConverterMap.put(targetType, tc.getConverter());
            log.trace("Added TypeConversion: {}", tc);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S, T> T convert(S value, Class<T> targetType) {

        if (value == null) {
            log.trace("Input value is null, returning null as conversion");
            return null;
        }

        Class<?> sourceType = value.getClass();
        if (targetType == sourceType
                || targetType.isAssignableFrom(sourceType)) {
            log.trace("No conversion is required, returning the input value");
            return (T) value;
        }

        Map<Class<?>, TypeConverter<?, ?>> targetTypeToConverterMap = typeConverters.get(sourceType);
        if (targetTypeToConverterMap == null) {
            log.debug("No conversion is found from source type {}", sourceType);
            throw new TypeConversionException(
                    "Cannot convert from [" + sourceType + "] to [" + targetType + "]");
        }

        TypeConverter<S, T> typeConverter = (TypeConverter<S, T>) targetTypeToConverterMap.get(targetType);
        if (typeConverter == null) {
            log.debug("No conversion is found to target type {}", targetType);
            throw new TypeConversionException(
                    "Cannot convert from [" + sourceType + "] to [" + targetType + "]");
        }

        try {
            log.debug("Converting value '{}' from {} to {}", value, sourceType, targetType);
            return typeConverter.convert(value);
        } catch (TypeConversionException e) {
            log.warn("Type conversion failed", e);
            throw e;
        } catch (RuntimeException e) {
            log.warn("Type conversion failed", e);
            throw new TypeConversionException("Type conversion failed", e);
        }


    }
}
