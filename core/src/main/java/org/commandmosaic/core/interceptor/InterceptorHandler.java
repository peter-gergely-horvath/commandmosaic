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

package org.commandmosaic.core.interceptor;

import org.commandmosaic.api.Command;
import org.commandmosaic.api.CommandContext;
import org.commandmosaic.api.executor.CommandExecutor;
import org.commandmosaic.api.executor.ParameterSource;
import org.commandmosaic.api.interceptor.CommandInterceptor;

public class InterceptorHandler implements CommandExecutor {

    private final CommandExecutor next;
    private final CommandInterceptor commandInterceptor;

    public InterceptorHandler(CommandInterceptor commandInterceptor, CommandExecutor next) {
        this.next = next;
        this.commandInterceptor = commandInterceptor;
    }


    @Override
    public <R, C extends Command<R>> R execute(
            Class<C> commandClass, ParameterSource parameters, CommandContext context) {

        return commandInterceptor.intercept(commandClass, parameters, context, next);
    }

    @Override
    public String toString() {
        return "InterceptorHandler{" +
                "next=" + next +
                ", commandInterceptor=" + commandInterceptor +
                '}';
    }
}
