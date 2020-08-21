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

 
package org.commandmosaic.plain.sample;

import org.commandmosaic.api.Command;
import org.commandmosaic.api.CommandContext;
import org.commandmosaic.api.CommandDispatcher;
import org.commandmosaic.api.Parameter;
import org.commandmosaic.api.configuration.CommandDispatcherConfiguration;
import org.commandmosaic.api.factory.CommandDispatcherFactory;
import org.commandmosaic.plain.PlainCommandDispatcherFactory;

public class SampleApplication {

    public static void main(String[] args) {

        CommandDispatcherConfiguration configuration = CommandDispatcherConfiguration.builder()
                .rootPackageFromClass(SampleApplication.class)
                .build();

        CommandDispatcherFactory factory = PlainCommandDispatcherFactory.getInstance();
        CommandDispatcher commandDispatcher = factory.getCommandDispatcher(configuration);

        GreetingCommand greetingCommand = new GreetingCommand("John Smith");
        String result = commandDispatcher.dispatchCommand(greetingCommand, null);

        System.out.println(result);
    }

    public static class GreetingCommand implements Command<String> {

        @Parameter
        private String name;

        public GreetingCommand() {
            // no argument constructor is required for the framework
        }

        public GreetingCommand(String name) {
            this.name = name;
        }

        @Override
        public String execute(CommandContext context) {
            return "Hello " + name;
        }
    }
}
