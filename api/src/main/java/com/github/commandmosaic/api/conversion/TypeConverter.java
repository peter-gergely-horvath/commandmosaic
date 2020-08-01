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

 
package com.github.commandmosaic.api.conversion;

/**
 * <p>
 * A {@code TypeConverter} converts a source object of {@code S} to a target of type {@code T}.
 * </p>
 *
 * <p>
 * Implementations of this interface must be Thread-safe.
 * </p>
 *
 * @param <S> source type
 * @param <T> target type
 */
@FunctionalInterface
public interface TypeConverter<S, T> {
    /**
     * Converts the source object of type {@code S} to target type {@code T}.
     * @param source the source object to convert, which must be an instance of {@code S} (never {@code null})
     * @return the converted object, which must be an instance of {@code T} (might be {@code null})
     */
    T convert(S source) throws TypeConversionException;
}
