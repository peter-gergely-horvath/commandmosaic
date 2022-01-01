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

package org.commandmosaic.security.login.command;

import org.commandmosaic.api.Command;
import org.commandmosaic.api.CommandContext;
import org.commandmosaic.security.AuthenticationException;
import org.commandmosaic.security.annotation.Access;

import java.security.Principal;

@Access.RequiresAuthentication // causes SecurityCommandInterceptor to authenticate the call
public abstract class LoginCommand<I extends Principal, T> implements Command<T> {

    @Override
    public final T execute(CommandContext context) {

        // SecurityCommandInterceptor should already have authenticated the caller
        final Principal principal = context.getCallerPrincipal();

        if (principal == null) {
            throw new AuthenticationException("Could not login: CallerPrincipal is null. " +
                    "Security is likely not enabled in CommandDispatcher configuration.");
        }

        @SuppressWarnings("unchecked")
        final I identity = (I) principal;
        return getLoginResponse(identity, context);
    }

    protected abstract T getLoginResponse(I identity, CommandContext context);
}
