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

import org.commandmosaic.security.core.Identity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

class UserNamePasswordIdentity implements Identity {

    private final String userName;
    private final String password;
    private final Set<String> authorities;

    UserNamePasswordIdentity(String userName, String password, String... authorities) {
        this.userName = userName;
        this.password = password;
        this.authorities = new HashSet<>(Arrays.asList(authorities));
    }

    @Override
    public String getName() {
        return userName;
    }

    @Override
    public Set<String> getAuthorities() {
        return authorities;
    }

    public String getPassword() {
        return password;
    }
}
