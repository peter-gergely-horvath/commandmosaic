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


module org.commandmosaic.core {
    uses org.commandmosaic.core.marshaller.MarshallerFactory;
    requires transitive org.commandmosaic.api;

    requires com.google.gson;
    requires com.google.common;
    requires org.slf4j;

    exports org.commandmosaic.core;
    exports org.commandmosaic.core.conversion;
    exports org.commandmosaic.core.factory;
    exports org.commandmosaic.core.interceptor;
    exports org.commandmosaic.core.parameter.source;
    exports org.commandmosaic.core.server;
    exports org.commandmosaic.core.server.model;
    exports org.commandmosaic.core.parameter;
    exports org.commandmosaic.core.marshaller;
    exports org.commandmosaic.core.marshaller.model;
}