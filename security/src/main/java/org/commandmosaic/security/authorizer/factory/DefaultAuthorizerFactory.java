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

package org.commandmosaic.security.authorizer.factory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.commandmosaic.api.Command;
import org.commandmosaic.api.CommandContext;
import org.commandmosaic.api.executor.ParameterSource;
import org.commandmosaic.security.AccessDeniedException;
import org.commandmosaic.security.annotation.Access;
import org.commandmosaic.security.authorizer.Authorizer;
import org.commandmosaic.security.core.Identity;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class DefaultAuthorizerFactory extends AuthorizerFactory {

    private static class PublicAccessAuthorizer implements Authorizer {

        private static final PublicAccessAuthorizer INSTANCE = new PublicAccessAuthorizer();

        @Override
        public void checkAuthorization(Class<? extends Command<?>> commandClass,
                                       Identity identity,
                                       ParameterSource parameters,
                                       CommandContext context) {
            // no-op: public access is allowed
        }

        @Override
        public boolean isAuthenticationRequired() {
            return false;
        }
    }

    private static class RequiresAuthenticationAuthorizer implements Authorizer {

        private static final RequiresAuthenticationAuthorizer INSTANCE =
                new RequiresAuthenticationAuthorizer();

        // CPD-OFF
        @Override
        public void checkAuthorization(Class<? extends Command<?>> commandClass,
                                       Identity identity,
                                       ParameterSource parameters,
                                       CommandContext context) {

            if (identity == null) {
                throw new AccessDeniedException("Authentication is required to access: " + commandClass.getName());
            }
        }
        // CPD-ON

        @Override
        public boolean isAuthenticationRequired() {
            return true;
        }
    }

    private static class RequiresAnyOfTheAuthoritiesAuthorizer implements Authorizer {

        private final Set<String> requiredAuthorities;

        private RequiresAnyOfTheAuthoritiesAuthorizer(Set<String> requiredAuthorities) {
            this.requiredAuthorities = requiredAuthorities;
        }

        @Override
        public void checkAuthorization(Class<? extends Command<?>> commandClass,
                                       Identity identity,
                                       ParameterSource parameters,
                                       CommandContext context) {

            if (identity == null) {
                throw new AccessDeniedException("Authentication is required to access: " + commandClass.getName());
            }

            final Set<String> presentAuthorities = identity.getAuthorities();
            if (presentAuthorities == null
                    || presentAuthorities.stream().noneMatch(requiredAuthorities::contains)) {

                // None of the required authorities is present for the given user
                throw new AccessDeniedException("Access Denied: " + commandClass.getName());
            }
        }

        @Override
        public boolean isAuthenticationRequired() {
            return true;
        }
    }

    // CPD-OFF
    private final LoadingCache<Set<String>, Authorizer> authorizerCache = CacheBuilder.newBuilder()
            .softValues().build(new CacheLoader<Set<String>, Authorizer>() {
                @Override
                public Authorizer load(Set<String> authorities) {
                    return new RequiresAnyOfTheAuthoritiesAuthorizer(authorities);
                }
            });
    // CPD-ON

    @Override
    public Authorizer getAuthorizer(Class<? extends Command<?>> clazz) {

        Access.IsPublic isPublicAnnotation =
                getAnnotationFromClassHierarchy(clazz, Access.IsPublic.class);

        Access.RequiresAuthentication requiresAuthenticationAnnotation =
                getAnnotationFromClassHierarchy(clazz, Access.RequiresAuthentication.class);


        Access.RequiresAnyOfTheAuthorities requiresAnyOfTheAuthoritiesAnnotation =
                getAnnotationFromClassHierarchy(clazz, Access.RequiresAnyOfTheAuthorities.class);

        checkAnnotations(clazz, isPublicAnnotation, requiresAuthenticationAnnotation, requiresAnyOfTheAuthoritiesAnnotation);

        Authorizer authorizer;
        if (isPublicAnnotation != null) {
            authorizer = PublicAccessAuthorizer.INSTANCE;
        } else if (requiresAuthenticationAnnotation != null) {
            authorizer = RequiresAuthenticationAuthorizer.INSTANCE;
        } else if (requiresAnyOfTheAuthoritiesAnnotation != null) {
            /*
            We implement the Flyweight pattern here: if multiple commands are annotated
            with Access.RequiresAnyOfTheAuthorities with the same set of authorities, we return
            the same Authorizer instance.
            */
            authorizer = getRequiresAnyOfTheAuthoritiesAuthorizer(clazz, requiresAnyOfTheAuthoritiesAnnotation);
        } else {
            throw new IllegalStateException("Annotation check reached an invalid state");
        }

        return authorizer;
    }

    private Authorizer getRequiresAnyOfTheAuthoritiesAuthorizer(
            Class<? extends Command<?>> clazz,
            Access.RequiresAnyOfTheAuthorities requiresAnyOfTheAuthoritiesAnnotation) {

        try {
            final String[] authorities = requiresAnyOfTheAuthoritiesAnnotation.value();
            if (authorities == null || authorities.length == 0) {
                throw new IllegalStateException(
                        "@RequiresAnyOfTheAuthorities annotation does not declare any authorities on: " + clazz.getName());
            }

            final Set<String> authoritiesSet = ImmutableSet.copyOf(authorities);

            return authorizerCache.get(authoritiesSet);

        } catch (ExecutionException | UncheckedExecutionException e) {
            // should not happen
            throw new IllegalStateException("Failed to fetch command access metadata for " + clazz.getName(), e);
        }
    }

    private void checkAnnotations(Class<? extends Command<?>> clazz,
                                  Access.IsPublic isPublicAnnotation,
                                  Access.RequiresAuthentication requiresAuthenticationAnnotation,
                                  Access.RequiresAnyOfTheAuthorities requiresAnyOfTheAuthoritiesAnnotation) {

        if (isPublicAnnotation == null
                && requiresAuthenticationAnnotation == null
                && requiresAnyOfTheAuthoritiesAnnotation == null) {
            throw new IllegalStateException("When security is used, a class must be either annotated with " +
                    "@IsPublic, @RequiresAuthentication or @RequiresAnyOfTheAuthorities");
        }

        if (isPublicAnnotation != null && requiresAuthenticationAnnotation != null) {
            throw new IllegalStateException(
                    "Both @IsPublic and @RequiresAuthentication are present on " + clazz);
        }

        if (isPublicAnnotation != null && requiresAnyOfTheAuthoritiesAnnotation != null) {
            throw new IllegalStateException(
                    "Both @IsPublic and @RequiresAnyOfTheAuthorities are present on " + clazz);
        }
    }

    private <A extends Annotation> A getAnnotationFromClassHierarchy(Class<?> clazz, Class<A> annotationClass) {
        A annotation;
        do {
            annotation = clazz.getAnnotation(annotationClass);
            if (annotation != null) {
                break;
            }

            clazz = clazz.getSuperclass();
        } while (clazz != null && clazz != Object.class);

        return annotation;
    }

}
