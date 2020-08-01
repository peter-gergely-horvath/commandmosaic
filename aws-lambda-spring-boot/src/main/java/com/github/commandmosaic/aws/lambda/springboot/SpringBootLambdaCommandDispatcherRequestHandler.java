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

 
package com.github.commandmosaic.aws.lambda.springboot;


import com.github.commandmosaic.api.CommandDispatcher;
import com.github.commandmosaic.api.server.CommandDispatcherServer;
import com.github.commandmosaic.aws.lambda.LambdaCommandDispatcherRequestHandler;
import com.github.commandmosaic.spring.container.SpringContainerCommandDispatcherServer;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SuppressWarnings("unused") // API class, sub classed by user code
public abstract class SpringBootLambdaCommandDispatcherRequestHandler extends LambdaCommandDispatcherRequestHandler {

    protected SpringBootLambdaCommandDispatcherRequestHandler(
            Class<?> springBootApplicationClass) {
        this(getSpringBootDispatcherServer(springBootApplicationClass, null));
    }

    protected SpringBootLambdaCommandDispatcherRequestHandler(
            Class<?> springBootApplicationClass, String... profiles) {

        this(getSpringBootDispatcherServer(springBootApplicationClass, profiles));
    }

    private SpringBootLambdaCommandDispatcherRequestHandler(CommandDispatcherServer commandDispatcherServer) {
        super(commandDispatcherServer);
    }

    @SuppressWarnings("unused") // we use this to hide the super constructor from subclasses
    private SpringBootLambdaCommandDispatcherRequestHandler(CommandDispatcher commandDispatcher) {
        super(commandDispatcher);
    }

    private static CommandDispatcherServer getSpringBootDispatcherServer(
            Class<?> springBootApplicationClass, String[] profiles) {

        if (springBootApplicationClass == null) {
            throw new IllegalStateException("springBootApplicationClass must be specified");
        }

        SpringApplicationBuilder springApplication = new SpringApplicationBuilder(springBootApplicationClass);
        if (profiles != null && profiles.length > 0) {
            springApplication.profiles(profiles);
        }

        ConfigurableApplicationContext configurableApplicationContext = springApplication.run();

        return new SpringContainerCommandDispatcherServer(configurableApplicationContext);
    }
}
