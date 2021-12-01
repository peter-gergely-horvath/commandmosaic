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

 
package org.commandmosaic.plain;

import org.commandmosaic.api.Command;
import org.commandmosaic.api.conversion.TypeConversionService;
import org.commandmosaic.api.CommandContext;
import org.commandmosaic.api.executor.CommandExecutor;
import org.commandmosaic.api.executor.ParameterSource;
import org.commandmosaic.core.parameter.ParameterInjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

final class ReflectiveCommandExecutor implements CommandExecutor {

    private static final Logger log = LoggerFactory.getLogger(ReflectiveCommandExecutor.class);

    private final ParameterInjector parameterInjector;

    ReflectiveCommandExecutor(TypeConversionService typeConversionService) {
        parameterInjector = new ParameterInjector(typeConversionService);
    }

    @Override
    public <R, C extends Command<R>> R execute(
            Class<C> commandClass, ParameterSource parameters, CommandContext context) {

        log.debug("Executing command {} with parameters: {} and context: {}", commandClass, parameters, context);

        C command = instantiateCommand(commandClass);

        parameterInjector.processInjection(command, parameters);

        return command.execute(context);
    }

    private <R, C extends Command<R>> C instantiateCommand(Class<C> commandClass) {
        try {
            return commandClass.getDeclaredConstructor().newInstance();

        } catch (NoSuchMethodException
                | InstantiationException
                | IllegalAccessException
                | InvocationTargetException e) {
            throw new RuntimeException("Failed to instantiate Command: " + commandClass, e);
        }
    }
}
