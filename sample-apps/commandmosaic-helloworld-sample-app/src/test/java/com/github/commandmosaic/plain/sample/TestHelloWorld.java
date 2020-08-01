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
import com.github.commandmosaic.api.CommandDispatcher;
import com.github.commandmosaic.api.configuration.CommandDispatcherConfiguration;
import com.github.commandmosaic.api.factory.CommandDispatcherFactory;
import com.github.commandmosaic.plain.PlainCommandDispatcherFactory;
import com.github.commandmosaic.plain.sample.SampleApplication;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

public class TestHelloWorld {

    private CommandDispatcher commandDispatcher;

    @Before
    public void beforeTest() {
        CommandDispatcherConfiguration configuration = CommandDispatcherConfiguration.builder()
                .rootPackageFromClass(SampleApplication.class)
                .build();

        CommandDispatcherFactory commandDispatcherFactory = PlainCommandDispatcherFactory.getInstance();
        commandDispatcher = commandDispatcherFactory.getCommandDispatcher(configuration);
    }

    @Test
    public void testDispatchingByPrototype() {

        Command<String> prototype = new SampleApplication.GreetingCommand("John Smith");

        String message = commandDispatcher.dispatchCommand(prototype, null);
        Assert.assertEquals("Hello John Smith", message);
    }

}
