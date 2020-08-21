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

 
package org.commandmosaic.core.parameter;

import org.commandmosaic.api.MissingParameterException;
import org.commandmosaic.api.Parameter;
import org.commandmosaic.api.ParameterInjectionException;
import org.commandmosaic.api.conversion.TypeConversionService;
import org.commandmosaic.api.executor.ParameterSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ParameterInjector {

    private static final Logger log = LoggerFactory.getLogger(ParameterInjector.class);

    private final Map<Class<?>, ParameterInjectionMetadata> injectionMetadataCache =
            new ConcurrentHashMap<>(256);

    private final ReflectionOperations reflectionOperations = new ReflectionOperations();

    private final TypeConversionService typeConversionService;

    public ParameterInjector(TypeConversionService typeConversionService) {
        Objects.requireNonNull(typeConversionService, "typeConversionService cannot be null");
        this.typeConversionService = typeConversionService;
    }

    private ParameterInjectionMetadata buildParameterMetadata(final Class<?> clazz) {
        List<ParameterInjectionMetadata.ParameterInjectedElement> elements = new ArrayList<>();
        Class<?> targetClass = clazz;

        do {
            final List<ParameterInjectionMetadata.ParameterInjectedElement> currElements = new ArrayList<>();

            reflectionOperations.doWithLocalFields(targetClass, field -> {
                Parameter parameterAnnotation = field.getAnnotation(Parameter.class);
                if (parameterAnnotation != null) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        throw new ParameterInjectionException(
                                "Parameter annotation is not supported on static fields: " + field);
                    }

                    boolean required = parameterAnnotation.required();
                    currElements.add(new ParameterFieldElement(field, required));

                    log.debug("Discovered parameter field (required={}): {}", required, field);
                }
            });

            elements.addAll(0, currElements);
            targetClass = targetClass.getSuperclass();
        }
        while (targetClass != null && targetClass != Object.class);

        return ParameterInjectionMetadata.forParameterInjectedElement(elements);
    }


    public void processInjection(Object commandObject, ParameterSource parameterSource)
            throws ParameterInjectionException {

        Class<?> clazz = commandObject.getClass();
        ParameterInjectionMetadata metadata = findParameterMetadata(clazz);

        try {
            metadata.inject(commandObject, parameterSource, typeConversionService);
        }
        catch (MissingParameterException ex) {
            throw new MissingParameterException(
                    "Required parameter(s) missing for [" + clazz + "]", ex);
        }
        catch (ParameterInjectionException ex) {
            throw ex;
        }
        catch (Throwable ex) {
            throw new ParameterInjectionException(
                    "Injection of parameters failed for [" + clazz + "]", ex);
        }
    }

    private ParameterInjectionMetadata findParameterMetadata(Class<?> clazz) {
        log.debug("Loading parameter injection metadata for {}", clazz);
        return this.injectionMetadataCache.computeIfAbsent(clazz, this::buildParameterMetadata);
    }

    private static class ParameterInjectionMetadata {

        private final Collection<ParameterInjectedElement> injectedElements;

        private ParameterInjectionMetadata(Collection<ParameterInjectedElement> elements) {
            this.injectedElements = elements;
        }

        public void inject(Object target,
                           ParameterSource parameterSource,
                           TypeConversionService typeConversionService) throws Throwable {

            log.debug("Injecting parameters to {}", target);

            for (ParameterInjectedElement element : injectedElements) {
                try {
                    element.inject(target, parameterSource, typeConversionService);
                }
                catch (ParameterInjectionException ex) {
                    throw ex;
                }
                catch (Exception ex) {
                    throw new ParameterInjectionException(
                            "Failed to inject parameter to: " + element.getMember(), ex);
                }

            }
        }

        public static final ParameterInjectionMetadata EMPTY = new ParameterInjectionMetadata(Collections.emptyList());

        public static ParameterInjectionMetadata forParameterInjectedElement(
                Collection<ParameterInjectedElement> elements) {

            return elements.isEmpty() ? ParameterInjectionMetadata.EMPTY : new ParameterInjectionMetadata(elements);
        }

        public abstract static class ParameterInjectedElement {

            protected final Member member;
            protected final boolean required;

            protected ParameterInjectedElement(Member member, boolean required) {
                this.member = member;
                this.required = required;
            }

            protected abstract void inject(Object target,
                                           ParameterSource source,
                                           TypeConversionService typeConversionService) throws Throwable;

            public Member getMember() {
                return member;
            }

        }
    }

    private static class ParameterFieldElement extends ParameterInjectionMetadata.ParameterInjectedElement {

        private final Class<?> fieldType;

        public ParameterFieldElement(Field field, boolean required) {
            super(field, required);
            fieldType = field.getType();
        }

        @Override
        protected void inject(Object target,
                              ParameterSource parameterSource,
                              TypeConversionService typeConversionService) throws Throwable {

            Field field = (Field) this.member;

            ReflectionOperations.makeAccessible(field);
            Object value = parameterSource.get(field);
            if (value != null) {
                Object convertedValue = typeConversionService.convert(value, fieldType);
                log.trace("Injecting value {} to {}", convertedValue, field);

                ReflectionOperations.makeAccessible(field);
                field.set(target, convertedValue);
            } else {
                if (required) {
                    throw new MissingParameterException("Required parameter is missing: " + field.getName());
                } else {
                    log.trace("Null value is not injected to {}", field);
                }
            }
        }
    }

}
