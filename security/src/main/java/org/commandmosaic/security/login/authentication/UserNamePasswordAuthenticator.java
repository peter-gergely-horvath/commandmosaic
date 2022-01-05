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


package org.commandmosaic.security.login.authentication;

import org.commandmosaic.api.CommandContext;
import org.commandmosaic.security.AuthenticationException;
import org.commandmosaic.security.authenticator.Authenticator;
import org.commandmosaic.security.core.Identity;

import java.util.Map;
import java.util.Objects;

public class UserNamePasswordAuthenticator implements Authenticator {

    private static final String USER_KEY = "user";
    private static final String PASSWORD_KEY = "password";

    private final UsernamePasswordAuthenticationService usernamePasswordAuthenticationService;

    public UserNamePasswordAuthenticator(UsernamePasswordAuthenticationService usernamePasswordAuthenticationService) {
        this.usernamePasswordAuthenticationService = Objects.requireNonNull(usernamePasswordAuthenticationService);
    }

    @Override
    public final Identity authenticate(CommandContext commandContext) throws AuthenticationException {
        Map<String, Object> auth = commandContext.getAuth();
        String user = null;
        String password = null;

        if (auth != null) {
            user = getValue(auth, USER_KEY);
            password = getValue(auth, PASSWORD_KEY);
        }

        if (user != null && password == null) {
            throw new AuthenticationException("Cannot authenticate: password is missing");
        }

        if (user == null && password != null) {
            throw new AuthenticationException("Cannot authenticate: user is missing");
        }

        return usernamePasswordAuthenticationService.authenticateUser(user, password);
    }

    private String getValue(Map<String, Object> auth, String key) {
        String value = null;
        Object valueObject = auth.get(key);
        if (valueObject != null) {
            if (!(valueObject instanceof String)) {
                throw new AuthenticationException(
                        "'" + key + "' must be a String, but was: " + valueObject.getClass());
            }
            value = (String) valueObject;
        }
        return value;
    }
}
