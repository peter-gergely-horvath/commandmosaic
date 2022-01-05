/*
 * Copyright (c) 2020-2022 Peter G. Horvath, All Rights Reserved.
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

 
package org.commandmosaic.api.conversion;

/**
 * <p>
 * A {@code TypeConversionService} contains one method,
 * {@link #convert(Object, Class)} that either converts
 * the specified object to the desired target type
 * or throws an Exception.
 * </p>
 */
public interface TypeConversionService {

    /**
     * Converts the specified object to the desired target type or throws an Exception.
     *
     * @param value the input value to convert
     * @param targetType the target type to convert to
     *
     * @param <S> type of the source
     * @param <T> type of the target
     *
     * @return the value converted to the target type
     *
     * @throws TypeConversionException if conversion fails; e.g. the conversion is not feasible
     */
    <S, T> T convert(S value, Class<T> targetType) throws TypeConversionException;
}
