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

 

package com.github.commandmosaic;

import com.github.commandmosaic.api.MissingParameterException;
import com.github.commandmosaic.api.ParameterInjectionException;
import com.github.commandmosaic.core.conversion.DefaultTypeConversionService;
import com.github.commandmosaic.core.parameter.ParameterInjector;
import com.github.commandmosaic.core.parameter.source.ParameterSources;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ParameterInjectorTest {

    private ParameterInjector parameterInjector;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void beforeTest() {
        parameterInjector = new ParameterInjector(new DefaultTypeConversionService());
    }

    @Test
    public void testTypeConversion() {

        HashMap<String, Object> parameters = new HashMap<>();

        int value = 42;
        parameters.put("value", value);

        NumberToStringCommand numberToStringCommand = new NumberToStringCommand();

        parameterInjector.processInjection(numberToStringCommand, ParameterSources.mapParameterSource(parameters));

        Assert.assertNotNull(numberToStringCommand.value);
        Assert.assertEquals(42L, (long) numberToStringCommand.value);

    }


    @Test
    public void testFailingTypeConversion() {

        expectedEx.expect(ParameterInjectionException.class);
        expectedEx.expectMessage("com.github.commandmosaic.NumberToStringCommand.value");

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("value", new Timestamp(new Date().getTime()));

        NumberToStringCommand numberToStringCommand = new NumberToStringCommand();

        parameterInjector.processInjection(numberToStringCommand, ParameterSources.mapParameterSource(parameters));

        Assert.fail("Should have thrown an exception");
    }

    @Test
    public void testMapInjection() {

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("name", "John Smith");
        parameters.put("message", "Hello");

        GreetingCommand greetingCommand = new GreetingCommand();
        Assert.assertNull(greetingCommand.name);
        Assert.assertNull(greetingCommand.getMessage());

        parameterInjector.processInjection(greetingCommand, ParameterSources.mapParameterSource(parameters));

        Assert.assertNotNull(greetingCommand.name);
        Assert.assertEquals( "John Smith", greetingCommand.name);

        String message = greetingCommand.getMessage();
        Assert.assertNotNull(message);
        Assert.assertEquals( "Hello", message);
    }

    @Test
    public void testMapInjectionWithNullParameters() {

        // We expect the Exception to be of a specific type, and contain the class name
        expectedEx.expect(MissingParameterException.class);
        expectedEx.expectMessage(GreetingCommand.class.getName());

        GreetingCommand greetingCommand = new GreetingCommand();
        parameterInjector.processInjection(greetingCommand, ParameterSources.mapParameterSource(null));
    }

    @Test
    public void testMapInjectionWithMissingFieldParameter() {

        // We expect the Exception to be of a specific type, and contain the class name
        expectedEx.expect(MissingParameterException.class);
        expectedEx.expectMessage(GreetingCommand.class.getName());

        Map<String, Object> parameters = Collections.singletonMap("name", "John Smith");

        GreetingCommand greetingCommand = new GreetingCommand();
        parameterInjector.processInjection(greetingCommand, ParameterSources.mapParameterSource(parameters));
    }

    @Test
    public void testMapInjectionWithMissingMethodParameter() {

        // We expect the Exception to be of a specific type, and contain the class name
        expectedEx.expect(MissingParameterException.class);
        expectedEx.expectMessage(GreetingCommand.class.getName());

        Map<String, Object> parameters = Collections.singletonMap("message", "Hello");

        GreetingCommand greetingCommand = new GreetingCommand();
        parameterInjector.processInjection(greetingCommand, ParameterSources.mapParameterSource(parameters));
    }

    @Test
    public void testPrototypeInjection() {

        GreetingCommand prototype = new GreetingCommand();
        prototype.name = "John Smith";
        prototype.setMessage("Hello");

        GreetingCommand greetingCommand = new GreetingCommand();
        Assert.assertNull(greetingCommand.name);
        Assert.assertNull(greetingCommand.getMessage());

        parameterInjector.processInjection(greetingCommand, ParameterSources.prototypeParameterSource(prototype));

        Assert.assertNotNull(greetingCommand.name);
        Assert.assertEquals( "John Smith", greetingCommand.name);

        String message = greetingCommand.getMessage();
        Assert.assertNotNull(message);
        Assert.assertEquals( "Hello", message);
    }
}
