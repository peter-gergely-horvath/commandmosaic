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

 
package org.commandmosaic.aws.lambda.springboot.sample;

import org.commandmosaic.aws.lambda.springboot.SpringBootLambdaCommandDispatcherRequestHandler;

/**
 * <p>
 * This class has to be configured as AWS Lambda RequestHandler.
 * The actual command dispatching logic is implemented in the
 * CommandMosaic-specific framework class, which we sub-class here:
 * we simply pass our {@code @SpringBootApplication}-annotated application
 * class to the {@code super} constructor, to the parent class,
 * which will internally start the Spring Boot application.
 * Nothing else has to be done here.
 * </p>
 */
@SuppressWarnings("unused") // Class is instantiated by AWS Lambda runtime based on configuration
public class SampleApplicationRequestHandler extends SpringBootLambdaCommandDispatcherRequestHandler {

    public SampleApplicationRequestHandler() {
        super(SampleApplication.class);
    }
}
