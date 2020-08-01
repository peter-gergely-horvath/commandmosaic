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

 
package com.github.commandmosaic.core.interceptor;

import com.github.commandmosaic.api.Command;
import com.github.commandmosaic.api.CommandContext;
import com.github.commandmosaic.api.interceptor.InterceptorChain;
import com.github.commandmosaic.api.executor.CommandExecutor;
import com.github.commandmosaic.api.executor.ParameterSource;

public final class DelegatingInterceptorChain implements InterceptorChain {

    private final CommandExecutor commandExecutor;

    public DelegatingInterceptorChain(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    @Override
    public <R, C extends Command<R>> R execute(
            Class<C> commandClass, ParameterSource parameters, CommandContext context) {

        return commandExecutor.execute(commandClass, parameters, context);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DelegatingInterceptorChain{");
        sb.append("commandExecutor=").append(commandExecutor);
        sb.append('}');
        return sb.toString();
    }
}
