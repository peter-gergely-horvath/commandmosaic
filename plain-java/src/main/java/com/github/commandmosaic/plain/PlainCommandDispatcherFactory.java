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

 
package com.github.commandmosaic.plain;

import com.github.commandmosaic.api.conversion.TypeConversionService;
import com.github.commandmosaic.api.executor.CommandExecutor;
import com.github.commandmosaic.core.factory.AbstractCommandDispatcherFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlainCommandDispatcherFactory extends AbstractCommandDispatcherFactory {

    private static final Logger log = LoggerFactory.getLogger(PlainCommandDispatcherFactory.class);

    private static final PlainCommandDispatcherFactory INSTANCE = new PlainCommandDispatcherFactory();

    public static PlainCommandDispatcherFactory getInstance() {
        return INSTANCE;
    }

    @Override
    protected CommandExecutor getCommandExecutor(TypeConversionService typeConversionService) {
        log.trace("Creating ReflectiveCommandExecutor");
        return new ReflectiveCommandExecutor(typeConversionService);
    }

}
