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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.commandmosaic.api.Command;
import org.commandmosaic.api.CommandContext;
import org.commandmosaic.api.executor.ParameterSource;
import org.commandmosaic.api.interceptor.InterceptorChain;
import org.commandmosaic.security.AccessDeniedException;
import org.commandmosaic.security.AuthenticationException;
import org.commandmosaic.security.annotation.RestrictedAccess;
import org.commandmosaic.security.annotation.UnauthenticatedAccess;
import org.commandmosaic.security.core.CallerIdentity;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("unused") // API class, sub classed by user code
public abstract class AbstractSecurityCommandInterceptor implements SecurityCommandInterceptor {


    private final LoadingCache<Class<?>, Boolean> unauthenticatedAccessCache = CacheBuilder.newBuilder()
            .softValues().build(new CacheLoader<>() {
                @Override
                public Boolean load(Class<?> clazz) {
                    return loadUnauthenticatedAccess(clazz);
                }
            });

    private final LoadingCache<Class<?>, Set<String>> commandRequiredRolesCache = CacheBuilder.newBuilder()
            .softValues().build(new CacheLoader<>() {
                @Override
                public Set<String> load(Class<?> clazz) {
                    return loadCommandRequiredRoles(clazz);
                }
            });


    private Set<String> loadCommandRequiredRoles(Class<?> clazz) {

        RestrictedAccess annotation = getAnnotationFromClassHierarchy(clazz, RestrictedAccess.class);
        if (annotation == null) {
            throw new IllegalStateException("When security is used, a class must be either annotated with " +
                    "@UnauthenticatedAccess or @RestrictedAccess");
        }

        Set<String> rolesSet;
        String[] requiredRoles = annotation.requiredRoles();
        if (requiredRoles.length > 0) {
            rolesSet = Set.of(requiredRoles);
        } else {
            rolesSet = Collections.emptySet();
        }

        return rolesSet;
    }

    private Boolean loadUnauthenticatedAccess(Class<?> clazz) {
        UnauthenticatedAccess unauthenticatedAccessAnnotation =
                getAnnotationFromClassHierarchy(clazz, UnauthenticatedAccess.class);

        if (unauthenticatedAccessAnnotation != null
                && getAnnotationFromClassHierarchy(clazz, RestrictedAccess.class) != null) {
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

    private <A extends Annotation> A getAnnotationFromClassHierarchy(Class<?> clazz, Class<A> annotationClass) {
        A annotation;
        do {
            annotation = (A) clazz.getAnnotation(annotationClass);
            if (annotation != null) {
                break;
            }

            clazz = clazz.getSuperclass();
        } while (clazz != null && clazz != Object.class);

        return annotation;
    }

    @Override
    public final <R, C extends Command<R>> R intercept(Class<C> commandClass, ParameterSource parameters,
                                                       CommandContext context, InterceptorChain next) {

        try {
            Boolean unauthenticatedAccessAllowed = unauthenticatedAccessCache.get(commandClass);
            Objects.requireNonNull(unauthenticatedAccessAllowed,
                    "unauthenticatedAccessAllowed cannot be null");

            if (!unauthenticatedAccessAllowed) {
                Set<String> requiredRoles = commandRequiredRolesCache.get(commandClass);

                CallerIdentity callerIdentity = authenticate(context);
                if (callerIdentity == null) {
                    throw new AccessDeniedException(
                            "Access Denied: authentication required");
                }

                Set<String> callerRoles = callerIdentity.getRoles();

                checkAuthorization(commandClass, requiredRoles, callerRoles);

                context = new SecurityAwareCommandContext(context, callerIdentity);
            }

        } catch (AuthenticationException e) {
            throw new AccessDeniedException(
                    "Access Denied: authentication failure", e);

        } catch (ExecutionException | UncheckedExecutionException e) {
            throw new IllegalStateException("Failed to fetch command security metadata for " + commandClass, e);

        }

        return next.execute(commandClass, parameters, context);
    }

    protected <R, C extends Command<R>> void checkAuthorization(Class<C> commandClass,
                                                                Set<String> requiredRoles,
                                                                Set<String> presentRoles) {

        if (!requiredRoles.isEmpty() && (presentRoles == null
                || presentRoles.stream().noneMatch(requiredRoles::contains))) {

            // None of the required roles is present for the given user
            throw new AccessDeniedException("Access Denied: " + commandClass.getName());
        }
    }

    /**
     * Attempts to authenticate the caller based on information available in the
     * command context. Returns a representation of the caller's identity or <code>null</code>
     * if authentication information was not present.
     * Throws {@code AuthenticationException} if authentication
     * information was present, but authentication was not successful for any reason.
     *
     * @param commandContext the command context
     * @return a <code>CallerIdentity</code>, which represents roles of the successfully authenticated user,
     * or <code>null</code>, if authentication information was not present
     * @throws AuthenticationException if authentication information was present, but authentication failed
     */
    protected abstract CallerIdentity authenticate(CommandContext commandContext) throws AuthenticationException;

}
