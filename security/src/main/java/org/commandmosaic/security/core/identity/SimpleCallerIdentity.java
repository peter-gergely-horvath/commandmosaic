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

package org.commandmosaic.security.core.identity;

import org.commandmosaic.security.core.CallerIdentity;

import java.util.Objects;
import java.util.Set;

public final class SimpleCallerIdentity implements CallerIdentity {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final Set<String> roles;

    public SimpleCallerIdentity(String name, Set<String> roles) {
        this.name = name;
        this.roles = roles;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<String> getRoles() {
        return roles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleCallerIdentity that = (SimpleCallerIdentity) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(roles, that.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, roles);
    }

    @Override
    public String toString() {
        return "SimpleCallerIdentity{" +
                "name='" + name + '\'' +
                ", roles=" + roles +
                '}';
    }
}
