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


module org.commandmosaic.aws.lambda.springboot {
    requires transitive org.commandmosaic.aws.lambda;
    requires transitive org.commandmosaic.spring.container;
    requires transitive org.commandmosaic.springboot.autoconfigure;

    requires aws.lambda.java.core;
    requires spring.boot;
    requires spring.context;

    exports org.commandmosaic.aws.lambda.springboot;
}