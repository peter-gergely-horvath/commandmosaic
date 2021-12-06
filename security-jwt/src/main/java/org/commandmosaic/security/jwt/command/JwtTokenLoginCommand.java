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

import org.commandmosaic.api.CommandContext;
import org.commandmosaic.security.AuthenticationException;

import org.commandmosaic.security.core.Identity;
import org.commandmosaic.security.jwt.core.TokenProvider;
import org.commandmosaic.security.login.command.LoginCommand;

import java.util.Map;
import java.util.Objects;

public abstract class JwtTokenLoginCommand extends LoginCommand<Identity, String> {

    private static final String REMEMBER_ME_KEY = "rememberMe";

    private final TokenProvider tokenProvider;

    protected JwtTokenLoginCommand(TokenProvider tokenProvider) {
        this.tokenProvider = Objects.requireNonNull(tokenProvider, "argument tokenProvider cannot be null");
    }

    protected String getLoginResponse(Identity identity, CommandContext context) {

        final Map<String, Object> auth = context.getAuth();

        boolean rememberMe = false;
        if (auth != null) {
            Object valueObject = auth.get(REMEMBER_ME_KEY);
            if (valueObject != null) {
                if (valueObject instanceof Boolean) {
                    rememberMe = (Boolean) valueObject;
                } else if (valueObject instanceof String) {
                    if ("true".equalsIgnoreCase((String)valueObject)) {
                        rememberMe = true;
                    } else if ("false".equalsIgnoreCase((String)valueObject)) {
                        rememberMe = false;
                    } else {
                        throw new AuthenticationException(
                                "Could not map value of '" + REMEMBER_ME_KEY + "' to a boolean: " + valueObject);
                    }
                } else
                    throw new AuthenticationException(
                            "'" + REMEMBER_ME_KEY + "' must be a String, but was: " + valueObject.getClass());
            }
        }

        return tokenProvider.createToken(identity, rememberMe);
    }
}
