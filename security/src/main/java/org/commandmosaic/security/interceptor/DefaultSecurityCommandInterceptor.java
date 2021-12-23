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
import org.commandmosaic.api.executor.CommandExecutor;
import org.commandmosaic.api.executor.ParameterSource;
import org.commandmosaic.security.AccessDeniedException;
import org.commandmosaic.security.AuthenticationException;
import org.commandmosaic.security.authenticator.Authenticator;
import org.commandmosaic.security.authenticator.AuthenticatorChain;
import org.commandmosaic.security.authorizer.Authorizer;
import org.commandmosaic.security.authorizer.factory.AuthorizerFactory;
import org.commandmosaic.security.core.Identity;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("unused") // API class
public class DefaultSecurityCommandInterceptor implements SecurityCommandInterceptor {

    private final Authenticator authenticator;
    private final AuthorizerFactory authorizerFactory;

    // CPD-OFF
    private final LoadingCache<Class<? extends Command<?>>, Authorizer> authorizerCache = CacheBuilder.newBuilder()
            .softValues().build(new CacheLoader<Class<? extends Command<?>>, Authorizer>() {
                @Override
                public Authorizer load(@Nonnull Class<? extends Command<?>> clazz) {
                    return getAuthorizer(clazz);
                }
            });
    // CPD-OFF

    protected DefaultSecurityCommandInterceptor(Authenticator firstAuthenticator,
                                                Authenticator... additionalAuthenticators) {
        this(AuthorizerFactory.getInstance(), firstAuthenticator, additionalAuthenticators);
    }

    protected DefaultSecurityCommandInterceptor(AuthorizerFactory authorizerFactory,
                                                Authenticator firstAuthenticator,
                                                Authenticator... additionalAuthenticators) {

        this.authenticator = getAuthenticator(firstAuthenticator, additionalAuthenticators);
        this.authorizerFactory = Objects.requireNonNull(authorizerFactory,
                "argument authorizerFactory cannot be null");
    }

    private Authenticator getAuthenticator(Authenticator firstAuthenticator,
                                           Authenticator... additionalAuthenticators) {
        if (firstAuthenticator == null) {
            throw new IllegalStateException("At least one authentication must be specified");
        }

        Authenticator authenticator;
        if (additionalAuthenticators == null || additionalAuthenticators.length == 0) {
            authenticator = firstAuthenticator;
        } else {
            final LinkedList<Authenticator> list = new LinkedList<>();
            list.add(firstAuthenticator);
            list.addAll(Arrays.asList(additionalAuthenticators));

            authenticator = new AuthenticatorChain(list);
        }
        return authenticator;
    }

    private Authorizer getAuthorizer(Class<? extends Command<?>> commandClass) {
        return this.authorizerFactory.getAuthorizer(commandClass);
    }

    @Override
    public final <R, C extends Command<R>> R intercept(Class<C> commandClass, ParameterSource parameters,
                                                       CommandContext context, CommandExecutor next) {

        try {
            final Authorizer authorizer = authorizerCache.get(commandClass);

            if(authorizer.isAuthenticationRequired()) {

                Identity identity = this.authenticator.authenticate(context);
                if (identity == null) {
                    throw new AccessDeniedException("Access Denied: authentication required");
                }

                authorizer.checkAuthorization(commandClass, identity, parameters, context);

                context = new SecurityAwareCommandContext(context, identity);
            }

        } catch (AuthenticationException e) {
            throw new AccessDeniedException(
                    "Access Denied: authentication failure", e);

        } catch (ExecutionException | UncheckedExecutionException e) {
            Throwable cause = e.getCause();

            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;

            } else if (cause instanceof Error) {
                throw (Error) cause;

            } else {
                throw new IllegalStateException(cause.getMessage(), cause);
            }
        }

        return next.execute(commandClass, parameters, context);
    }
}
