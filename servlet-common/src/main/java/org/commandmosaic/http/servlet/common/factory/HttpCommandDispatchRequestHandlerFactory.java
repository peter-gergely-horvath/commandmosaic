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

package org.commandmosaic.http.servlet.common.factory;

import org.commandmosaic.api.server.CommandDispatcherServer;
import org.commandmosaic.http.servlet.common.HttpCommandDispatchRequestHandler;

import java.util.Iterator;
import java.util.ServiceLoader;

public abstract class HttpCommandDispatchRequestHandlerFactory {

    protected HttpCommandDispatchRequestHandlerFactory() {

    }

    public static HttpCommandDispatchRequestHandlerFactory newInstance() {

        ServiceLoader<HttpCommandDispatchRequestHandlerFactory> serviceLoader = ServiceLoader
                .load(HttpCommandDispatchRequestHandlerFactory.class);

        Iterator<HttpCommandDispatchRequestHandlerFactory> iterator = serviceLoader.iterator();

        if (iterator.hasNext()) {
            HttpCommandDispatchRequestHandlerFactory singleExpectedFactory = iterator.next();

            if (iterator.hasNext()) {
                HttpCommandDispatchRequestHandlerFactory unexpectedAnotherFactoryFound = iterator.next();

                throw new IllegalStateException(
                        "Multiple implementations found (this is caused by misconfigured dependencies): "
                                + singleExpectedFactory.getClass() + ", " + unexpectedAnotherFactoryFound.getClass());
            }

            return singleExpectedFactory;
        }

        return new DefaultHttpCommandDispatchRequestHandlerFactory();
    }

    public abstract HttpCommandDispatchRequestHandler getHttpCommandDispatchRequestHandler(
            CommandDispatcherServer commandDispatcherServer);
}
