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

 
package org.commandmosaic.core.parameter.source;

import org.commandmosaic.api.executor.ParameterSource;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;

class MapParameterSource implements ParameterSource {

    private final Map<String, Object> map;

    MapParameterSource(Map<String, Object> map) {
        this.map = map;
    }

    @Override
    public Object get(Field field) {
        Objects.requireNonNull(field, "Cannot read property value from a null Field");

        String fieldName = field.getName();

        return map.get(fieldName);
    }
}
