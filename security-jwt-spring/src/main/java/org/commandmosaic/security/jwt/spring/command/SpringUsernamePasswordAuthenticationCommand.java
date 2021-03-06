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

package org.commandmosaic.security.jwt.spring.command;

import org.commandmosaic.security.core.identity.SimpleCallerIdentity;
import org.springframework.security.authentication.AuthenticationManager;
import org.commandmosaic.security.core.CallerIdentity;
import org.commandmosaic.security.jwt.command.UsernamePasswordAuthenticationCommand;
import org.commandmosaic.security.jwt.core.TokenProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class SpringUsernamePasswordAuthenticationCommand extends UsernamePasswordAuthenticationCommand {

    private final AuthenticationManager authenticationManager;

    public SpringUsernamePasswordAuthenticationCommand(TokenProvider tokenProvider,
                                                       AuthenticationManager authenticationManager) {
        super(tokenProvider);
        this.authenticationManager = Objects.requireNonNull(authenticationManager);
    }

    protected CallerIdentity authenticate(String username, String password) {

        if (username == null) {
            username = "";
        }

        if (password == null) {
            password = "";
        }

        username = username.trim();
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);

        Authentication authentication = authenticationManager.authenticate(authRequest);

        Collection<? extends GrantedAuthority> grantedAuthorities = authentication.getAuthorities();

        String name = authentication.getName();
        Set<String> authorities = grantedAuthorities.stream().
                map(SpringUsernamePasswordAuthenticationCommand::mapToString)
                .collect(Collectors.toUnmodifiableSet());

        return new SimpleCallerIdentity(name, authorities);
    }

    private static String mapToString(GrantedAuthority grantedAuthority) {
        String authority = grantedAuthority.getAuthority();
        if (authority == null) {
            throw new IllegalStateException("authority is null: " + SpringUsernamePasswordAuthenticationCommand.class +
                            " does not support this case");
        }

        return authority;
    }
}
