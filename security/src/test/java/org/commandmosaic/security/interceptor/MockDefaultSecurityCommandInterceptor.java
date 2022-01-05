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

package org.commandmosaic.security.interceptor;

import org.commandmosaic.api.CommandContext;
import org.commandmosaic.security.AuthenticationException;
import org.commandmosaic.security.authenticator.Authenticator;
import org.commandmosaic.security.core.Identity;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

final class MockDefaultSecurityCommandInterceptor extends DefaultSecurityCommandInterceptor {

    MockDefaultSecurityCommandInterceptor(
            UserNamePasswordIdentity... callerIdentities) {

        super(new MockAuthenticator(Arrays.asList(callerIdentities)));
    }

    private static class MockAuthenticator implements Authenticator {

        private final Collection<UserNamePasswordIdentity> users;

        private MockAuthenticator(Collection<UserNamePasswordIdentity> users) {
            this.users = users;
        }

        @Override
        public Identity authenticate(CommandContext commandContext) {
            Map<String, Object> auth = commandContext.getAuth();
            if (auth != null) {
                String userName = (String) auth.get("username");
                String password = (String) auth.get("password");

                UserNamePasswordIdentity callerIdentity = users.stream()
                        .filter(it -> it.getName().equalsIgnoreCase(userName))
                        .findFirst()
                        .orElseThrow(() -> new AuthenticationException("Failed to authenticate: user is not found"));

                if (!callerIdentity.getPassword().equals(password)) {
                    throw new AuthenticationException("Failed to authenticate: password is invalid");
                }

                return callerIdentity;
            }

            return null;
        }
    }
}
