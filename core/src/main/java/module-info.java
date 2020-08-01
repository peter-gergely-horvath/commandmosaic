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


module com.github.commandmosaic.core {
    requires transitive com.github.commandmosaic.api;

    requires com.google.gson;
    requires transitive com.google.common;
    requires transitive org.slf4j;

    exports com.github.commandmosaic.core;
    exports com.github.commandmosaic.core.conversion;
    exports com.github.commandmosaic.core.factory;
    exports com.github.commandmosaic.core.interceptor;
    exports com.github.commandmosaic.core.parameter.source;
    exports com.github.commandmosaic.core.server;
    exports com.github.commandmosaic.core.server.model;
    exports com.github.commandmosaic.core.parameter;
}