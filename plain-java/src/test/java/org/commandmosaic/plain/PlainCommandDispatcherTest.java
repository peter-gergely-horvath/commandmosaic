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

 
package org.commandmosaic.plain;

import org.commandmosaic.api.CommandDispatcher;
import org.commandmosaic.api.configuration.CommandDispatcherConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

public class PlainCommandDispatcherTest {

    private CommandDispatcher commandDispatcher;

    @Before
    public void beforeTest() {
        CommandDispatcherConfiguration configuration = CommandDispatcherConfiguration.builder()
                .rootPackageFromClass(PlainCommandDispatcherTest.class)
                .build();

        PlainCommandDispatcherFactory factory = PlainCommandDispatcherFactory.getInstance();

        commandDispatcher = factory.getCommandDispatcher(configuration);
    }

    @Test
    public void testClassDispatching() {

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("name", "John Smith");
        parameters.put("message", "Hello");

        String message =
                commandDispatcher.dispatchCommand(GreetingCommand.class, parameters, null);

        Assert.assertNotNull(message);
        Assert.assertEquals( "Hello John Smith", message);
    }

    @Test
    public void testClassNameDispatching() {

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("name", "John Smith");
        parameters.put("message", "Hello");

        String message =
                (String) commandDispatcher.dispatchCommand("GreetingCommand", parameters, null);

        Assert.assertNotNull(message);
        Assert.assertEquals( "Hello John Smith", message);
    }

    @Test
    public void testPrototypeDispatching() {

        GreetingCommand prototype = new GreetingCommand("Hello", "John Smith");

        String message = commandDispatcher.dispatchCommand(prototype, null);

        Assert.assertNotNull(message);
        Assert.assertEquals( "Hello John Smith", message);
    }


}
