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

package org.commandmosaic.spring.security.adapter;

import com.google.common.collect.ImmutableSet;
import org.commandmosaic.api.CommandContext;
import org.commandmosaic.security.AuthenticationException;
import org.commandmosaic.security.authenticator.Authenticator;
import org.commandmosaic.security.core.Identity;
import org.commandmosaic.security.core.SimpleIdentity;
import org.commandmosaic.security.interceptor.DefaultSecurityCommandInterceptor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Set;

/**
 * <p>
 * A security {@code CommandInterceptor} adapter, which retrieves
 * the granted authorities from Spring Security {@link SecurityContextHolder},
 * thus allowing the client to send command dispatch requests without specifying
 * the authentication/authorization information in the request body, but using
 * the method configured for Spring Security.</p>
 *
 * <p>
 * This allows a library user to seamlessly integrate CommandMosaic with an
 * existing Spring or Spring Boot application using the application's existing
 * Spring Security infrastructure.
 * </p>
 */
public final class SpringSecurityCommandInterceptor extends DefaultSecurityCommandInterceptor {

    public SpringSecurityCommandInterceptor() {
        super(new SpringSecurityAdapterAuthenticator());
    }

    private static class SpringSecurityAdapterAuthenticator implements Authenticator {

        @Override
        public Identity authenticate(CommandContext commandContext) throws AuthenticationException {

            SecurityContext context = SecurityContextHolder.getContext();

            Authentication authentication = context.getAuthentication();
            if (authentication == null
                    || authentication instanceof AnonymousAuthenticationToken) {
                return null; // not authenticated
            }

            Collection<? extends GrantedAuthority> grantedAuthorities = authentication.getAuthorities();

            String name = authentication.getName();
            Set<String> authorities = grantedAuthorities.stream().
                    map(this::mapToString)
                    .collect(ImmutableSet.toImmutableSet());

            return new SimpleIdentity(name, authorities);
        }

        private String mapToString(GrantedAuthority grantedAuthority) {
            String authority = grantedAuthority.getAuthority();
            if (authority == null) {
                throw new IllegalStateException(
                        "authority is null: " + SpringSecurityCommandInterceptor.class + " does not support this case");
            }

            return authority;
        }
    }
}
