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

 
package com.github.commandmosaic.security.annotation;

import java.lang.annotation.*;


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
 * {@code @RestrictedAccess}: a command class annotated
 * with {@code @UnauthenticatedAccess} cannot be annotated with
 * {@code @RestrictedAccess} and vice versa.
 * </p>
 *
 * @see com.github.commandmosaic.security.interceptor.AbstractSecurityCommandInterceptor
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UnauthenticatedAccess {

}