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

 
package org.commandmosaic.api.interceptor;

import org.commandmosaic.api.Command;
import org.commandmosaic.api.CommandContext;
import org.commandmosaic.api.executor.ParameterSource;

/**
 * <p>
 * A {@code CommandInterceptor} intercepts {@link Command} dispatch instructions
 * and performs tasks on either the dispatch instruction details (e.g. parameter or
 * context information) or the, or on the result returned from a {@link Command}, or both.
 * </p>
 *
 * <p>
 * {@code CommandInterceptor}s perform intercepting in the
 * {@link CommandInterceptor#intercept(Class, ParameterSource, CommandContext, InterceptorChain)} intercept}
 * method.
 * </p>
 *
 * <p>
 * {@code CommandInterceptor}s can be used for a variety of purposes,
 * like mapping command results to a desired view model format,
 * authenticating the caller user in a remote usage scenario etc.
 * </p>
 */
public interface CommandInterceptor {
    /**
     * <p>
     * The <code>intercept</code> method of the {@code CommandInterceptor} is
     * called by {@code org.commandmosaic.api.CommandDispatcher CommandDispatcher}
     * each time a dispatch instruction is received through its API methods.
     * </p>
     *
     * <p>
     * The {@code InterceptorChain} passed in to this method allows the
     * {@code CommandInterceptor} to propagate the dispatch instruction
     * to the next entity in the chain.
     * </p>
     *
     * <p>
     * A {@code CommandInterceptor} is free to change the parameters passed to the
     * rest of the processing chain or to return a value different from the one
     * returned by the rest of the chain, thus altering the behaviour.
     * </p>
     *
     * <p>
     * If a {@code CommandInterceptor} throws an {@code Exception},
     * processing of the chain halts and the {@code Exception} is
     * immediately propagated back to the caller.
     * </p>
     *
     * <p>
     * If a {@code CommandInterceptor} does not invoke {@code execute} method
     * on the {@code InterceptorChain} passed in to this method, the rest of
     * the invocation chain, including the specified {@code Command} class is
     * not called at all.
     * </p>
     *
     * @param commandClass the class of the command to dispatch (cannot be {@code null})
     * @param parameters the parameters of the command (might be {@code null})
     * @param context the context object containing additional context information (might be {@code null})
     * @param next the next element in the chain
     *
     * @param <R> the return type of the command
     * @param <C> the class of the command

     * @return the value to be returned to the caller
     */
    <R, C extends Command<R>> R intercept(Class<C> commandClass,
                                          ParameterSource parameters,
                                          CommandContext context,
                                          InterceptorChain next);
}
