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

 
package com.github.commandmosaic.plain.sample;

import com.github.commandmosaic.api.Command;
import com.github.commandmosaic.api.CommandContext;
import com.github.commandmosaic.api.CommandDispatcher;
import com.github.commandmosaic.api.Parameter;
import com.github.commandmosaic.api.configuration.CommandDispatcherConfiguration;
import com.github.commandmosaic.api.factory.CommandDispatcherFactory;
import com.github.commandmosaic.plain.PlainCommandDispatcherFactory;

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
