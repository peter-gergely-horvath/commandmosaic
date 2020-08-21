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

 
package org.commandmosaic.api;

import org.commandmosaic.api.interceptor.CommandInterceptor;

import java.util.Map;
import java.util.Optional;

/**
 * <p>
 * Provides access to additional contextual information
 * for the execution of the command. Optionally, contains
 * authentication/authorization information and named Attributes
 * (arbitrary objects keyed by a string) that might contain
 * additional environment or use-case specific information.
 * </p>
 * <p>
 * {@link CommandInterceptor CommandInterceptor}s
 * for example can use Attributes to communicate with each other
 * or to convey derived data to the command being executed via Attributes.
 * </p>
 *
 * @see CommandDispatcher
 * @see CommandInterceptor
 *
 */
public interface CommandContext {
    /**
     * <p>
     * If the command dispatch was requested by a remote client, this method
     * returns a read-only view of the authentication/authorization {@code Map} field
     * received as part of the request. This map is optional and contains user defined
     * elements that are used to authenticate the remote client, if applicable. </p>
     *
     * <p>
     * If security checks are required for a particular application, application developers
     * are strongly encouraged to use a
     * {@link CommandInterceptor CommandInterceptor}
     * instead of implementing authentication and authorization logic in all commands.
     * </p>
     *
     * @return read-only authentication/authorization field received as part of the request (might be {@code null})
     */
    Map<String, Object> getAuth();

    /**
     * Returns an {@code Optional} of the specified Attribute with the desired class.
     * The {@code Optional} will be empty if the Attribute was not present, otherwise
     * it will contain that Attribute
     *
     * @param key the attribute key
     * @param clazz the attribute class
     * @param <T> the attribute class
     *
     * @return an {@code Optional} of the specified Attribute, if present, otherwise and empty() {@code Optional}
     */
    <T> Optional<T> getAttribute(String key, Class<T> clazz);

    /**
     * Returns an {@code Iterable} of all Attribute names that
     * exists within this {@code CommandContext} at the time of the method call.
     *
     * @return an {@code Iterable} of the Attribute keys existing at the time of the method call
     */
    Iterable<String> getAttributeNames();

    /**
     * Returns an indication whether the specified Attribute name exists within this
     * {@code CommandContext}.
     *
     * @param key the attribute key
     *
     * @return {@code true} if the specified Attribute name is found, {@code false} otherwise
     */
    boolean containsAttribute(String key);

    /**
     * Sets the specified attribute key to the given value, creating
     *
     * @param key the attribute key
     * @param value the attribute value to set
     * @param attributeType and indication whether the attribute can later be set to a different value
     *                      ({@code Mutable}), or attempt to change it must fail with an exception ({@code Immutable})
     *
     * @throws IllegalStateException if an Attribute was created previously with the same key
     *          and {@code Immutable} {@code AttributeType}
     */
    void setAttribute(String key, Object value, AttributeType attributeType);

    /**
     * <p>
     * Represents the possible types of an Attribute, whether or not
     * an Attribute name set already can be set to a different value
     * in the same {@code CommandContext} later on.</p>
     *
     * <p>
     * The value set of Mutable attribute can later be set any times
     * later on. For an Immutable attribute, the value can only set
     * by once, after which the attempt the set it to a different
     * value will fail with an Exception being thrown.
     * </p>
     *
     * <p>
     * More formally: if {@link CommandContext#containsAttribute(String)} returned
     * {@code true} for a certain key, a {@link CommandContext#setAttribute(String, Object, AttributeType)}
     * call made using the <b>same key</b> will:
     * </p>
     * <ul>
     *     <li>complete successfully if the attribute was previously set with {@code AttributeType}={@code Mutable}</li>
     *     <li>throw an Exception if the attribute was previously set with {@code AttributeType}={@code Immutable}</li>
     * </ul>
     */
    enum AttributeType {
        /**
         * The attribute is mutable, thus its value can be changed
         * after it has been set via
         * {@link CommandContext#setAttribute(String, Object, AttributeType)}
         */
        Mutable,
        /**
         * The attribute is immutable, thus its value can NOT be changed
         * after it has been set via
         * {@link CommandContext#setAttribute(String, Object, AttributeType)}
         */
        Immutable
    }
}
