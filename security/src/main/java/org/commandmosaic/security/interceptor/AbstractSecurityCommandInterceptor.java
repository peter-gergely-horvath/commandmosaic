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

 
package org.commandmosaic.security.interceptor;

import org.commandmosaic.api.Command;
import org.commandmosaic.api.CommandContext;
import org.commandmosaic.api.executor.ParameterSource;
import org.commandmosaic.api.interceptor.CommandInterceptor;
import org.commandmosaic.api.interceptor.InterceptorChain;
import org.commandmosaic.security.AuthenticationException;
import org.commandmosaic.security.AccessDeniedException;
import org.commandmosaic.security.annotation.RestrictedAccess;
import org.commandmosaic.security.annotation.UnauthenticatedAccess;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;

import java.util.*;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("unused") // API class, sub classed by user code
public abstract class AbstractSecurityCommandInterceptor implements CommandInterceptor {


    private final LoadingCache<Class<?>, Boolean> unauthenticatedAccessCache = CacheBuilder.newBuilder()
            .softValues().build(new CacheLoader<Class<?>, Boolean>() {
                @Override
                public Boolean load(Class<?> clazz) {
                    return loadUnauthenticatedAccess(clazz);
                }
            });

    private final LoadingCache<Class<?>, Set<String>> commandRequiredRolesCache = CacheBuilder.newBuilder()
            .softValues().build(new CacheLoader<Class<?>, Set<String>>() {
                @Override
                public Set<String> load(Class<?> clazz) {
                    return loadCommandRequiredRoles(clazz);
                }
            });


    private Set<String> loadCommandRequiredRoles(Class<?> clazz) {
        RestrictedAccess annotation = clazz.getAnnotation(RestrictedAccess.class);
        if (annotation == null) {
            throw new IllegalStateException("When security is used, a class must be either annotated with " +
                    "@UnauthenticatedAccess or @RestrictedAccess");
        }
        String[] requiredRoles = annotation.requiredRoles();
        return requiredRoles != null && requiredRoles.length != 0 ?
                new HashSet<>(Arrays.asList(requiredRoles)) : Collections.emptySet();

    }


    private Boolean loadUnauthenticatedAccess(Class<?> clazz) {
        UnauthenticatedAccess unauthenticatedAccessAnnotation = clazz.getAnnotation(UnauthenticatedAccess.class);
        if (unauthenticatedAccessAnnotation != null
                && clazz.getAnnotation(RestrictedAccess.class) != null) {
                /*
                fail fast if we detect that both @UnauthenticatedAccess and @RestrictedAccess annotations
                are present: this is clearly invalid. We rather prevent the usage completely than exposing a
                @RestrictedAccess command, which was accidentally marked with @UnauthenticatedAccess as well.
                */
                throw new IllegalStateException(
                        "Both @UnauthenticatedAccess and @RestrictedAccess are present on " + clazz);
        }

        return unauthenticatedAccessAnnotation != null;
    }

    @Override
    public final <R, C extends Command<R>> R intercept(Class<C> commandClass, ParameterSource parameters,
                                                       CommandContext context, InterceptorChain next) {
        /*
        checkAccess prevents propagation by throwing an exception
        if the user does not have the required permissions
        */
        this.checkAccess(commandClass, parameters, context);

        return next.execute(commandClass, parameters, context);
    }

    private <R, C extends Command<R>> void checkAccess(Class<C> commandClass,
                                                       ParameterSource parameters,
                                                       CommandContext context)
            throws AuthenticationException, AccessDeniedException {

        try {
            Boolean unauthenticatedAccess = unauthenticatedAccessCache.get(commandClass);
            Objects.requireNonNull(unauthenticatedAccess, "unauthenticatedAccess cannot be null");

            if (!unauthenticatedAccess) {
                Set<String> requiredRoles = commandRequiredRolesCache.get(commandClass);

                Set<String> rolesExtractedFromTheRequest = attemptLogin(context);

                checkAuthorization(commandClass, requiredRoles, rolesExtractedFromTheRequest);
            }

        }
        catch (ExecutionException | UncheckedExecutionException e) {
            throw new IllegalStateException("Failed to fetch command security metadata for " + commandClass, e);
        }
    }

    protected  <R, C extends Command<R>> void checkAuthorization(Class<C> commandClass,
                                                                 Set<String> requiredRoles,
                                                                 Set<String> presentRoles) {

        if (!requiredRoles.isEmpty() && (presentRoles == null
                        || presentRoles.stream().noneMatch(requiredRoles::contains))) {

            // None of the required roles is present for the given user
            throw new AccessDeniedException("Access Denied: " + commandClass.getName());
        }
    }

    /**
     * Attempts to perform a login based on information available in the
     * command context. Throws {@code AuthenticationException}
     * if authentication was not successful for any reason.
     *
     * @param commandContext the command context
     * @return the roles of the successfully authenticated user
     *
     * @throws AuthenticationException if authentication was not successful
     */
    protected abstract Set<String> attemptLogin(CommandContext commandContext) throws AuthenticationException;

}
