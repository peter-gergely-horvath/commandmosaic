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

package org.commandmosaic.security.interceptor;

import org.commandmosaic.api.CommandContext;
import org.commandmosaic.security.AuthenticationException;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

final class MockAbstractSecurityCommandInterceptor extends AbstractSecurityCommandInterceptor {

    private final Map<String, String> userNameToPasswordMap;
    private final Map<String, Set<String>> userNameToRolesMap;

    MockAbstractSecurityCommandInterceptor(
            Map<String, String> userNameToPasswordMap, Map<String, Set<String>> userNameToRolesMap) {
        this.userNameToPasswordMap = userNameToPasswordMap;
        this.userNameToRolesMap = userNameToRolesMap;
    }


    @Override
    protected Set<String> attemptLogin(CommandContext commandContext) {
        Map<String, Object> auth = commandContext.getAuth();
        if (auth != null) {
            String userName = (String) auth.get("username");
            String password = (String) auth.get("password");

            String storedPassword = userNameToPasswordMap.get(userName);

            if (storedPassword == null) {
                throw new AuthenticationException("Failed to authenticate");
            } else if (!storedPassword.equals(password)) {
                throw new AuthenticationException("Failed to authenticate");
            } else {
                return userNameToRolesMap.get(userName);
            }
        }

        throw new AuthenticationException("Failed to login: no auth");
    }
}
