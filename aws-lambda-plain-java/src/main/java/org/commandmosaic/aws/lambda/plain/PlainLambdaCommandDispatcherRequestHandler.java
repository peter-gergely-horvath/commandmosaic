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

 
package org.commandmosaic.aws.lambda.plain;

import org.commandmosaic.api.CommandDispatcher;
import org.commandmosaic.api.configuration.CommandDispatcherConfiguration;
import org.commandmosaic.api.server.CommandDispatcherServer;
import org.commandmosaic.api.interceptor.CommandInterceptor;
import org.commandmosaic.aws.lambda.LambdaCommandDispatcherRequestHandler;
import org.commandmosaic.plain.PlainCommandDispatcherFactory;

import java.util.List;

@SuppressWarnings("unused") // API class, sub classed by user code
public class PlainLambdaCommandDispatcherRequestHandler extends LambdaCommandDispatcherRequestHandler {

    protected PlainLambdaCommandDispatcherRequestHandler(String commandRootPackage) {
        this(commandRootPackage, null);
    }

    protected PlainLambdaCommandDispatcherRequestHandler(String commandRootPackage,
                                                         List<Class<? extends CommandInterceptor>> interceptors) {

        this(buildCommandDispatcher(commandRootPackage, interceptors));
    }

    private PlainLambdaCommandDispatcherRequestHandler(CommandDispatcher commandDispatcher) {
        super(commandDispatcher);
    }

    private PlainLambdaCommandDispatcherRequestHandler(CommandDispatcherServer commandDispatcherServer) {
        super(commandDispatcherServer);
    }

    private static CommandDispatcher buildCommandDispatcher(String commandRootPackage,
                                                            List<Class<? extends CommandInterceptor>> interceptors) {

        CommandDispatcherConfiguration.Builder configBuilder = CommandDispatcherConfiguration.builder()
                .rootPackage(commandRootPackage);

        if (interceptors != null) {
            interceptors.forEach(configBuilder::interceptor);
        }

        CommandDispatcherConfiguration configuration = configBuilder.build();

        return PlainCommandDispatcherFactory.getInstance().getCommandDispatcher(configuration);
    }
}
