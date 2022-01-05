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
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class DefaultAuthorizerFactory extends AuthorizerFactory {

    private static class PublicAccessAuthorizer implements Authorizer {

        // Thread-safe, since no state is held
        private static final PublicAccessAuthorizer INSTANCE = new PublicAccessAuthorizer();

        @Override
        public boolean isAuthenticationRequired() {
            return false;
        }

        @Override
        public void checkAuthorization(Class<? extends Command<?>> commandClass,
                                       Identity identity,
                                       ParameterSource parameters,
                                       CommandContext context) {
            // no-op: public access is allowed
        }
    }

    private static class RequiresAuthenticationAuthorizer implements Authorizer {

        // Thread-safe, since no state is held
        private static final RequiresAuthenticationAuthorizer INSTANCE = new RequiresAuthenticationAuthorizer();

        @Override
        public boolean isAuthenticationRequired() {
            return true;
        }

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
    }

    private static class RequiresAnyOfTheAuthoritiesAuthorizer implements Authorizer {

        // ImmutableSet is thread-safe
        private final ImmutableSet<String> requiredAuthorities;

        private RequiresAnyOfTheAuthoritiesAuthorizer(ImmutableSet<String> requiredAuthorities) {
            Objects.requireNonNull(requiredAuthorities, "argument requiredAuthorities cannot be null");
            this.requiredAuthorities = requiredAuthorities;
        }

        @Override
        public boolean isAuthenticationRequired() {
            return true;
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
        public String toString() {
            return "RequiresAnyOfTheAuthoritiesAuthorizer{requiredAuthorities=" + requiredAuthorities + '}';
        }
    }

    // CPD-OFF
    private final LoadingCache<ImmutableSet<String>, Authorizer> authorizerCache = CacheBuilder.newBuilder()
            .softValues().build(new CacheLoader<ImmutableSet<String>, Authorizer>() {
                @Override
                public Authorizer load(ImmutableSet<String> authorities) {
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

        Access.RequiresAuthority requiresAuthorityAnnotation =
                getAnnotationFromClassHierarchy(clazz, Access.RequiresAuthority.class);

        Access.RequiresAnyOfTheAuthorities requiresAnyOfTheAuthoritiesAnnotation =
                getAnnotationFromClassHierarchy(clazz, Access.RequiresAnyOfTheAuthorities.class);

        checkAnnotations(clazz, isPublicAnnotation, requiresAuthenticationAnnotation,
                requiresAuthorityAnnotation, requiresAnyOfTheAuthoritiesAnnotation);

        Authorizer authorizer;
        if (isPublicAnnotation != null) {
            authorizer = PublicAccessAuthorizer.INSTANCE;

        } else if (requiresAuthenticationAnnotation != null) {
            authorizer = RequiresAuthenticationAuthorizer.INSTANCE;

        } else if (requiresAuthorityAnnotation != null) {
            authorizer = getRequiresAnyOfTheAuthoritiesAuthorizer(clazz, requiresAuthorityAnnotation);

        } else if (requiresAnyOfTheAuthoritiesAnnotation != null) {
            authorizer = getRequiresAnyOfTheAuthoritiesAuthorizer(clazz, requiresAnyOfTheAuthoritiesAnnotation);

        } else {
            throw new IllegalStateException("Annotation check reached an invalid state");
        }

        return authorizer;
    }

    private Authorizer getRequiresAnyOfTheAuthoritiesAuthorizer(
            Class<? extends Command<?>> clazz,
            Access.RequiresAuthority requiresAuthorityAnnotation) {


        final String authority = requiresAuthorityAnnotation.value();
        if (authority.trim().length() == 0) {
            throw new IllegalStateException(
                    "@RequiresAuthority value is empty on: " + clazz.getName());
        }

        return getRequiresAnyOfTheAuthoritiesAuthorizer(clazz, authority);
    }

    private Authorizer getRequiresAnyOfTheAuthoritiesAuthorizer(
            Class<? extends Command<?>> clazz,
            Access.RequiresAnyOfTheAuthorities requiresAnyOfTheAuthoritiesAnnotation) {


            final String[] authorities = requiresAnyOfTheAuthoritiesAnnotation.value();
            if (authorities.length == 0) {
                throw new IllegalStateException(
                        "@RequiresAnyOfTheAuthorities annotation does not declare any authorities on: "
                                + clazz.getName());
            }

        return getRequiresAnyOfTheAuthoritiesAuthorizer(clazz, authorities);
    }

    private Authorizer getRequiresAnyOfTheAuthoritiesAuthorizer(Class<? extends Command<?>> clazz,
                                                                String... authorities) {
        try {
            /*
              We implement the Flyweight pattern here: if multiple commands are annotated
              with Access.RequiresAuthority or Access.RequiresAnyOfTheAuthorities specifying
              the same set of authorities, we return the same Authorizer instance.
            */

            final ImmutableSet<String> authoritiesSet = ImmutableSet.copyOf(authorities);

            return authorizerCache.get(authoritiesSet);

        } catch (ExecutionException | UncheckedExecutionException e) {
            // should not happen
            throw new IllegalStateException("Failed to fetch command access metadata for " + clazz.getName(), e);
        }
    }

    private void checkAnnotations(Class<? extends Command<?>> clazz,
                                  Access.IsPublic isPublicAnnotation,
                                  Access.RequiresAuthentication requiresAuthenticationAnnotation,
                                  Access.RequiresAuthority requiresAuthorityAnnotation,
                                  Access.RequiresAnyOfTheAuthorities requiresAnyOfTheAuthoritiesAnnotation) {

        if (isPublicAnnotation == null
                && requiresAuthenticationAnnotation == null
                && requiresAuthorityAnnotation == null
                && requiresAnyOfTheAuthoritiesAnnotation == null) {
            throw new IllegalStateException("When security is used, a class must be either annotated with " +
                    "@IsPublic, @RequiresAuthentication, @RequiresAuthority or @RequiresAnyOfTheAuthorities");
        }

        if (isPublicAnnotation != null && requiresAuthenticationAnnotation != null) {
            throw new IllegalStateException(
                    "Both @IsPublic and @RequiresAuthentication are present on " + clazz);
        }

        if (isPublicAnnotation != null && requiresAuthorityAnnotation != null) {
            throw new IllegalStateException(
                    "Both @IsPublic and @RequiresAuthority are present on " + clazz);
        }

        if (isPublicAnnotation != null && requiresAnyOfTheAuthoritiesAnnotation != null) {
            throw new IllegalStateException(
                    "Both @IsPublic and @RequiresAnyOfTheAuthorities are present on " + clazz);
        }

        if (requiresAuthorityAnnotation != null && requiresAnyOfTheAuthoritiesAnnotation != null) {
            throw new IllegalStateException(
                    "Both @RequiresAuthority and @RequiresAnyOfTheAuthorities are present on " + clazz);
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
