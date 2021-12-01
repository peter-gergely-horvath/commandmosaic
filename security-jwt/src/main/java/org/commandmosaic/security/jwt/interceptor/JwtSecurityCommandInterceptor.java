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

package org.commandmosaic.security.jwt.interceptor;

import org.commandmosaic.security.interceptor.DefaultSecurityCommandInterceptor;
import org.commandmosaic.security.jwt.core.TokenProvider;
import org.commandmosaic.api.CommandContext;
import org.commandmosaic.security.AuthenticationException;
import org.commandmosaic.security.core.CallerIdentity;

import org.commandmosaic.security.authenticator.Authenticator;

import java.util.Map;

public class JwtSecurityCommandInterceptor extends DefaultSecurityCommandInterceptor {

    private static final String KEY_TOKEN = "token";

    public JwtSecurityCommandInterceptor(TokenProvider tokenProvider) {
        super(new JwtAuthenticator(tokenProvider));
    }

    private static class JwtAuthenticator implements Authenticator {

        private final TokenProvider tokenProvider;

        private JwtAuthenticator(TokenProvider tokenProvider) {
            this.tokenProvider = tokenProvider;
        }

        @Override
        public CallerIdentity authenticate(CommandContext commandContext) throws AuthenticationException {

            Map<String, Object> auth = commandContext.getAuth();
            if (auth != null) {
                Object token = auth.get(KEY_TOKEN);

                if (token != null) {
                    if (!(token instanceof String)) {
                        throw new AuthenticationException(
                                "'" + KEY_TOKEN + "' must be a String, but was: " + token.getClass());
                    }

                    String tokenString = (String) token;

                    return tokenProvider.getCallerIdentity(tokenString).orElse(null);
                }
            }

            return null;
        }

    }
}
