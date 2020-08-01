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

 
package com.github.commandmosaic.api.interceptor;

import com.github.commandmosaic.api.Command;
import com.github.commandmosaic.api.CommandContext;
import com.github.commandmosaic.api.executor.ParameterSource;

/**
 * An {@code InterceptorChain} represents the invocation chain of an intercepted
 * command dispatch instruction. {@link CommandInterceptor}s use the
 * {@code InterceptorChain} interface to invoke the next {@code CommandInterceptor}
 * in the chain, or if the calling {@code CommandInterceptor} is the last one in
 * the chain, to invoke the {@link com.github.commandmosaic.api.executor.CommandExecutor CommandExecutor}
 * at the end of the chain.
 *
 * @see com.github.commandmosaic.api.interceptor.CommandInterceptor
 * @see com.github.commandmosaic.api.executor.CommandExecutor
 * @see com.github.commandmosaic.api.configuration.CommandDispatcherConfiguration
 *
 */
public interface InterceptorChain {

    /**
     * Causes the next {@code CommandInterceptor} in the chain to be invoked,
     * or if the calling {@code CommandInterceptor} is the last {@code CommandInterceptor}
     * in the chain, causes the {@link com.github.commandmosaic.api.executor.CommandExecutor CommandExecutor}
     * at the end of the chain to be invoked.
     *
     * @param commandClass the class of the command to dispatch (cannot be {@code null})
     * @param parameters the parameters of the command (might be {@code null})
     * @param context the context object containing additional context information (might be {@code null})
     * @param <R> the return type of the command
     * @param <C> the class of the command
     *
     * @return the return value of the command
     */
    <R, C extends Command<R>> R execute(Class<C> commandClass,
                                        ParameterSource parameters,
                                        CommandContext context);
}
