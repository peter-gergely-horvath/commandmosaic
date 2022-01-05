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

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

final class SecurityAwareCommandContext implements CommandContext {

    private final CommandContext commandContext;
    private final Principal principal;

    SecurityAwareCommandContext(CommandContext commandContext, Principal principal) {
        this.commandContext = commandContext;
        this.principal = principal;
    }

    @Override
    public Map<String, Object> getAuth() {
        return commandContext.getAuth();
    }

    @Override
    public Principal getCallerPrincipal() {
        return principal;
    }

    @Override
    public <T> Optional<T> getAttribute(String key, Class<T> clazz) {
        return commandContext.getAttribute(key, clazz);
    }

    @Override
    public Iterable<String> getAttributeNames() {
        return commandContext.getAttributeNames();
    }

    @Override
    public boolean containsAttribute(String key) {
        return commandContext.containsAttribute(key);
    }

    @Override
    public void setAttribute(String key, Object value, AttributeType attributeType) {
        commandContext.setAttribute(key, value, attributeType);
    }
}
