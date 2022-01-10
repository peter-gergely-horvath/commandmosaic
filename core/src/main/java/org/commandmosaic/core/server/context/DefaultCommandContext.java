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

 
package org.commandmosaic.core.server.context;

import com.google.common.collect.ImmutableMap;
import org.commandmosaic.api.CommandContext;

import java.security.Principal;
import java.util.*;

public final class DefaultCommandContext implements CommandContext {

    private static final class AttributeEntry {
        private final AttributeType attributeType;
        private final Object value;

        private AttributeEntry(AttributeType attributeType, Object value) {
            this.attributeType = attributeType;
            this.value = value;
        }
    }


    private Map<String, Object> auth;
    private Map<String, AttributeEntry> attributes;


    public DefaultCommandContext() {

    }

    public DefaultCommandContext(DefaultCommandContext other) {
        this.setAuth(other.getAuth());

        //noinspection IncompleteCopyConstructor: only create a defensive copy if it's not null
        this.attributes = other.attributes != null ? new HashMap<>(other.attributes) : null;
    }


    public DefaultCommandContext(Map<String, Object> auth) {
        this.setAuth(auth);
    }


    @Override
    public Map<String, Object> getAuth() {
        return auth;
    }

    @Override
    public Principal getCallerPrincipal() {
        return null;
    }

    @Override
    public Iterable<String> getAttributeNames() {
        if (attributes == null) {
            return Collections::emptyIterator;
        } else {
            return Collections.unmodifiableSet(attributes.keySet());
        }

    }

    @Override
    public boolean containsAttribute(String key) {
        return attributes != null && attributes.containsKey(key);
    }

    @Override
    public <T> Optional<T> getAttribute(String key, Class<T> desiredClass) {

        if(attributes != null) {
            AttributeEntry entry = attributes.get(key);

            if (entry != null) {
                Object entryValue = entry.value;
                if (entryValue != null) {
                    Class<?> entryClass = entry.value.getClass();
                    if(desiredClass.isAssignableFrom(entryClass)) {
                        @SuppressWarnings("unchecked")
                        T castEntryValue = (T)entryValue;
                        return Optional.of(castEntryValue);
                    } else {
                        throw new IllegalArgumentException("Cannot retrieve Attribute as [" + desiredClass
                                +"], as its type is [" + entryClass +"]");
                    }
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public void setAttribute(String key, Object value, AttributeType attributeType) {
        if (attributes == null) {
            attributes = new HashMap<>();
        }

        AttributeEntry existingAttributeEntry = attributes.get(key);
        if (existingAttributeEntry != null
                && existingAttributeEntry.attributeType == AttributeType.Immutable) {

            throw new IllegalStateException("An immutable Attribute cannot be re-defined: ["+key+"]");
        }

        attributes.put(key, new AttributeEntry(attributeType, value));
    }


    public void setAuth(Map<String, Object> auth) {
        this.auth = auth != null ? ImmutableMap.copyOf(auth) : null;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultCommandContext{");
        sb.append("authentication=").append(auth);
        sb.append(", attributes=").append(attributes);
        sb.append('}');
        return sb.toString();
    }
}
