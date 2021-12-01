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

import org.commandmosaic.security.interceptor.DefaultSecurityCommandInterceptor;

import java.lang.annotation.*;

/**
 * <p>
 * Contains a variety of annotations to mark different access levels of commands.
 * </p>
 */
public final class Access {

    private Access() {
        throw new AssertionError("no instances allowed");
    }

    /**
     * <p>
     * Marks commands that can be accessed without authentication.
     * </p>
     * <p>
     * NOTE: for the security to work properly, a corresponding security
     * {@code CommandInterceptor} must be configured.
     * </p>
     * <p>
     * This annotation is mutually exclusive with
     * {@code @RequiresAnyOfTheRoles}: a command class annotated
     * with {@code @IsPublic} cannot be annotated with
     * {@code @RequiresAnyOfTheRoles} and vice versa.
     * </p>
     *
     * @see DefaultSecurityCommandInterceptor
     */
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface IsPublic {

    }

    /**
     * <p>
     * Marks commands that can be accessed only after authentication,
     * but without any further specific requirement.
     * </p>
     * <p>
     * NOTE: for the security to work properly, a corresponding security
     * {@code CommandInterceptor} must be configured.
     * </p>
     * <p>
     * This annotation is mutually exclusive with
     * {@code @IsPublic}: a command class annotated
     * with {@code @RequiresAuthentication} cannot be annotated with
     * {@code @IsPublic} and vice versa.
     * </p>
     * <p>
     * The annotation {@code @RequiresAnyOfTheRoles} implicitly
     * declares the command access to {@code @RequiresAuthentication}:
     * as for role based security to work, the caller's identity must
     * be established via authentication.
     * </p>
     *
     * @see DefaultSecurityCommandInterceptor
     */
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface RequiresAuthentication {

    }

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
     * This annotation is mutually exclusive with
     * {@code @IsPublic}: a command class annotated
     * with {@code @RequiresAnyOfTheRoles} cannot be annotated with
     * {@code @IsPublic} and vice versa.
     * </p>
     *
     * <p>
     * This annotation implicitly declares the command access to
     * {@code @RequiresAuthentication}:
     * as for role based security to work, the caller's identity must
     * be established via authentication.
     * </p>

     *
     * @see DefaultSecurityCommandInterceptor
     */
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface RequiresAnyOfTheRoles {
        /**
         * Declares the roles required to execute the command.
         * If the caller is not authenticated or does not have at
         * least one of the roles listed in {@code value}, the command
         * cannot be executed.
         *
         * @return an array of role names required to execute the command. Must not be empty.
         */
        String[] value();
    }
}
