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

 
package org.commandmosaic.api.configuration.conversion;

import org.commandmosaic.api.conversion.TypeConverter;

import java.util.Objects;

/**
 * <p>
 * A {@code TypeConversion} encapsulates configuration describing
 * the source and target type of a {@link TypeConverter} and
 * {@code TypeConverter} itself.
 * </p>
 *
 * @param <S> source type
 * @param <T> target type
 */
public final class TypeConversion<S, T> {

    private final Class<S> sourceType;
    private final Class<T> targetType;
    private final TypeConverter<S, T> converter;

    public TypeConversion(Class<S> sourceType, Class<T> targetType, TypeConverter<S, T> converter) {
        Objects.requireNonNull(sourceType, "sourceType cannot be null");
        Objects.requireNonNull(targetType, "targetType cannot be null");
        Objects.requireNonNull(converter, "converter cannot be null");

        this.sourceType = sourceType;
        this.targetType = targetType;
        this.converter = converter;
    }

    public Class<?> getSourceType() {
        return sourceType;
    }

    public Class<?> getTargetType() {
        return targetType;
    }

    public TypeConverter<S, T> getConverter() {
        return converter;
    }

    @Override
    public String toString() {
        return new StringBuilder("TypeConversion{")
                .append(sourceType).append("-->").append(targetType)
                .append('}')
                .toString();
    }
}
