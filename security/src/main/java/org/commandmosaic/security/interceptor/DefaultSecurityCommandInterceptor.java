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
import org.commandmosaic.security.authenticator.Authenticator;
import org.commandmosaic.security.authorizer.Authorizer;
import org.commandmosaic.security.authorizer.factory.AuthorizerFactory;
import org.commandmosaic.security.core.CallerIdentity;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("unused") // API class, sub classed by user code
public abstract class DefaultSecurityCommandInterceptor implements SecurityCommandInterceptor {

    private final Authenticator authenticator;
    private final AuthorizerFactory authorizerFactory;

    // CPD-OFF
    private final LoadingCache<Class<? extends Command<?>>, Authorizer> authorizerCache = CacheBuilder.newBuilder()
            .softValues().build(new CacheLoader<>() {
                @Override
                public Authorizer load(Class<? extends Command<?>> clazz) {
                    return getAuthorizer(clazz);
                }
            });
    // CPD-OFF

    protected DefaultSecurityCommandInterceptor(Authenticator authenticator) {
        this(authenticator, AuthorizerFactory.getInstance());
    }

    protected DefaultSecurityCommandInterceptor(Authenticator authenticator, AuthorizerFactory authorizerFactory) {
        Objects.requireNonNull(authenticator, "argument authenticator cannot be null");
        Objects.requireNonNull(authorizerFactory, "argument authorizerFactory cannot be null");

        this.authenticator = authenticator;
        this.authorizerFactory = authorizerFactory;
    }

    protected Authorizer getAuthorizer(Class<? extends Command<?>> commandClass) {
        return this.authorizerFactory.getAuthorizer(commandClass);
    }

    @Override
    public final <R, C extends Command<R>> R intercept(Class<C> commandClass, ParameterSource parameters,
                                                       CommandContext context, InterceptorChain next) {

        try {
            final Authorizer authorizer = authorizerCache.get(commandClass);

            if(authorizer.isAuthenticationRequired()) {

                CallerIdentity callerIdentity = this.authenticator.authenticate(context);
                if (callerIdentity == null) {
                    throw new AccessDeniedException("Access Denied: authentication required");
                }

                authorizer.checkAuthorization(commandClass, callerIdentity, parameters, context);

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
}
