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

 
package org.commandmosaic.core.factory;

import org.commandmosaic.api.CommandDispatcher;
import org.commandmosaic.api.configuration.CommandDispatcherConfiguration;
import org.commandmosaic.api.configuration.conversion.TypeConversion;
import org.commandmosaic.api.conversion.TypeConversionService;
import org.commandmosaic.api.executor.CommandExecutor;
import org.commandmosaic.api.factory.CommandDispatcherFactory;
import org.commandmosaic.api.interceptor.CommandInterceptor;
import org.commandmosaic.core.DefaultCommandDispatcher;
import org.commandmosaic.core.conversion.DefaultTypeConversionService;
import org.commandmosaic.core.interceptor.InterceptorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

public abstract class AbstractCommandDispatcherFactory implements CommandDispatcherFactory {

    private static final Logger log = LoggerFactory.getLogger(AbstractCommandDispatcherFactory.class);

    @Override
    public final CommandDispatcher getCommandDispatcher(CommandDispatcherConfiguration configuration) {

        log.info("Constructing CommandDispatcher from configuration: {}", configuration);

        CommandExecutor commandExecutor = getCommandExecutor(configuration);

        List<Class<? extends CommandInterceptor>> interceptors = configuration.getInterceptors();
        if (interceptors != null && !interceptors.isEmpty()) {
            log.info("Interceptor(s) configured: {}", interceptors);
            commandExecutor = buildInterceptorChain(commandExecutor, interceptors);
        } else {
            log.info("No interceptors configured");
        }

        return new DefaultCommandDispatcher(configuration, commandExecutor);
    }

    protected CommandExecutor getCommandExecutor(CommandDispatcherConfiguration configuration) {
        log.trace("Constructing CommandExecutor from configuration: {}", configuration);
        Collection<TypeConversion<?,?>> typeConversions = configuration.getTypeConversions();

        log.trace("User defined typeConversions: {}", typeConversions);

        TypeConversionService typeConversionService = new DefaultTypeConversionService(typeConversions);

        return getCommandExecutor(typeConversionService);
    }

    protected abstract CommandExecutor getCommandExecutor(TypeConversionService typeConversionService);

    protected CommandExecutor buildInterceptorChain(CommandExecutor commandExecutor,
                                                    List<Class<? extends CommandInterceptor>> interceptorClasses) {

        if (interceptorClasses == null || interceptorClasses.isEmpty()) {
            throw new IllegalStateException("Interceptor class list cannot be null or empty");
        }

        log.trace("Building interceptor chain; from last to first");

        CommandExecutor currentCommandExecutor = commandExecutor;

        final int lastIndexInList = interceptorClasses.size() - 1;
        for (int i = lastIndexInList; i >= 0; i--) {

            Class<? extends CommandInterceptor> commandInterceptorClass = interceptorClasses.get(i);

            log.trace("Retrieving interceptor instance: {}", commandInterceptorClass);
            CommandInterceptor interceptorInstance = getCommandInterceptor(commandInterceptorClass);

            currentCommandExecutor = new InterceptorHandler(interceptorInstance, currentCommandExecutor);
        }

        log.trace("Interceptor chain created: {}", currentCommandExecutor);
        return currentCommandExecutor;
    }

    protected CommandInterceptor getCommandInterceptor(Class<? extends CommandInterceptor> commandInterceptorClass) {
        try {
            log.trace("Instantiating interceptor class: {}", commandInterceptorClass);
            return commandInterceptorClass.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            log.error("Failed to instantiate interceptor class", e);
            throw new RuntimeException("Could not instantiate " + commandInterceptorClass, e);
        }
    }

}
