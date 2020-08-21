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

 
package org.commandmosaic.aws.lambda.springboot.sample;

import org.junit.Ignore;

@Ignore
public class TestSpringBootApplication {

    /*
    private final String expectedGreetingMessage  = "Hello John Smith, welcome to Lambda-Spring Boot world from "
                    + GreetCommand.class.getName();


    private static class TestCommandDispatcherRequestHandler extends SpringBootLambdaCommandDispatcherRequestHandler {

        protected TestCommandDispatcherRequestHandler(StandaloneSpringBootCommandDispatcherServerConfiguration configuration) {
            super(configuration);
        }
    }


    @Test
    public void testRequestHandlerSetup() {

        SimpleSpringBootContainerConfiguration simpleConfiguration = new SimpleSpringBootContainerConfiguration();
        simpleConfiguration.setSpringBootApplicationClass(SampleApplication.class.getName());
        simpleConfiguration.setInterceptors(Arrays.asList(CognitoAuthInterceptor.class));

        SpringBootLambdaCommandDispatcherRequestHandler requestHandler = new TestCommandDispatcherRequestHandler(simpleConfiguration);

        Map<String, Object> arguments = Collections.singletonMap("name", "John Smith");

        CommandDispatchRequest request = new CommandDispatchRequest();
        request.setCommandName("GreetCommand");
        request.setParameters(arguments);


        CommandDispatchResponse response = requestHandler.handleRequest(request, null);

        Assert.assertNotNull(response);

        Object resultObject = response.getResult();

        Assert.assertTrue(resultObject instanceof String);
        Assert.assertEquals(expectedGreetingMessage, resultObject);
    }*/

}
