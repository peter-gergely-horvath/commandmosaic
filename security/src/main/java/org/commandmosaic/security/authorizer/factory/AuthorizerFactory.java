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

import org.commandmosaic.api.Command;
import org.commandmosaic.security.authorizer.Authorizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.ServiceLoader;

public abstract class AuthorizerFactory {

    private static final Logger log = LoggerFactory.getLogger(AuthorizerFactory.class);

    public static AuthorizerFactory getInstance() {
        log.debug("Constructing new instance of AuthorizerFactory");

        log.trace("Performing ServiceLoader load for: {}", AuthorizerFactory.class);
        ServiceLoader<AuthorizerFactory> serviceLoader = ServiceLoader
                .load(AuthorizerFactory.class);

        log.trace("Discovering available AuthorizerFactory types");
        Iterator<AuthorizerFactory> iterator = serviceLoader.iterator();

        AuthorizerFactory factory;
        if (!iterator.hasNext()) {
            log.debug("No custom factory is discovered by ServiceLoader, falling back to default");
            factory = new DefaultAuthorizerFactory();
        } else {
            AuthorizerFactory singleExpectedFactory = iterator.next();
            if (log.isTraceEnabled()) {
                log.trace("Factory discovered: {}", singleExpectedFactory);
            }

            if (iterator.hasNext()) {
                AuthorizerFactory unexpectedAnotherFactoryFound = iterator.next();
                log.warn("Unexpected additional factory discovered: {}", unexpectedAnotherFactoryFound);

                throw new IllegalStateException(
                        "Multiple implementations found (this is caused by misconfigured dependencies): "
                                + singleExpectedFactory.getClass() + ", " + unexpectedAnotherFactoryFound.getClass());
            }

            factory = singleExpectedFactory;
        }


        if(log.isDebugEnabled()) {
            log.debug("Factory used: {}", factory.getClass());
        }

        return factory;
    }

    public abstract Authorizer getAuthorizer(Class<? extends Command<?>> commandClass);
}
