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

import org.commandmosaic.api.configuration.CommandDispatcherConfiguration;
import org.commandmosaic.api.factory.CommandDispatcherFactory;
import org.commandmosaic.api.server.CommandDispatcherServer;

import java.util.Map;

/**
 * <p>
 * Provides various methods to dispatch (request execution of) {@link Command}s.
 * </p>
 *
 * <p>
 * Normally, there will be only one central {@code CommandDispatcher} instance required in an application.
 * </p>
 *
 * <p>
 * A {@code CommandDispatcher} is built through a
 * {@link CommandDispatcherFactory CommandDispatcherFactory} factory class
 * depending on the environment used (e.g. whether or not Spring Framework is used) by passing a
 * {@link CommandDispatcherConfiguration CommandDispatcherConfiguration}.
 * Building a new instance can be an expensive operation: as a result, a {@code CommandDispatcher}
 * should be built once and re-used later on for servicing multiple dispatch requests.
 * </p>
 *
 * <p>
 * When {@code CommandDispatcher} is exposed as a service for remote clients, users probably want to use a
 * {@link CommandDispatcherServer CommandDispatcherServer}
 * implementation, that wraps the {@code CommandDispatcher} and provides standardized request-response
 * marshalling/unmarshalling: this approach is by default used for example in AWS Lambda Request Handlers.
 * </p>
 *
 * <p>
 * The dispatcher takes full ownership of parameters passed: parameters passed
 * (e.g. a mutable collection) <b>MUST NOT</b> changed after the method is called.
 * No defensive copying of collections or deep cloning of objects will be performed.
 * </p>
 *
 * <p>
 * {@code CommandDispatcher} instances are Thread-safe, they can safely be used across multiple
 * threads without external synchronisation.
 * </p>
 *
 * @see CommandDispatcherConfiguration
 * @see CommandDispatcherServer
 *
 * @author Peter G. Horvath
 */
public interface CommandDispatcher {
    /**
     * <p>
     * Dispatches the specified command for execution with the parameters supplied.
     * </p>
     *
     * @param commandName the name of the command to dispatch (cannot be {@code null})
     * @param parameters the parameters of the command (might be {@code null})
     * @param context the context object containing additional context information (might be {@code null})
     *
     * @return the return value of the command
     */
    Object dispatchCommand(String commandName,
                           Map<String, Object> parameters,
                           CommandContext context);

    /**
     * <p>
     * Dispatches the specified command for execution with the parameters supplied.
     * </p>
     *
     * @param commandClass the class of the command to dispatch (cannot be {@code null})
     * @param parameters the parameters of the command (might be {@code null})
     * @param context the context object containing additional context information (might be {@code null})
     * @param <R> the return type of the command
     * @param <C> the class of the command
     *
     * @return the return value of the command
     */
    <R, C extends Command<R>> R dispatchCommand(Class<C> commandClass,
                                                Map<String, Object> parameters,
                                                CommandContext context);

    /**
     * <p>
     * Dispatches the command specified by the command prototype for execution:
     * the class of the prototype determines the command class to be dispatched
     * and parameters set in the prototype are passed as parameters of the
     * dispatched command.
     * </p>
     *
     * <p>
     * This method uses the <i>Prototype pattern</i>: the {@code commandPrototype}
     * object acts solely as a specification of command class and parameters to apply.
     * All input parameters must be annotated with {@code @Parameter} annotation:
     * parameters unmarked will not be passed to the actual command instance.
     * </p>
     *
     * <p>
     * This method might be useful when the {@code CommandDispatcher} is used locally,
     * without being exposed as a remote service: users can construct an instance of
     * the desired command, inject the parameters either via constructor parameters
     * or setters, then submit it to the {@code CommandDispatcher} as a prototype.
     * </p>
     *
     * <p>
     * As an optimisation, a {@code CommandDispatcher} implementation <b>might</b> elect
     * to invoke the {@code execute} method of the passed {@code Command} object
     * directly, but behaving this way is fully optional and happens at the discretion
     * of the specific implementation. A {@code Command} implementation is explicitly
     * forbidden from relying on such undocumented, non-standardized internal optimization
     * behaviour: all input parameter fields must be annotated with {@code @Parameter},
     * otherwise their propagation to the actual {@code Command} invoked is not guaranteed
     * at all.
     * </p>
     *
     * @param commandPrototype the prototype of the command to dispatched (cannot be {@code null})
     * @param context the context object containing additional context information (might be {@code null})
     * @param <R> the return type of the command
     * @param <C> the class of the command
     *
     * @return the return value of the command
     */
    <R, C extends Command<R>> R dispatchCommand(C commandPrototype,
                                                CommandContext context);

}
