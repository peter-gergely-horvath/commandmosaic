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

 
package org.commandmosaic.security.annotation;

import org.commandmosaic.security.interceptor.AbstractSecurityCommandInterceptor;

import java.lang.annotation.*;

/**
 * <p>
 * Marks commands that can be accessed only after authentication
 * with the user having the roles specified, in the annotation.
 * </p>
 * <p>
 * NOTE: for the security to work properly, a corresponding security
 * {@code CommandInterceptor} must be configured.
 * </p>
 * <p>
 * If there is any required role specified in
 * {@link RestrictedAccess#requiredRoles()},
 * the command cannot be executed unless the user has all the roles.
 * </p>
 * <p>
 * This annotation is mutually exclusive with
 * {@code @UnauthenticatedAccess}: a command class annotated
 * with {@code @RestrictedAccess} cannot be annotated with
 * {@code @UnauthenticatedAccess} and vice versa.
 * </p>
 *
 * @see AbstractSecurityCommandInterceptor
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RestrictedAccess {
    /**
     * Declares the roles required to execute the command.
     * Might be empty, in which case only authentication
     * is required for executing the command.
     * If the caller is not authenticated or does not have at
     * least one of the {@code requiredRoles}, the command
     * cannot be executed.
     *
     * @return an array of role names required to execute the command.
     *      Might be empty, in which case only authentication is required for executing the command.
     */
    String[] requiredRoles();
}