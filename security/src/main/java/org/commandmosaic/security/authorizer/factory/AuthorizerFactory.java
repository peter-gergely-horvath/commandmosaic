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

import org.commandmosaic.api.Command;
import org.commandmosaic.core.factory.support.ServiceLoaderSupport;
import org.commandmosaic.security.authorizer.Authorizer;

public abstract class AuthorizerFactory {

    private static final ServiceLoaderSupport<AuthorizerFactory> serviceLoaderSupport =
            new ServiceLoaderSupport<>(AuthorizerFactory.class);

    public static AuthorizerFactory getInstance() {
        return serviceLoaderSupport.loadSingleServiceOrGetDefault(DefaultAuthorizerFactory::new);
    }

    public abstract Authorizer getAuthorizer(Class<? extends Command<?>> commandClass);
}
