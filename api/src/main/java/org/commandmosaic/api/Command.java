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

 
package org.commandmosaic.api;


import org.commandmosaic.api.interceptor.CommandInterceptor;
import org.commandmosaic.api.server.CommandDispatcherServer;

/**
 * <p>
 * A {@code Command} encapsulates the business logic and all the
 * parameter information that is needed to execute it. In concrete
 * {@code Command} implementations, parameters are stored as fields
 * and are annotated with the {@code @Parameter} annotation: these
 * parameter fields are then injected by the framework.
 * </p>
 *
 * <p>
 * {@code Command}s are dispatched via the {@link CommandDispatcher} API,
 * passing parameter values. Application code normally does not call the
 * {@link Command#execute(CommandContext)} method directly.
 * </p>
 *
 * <p>
 * For cases, where the command represents a request coming in from a
 * (remote) client, a
 * {@link CommandDispatcherServer}
 * implementation can be used. Wrappers built around
 * {@code CommandDispatcherServer} allow the command dispatching to be
 * adopted to a variety of runtime environments.
 * </p>
 *
 * <p>
 * {@code CommandContext} passed to {@link Command#execute(CommandContext)}
 * holds additional context information for the command, including optional
 * authentication/authorization information and further, arbitrary "Attribute"
 * objects added by
 * {@link CommandInterceptor CommandInterceptor}s.
 * </p>
 *
 * @param <T> the return type of the command
 *
 * @see CommandDispatcher
 * @see CommandContext
 * @see CommandInterceptor
 */
public interface Command<T> {
    /**
     * <p>
     * Executes the command and returns the result as defined by the concrete Command implementation.
     * </p>
     * <p>
     * This method is normally called by the framework only.
     * Application code should use the {@link CommandDispatcher} API
     * to invoke a command (e.g. if one command calls another command.)
     * </p>
     *
     * @param context {@link CommandContext} with additional context information (might be {@code null})
     *
     * @return the result of the command, as defined by the concrete Command implementation
     */
    T execute(CommandContext context);
}
