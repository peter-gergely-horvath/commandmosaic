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

package org.commandmosaic.security.jwt.command;

import org.commandmosaic.api.Command;
import org.commandmosaic.api.CommandContext;
import org.commandmosaic.api.Parameter;
import org.commandmosaic.security.AuthenticationException;
import org.commandmosaic.security.annotation.UnauthenticatedAccess;
import org.commandmosaic.security.core.CallerIdentity;
import org.commandmosaic.security.jwt.core.TokenProvider;

import java.util.Objects;

@UnauthenticatedAccess
public abstract class UsernamePasswordAuthenticationCommand implements Command<String> {

    private final TokenProvider tokenProvider;

    @Parameter
    private String username;

    @Parameter
    private String password;

    @Parameter(required = false)
    private boolean rememberMe;


    protected UsernamePasswordAuthenticationCommand(TokenProvider tokenProvider) {
        this.tokenProvider = Objects.requireNonNull(tokenProvider, "argument tokenProvider cannot be null");
    }

    @Override
    public String execute(CommandContext context) {

        CallerIdentity callerIdentity = authenticate(username, password);
        if (callerIdentity == null) {
            throw new AuthenticationException("Authentication failed");
        }

        return tokenProvider.createToken(callerIdentity, rememberMe);
    }

    protected abstract CallerIdentity authenticate(String user, String password);
}
