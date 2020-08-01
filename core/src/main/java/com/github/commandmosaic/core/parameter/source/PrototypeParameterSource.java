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

 
package com.github.commandmosaic.core.parameter.source;

import com.github.commandmosaic.api.executor.ParameterSource;

import java.lang.reflect.Field;

class PrototypeParameterSource implements ParameterSource {

    private final Object prototype;

    PrototypeParameterSource(Object prototype) {
        this.prototype = prototype;
    }

    @Override
    public Object get(Field field) {
        try {
            return field.get(prototype);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not extract field " + field + " from " + prototype, e);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PrototypeParameterSource{");
        sb.append("prototype=").append(prototype);
        sb.append('}');
        return sb.toString();
    }
}
