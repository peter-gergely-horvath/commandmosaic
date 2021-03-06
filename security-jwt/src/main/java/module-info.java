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


module org.commandmosaic.security.jwt {
    requires com.google.common;

    requires transitive org.commandmosaic.security;
    requires transitive org.commandmosaic.security.web;

    requires org.slf4j;
    requires jjwt.api;

    exports org.commandmosaic.security.jwt.core;
    exports org.commandmosaic.security.jwt.interceptor;

    opens org.commandmosaic.security.jwt.interceptor to org.commandmosaic.core;

    exports org.commandmosaic.security.jwt.config;
    exports org.commandmosaic.security.jwt.command;
}
