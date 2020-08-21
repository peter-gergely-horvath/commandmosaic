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

 
package org.commandmosaic.core;

import org.commandmosaic.api.Command;
import org.commandmosaic.api.CommandDispatcher;
import org.commandmosaic.api.CommandContext;
import org.commandmosaic.api.executor.CommandExecutor;
import org.commandmosaic.api.configuration.CommandDispatcherConfiguration;

import org.commandmosaic.api.executor.ParameterSource;
import org.commandmosaic.api.server.NoSuchCommandException;
import org.commandmosaic.core.parameter.source.ParameterSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

public class DefaultCommandDispatcher implements CommandDispatcher {

    private static final Logger log = LoggerFactory.getLogger(DefaultCommandDispatcher.class);

    protected final String packageName;

    private final CommandExecutor commandExecutor;

    public DefaultCommandDispatcher(CommandDispatcherConfiguration configuration, CommandExecutor commandExecutor) {

        Objects.requireNonNull(configuration, "configuration cannot be null");
        Objects.requireNonNull(commandExecutor, "commandExecutor cannot be null");

        this.packageName = configuration.getPackageName();
        Objects.requireNonNull(this.packageName, "CommandDispatcher package name cannot be null");
        if (this.packageName.trim().isEmpty()) {
            throw new IllegalArgumentException("CommandDispatcher package name cannot be empty");
        }
        this.commandExecutor = commandExecutor;
    }

    @Override
    public Object dispatchCommand(
            String commandName, Map<String, Object> parameters, CommandContext context) {

        return dispatchCommand(resolveCommandClass(commandName), parameters, context);
    }

    @Override
    public <R, C extends Command<R>> R dispatchCommand(
            Class<C> commandClass, Map<String, Object> parameters, CommandContext context) {

        ParameterSource parameterSource = ParameterSources.mapParameterSource(parameters);
        return dispatchClass(commandClass, parameterSource, context);
    }

    @Override
    public <R, C extends Command<R>> R dispatchCommand(
            C commandPrototype, CommandContext context) {

        Objects.requireNonNull(commandPrototype, "argument commandPrototype cannot be null");

        @SuppressWarnings("unchecked")
        Class<? extends Command<R>> prototypeClass = (Class<? extends Command<R>>) commandPrototype.getClass();

        ParameterSource parameterSource = ParameterSources.prototypeParameterSource(commandPrototype);
        return dispatchClass(prototypeClass, parameterSource, context);
    }

    private <R, C extends Command<R>> R dispatchClass(
            Class<C> commandClass, ParameterSource parameters, CommandContext context) {

        if (!commandClass.getName().startsWith(packageName)) {
            throw new IllegalArgumentException("Command ["+ commandClass +"] is not inside the exposed package");
        }

        return commandExecutor.execute(commandClass, parameters, context);
    }


    protected Class<? extends Command<Object>> resolveCommandClass(String commandName) {
        try {
            String commandRelativeClass = commandName.replaceAll("/", ".");
            String className = String.format("%s.%s", packageName, commandRelativeClass);

            log.debug("Resolved command '{}' to class name '{}'", commandName, className);

            Class<?> loadedClass = Class.forName(className);

            if (!Command.class.isAssignableFrom(loadedClass)) {
                throw new IllegalArgumentException("The supplied name does not identify a command: " + commandName);
            }

            @SuppressWarnings("unchecked") // previous if statement guards type cast
            Class<? extends Command<Object>> commandClass = (Class<? extends Command<Object>>) loadedClass;
            return commandClass;

        } catch (ClassNotFoundException e) {
            throw new NoSuchCommandException("No such command: " + commandName);
        }

    }

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName())
                .append("@").append(Integer.toHexString(hashCode())).append("{")
                .append("packageName='").append(packageName).append('\'')
                .append('}').toString();
    }
}
