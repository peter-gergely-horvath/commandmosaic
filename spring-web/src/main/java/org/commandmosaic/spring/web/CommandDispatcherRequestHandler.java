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

 
package org.commandmosaic.spring.web;

import org.commandmosaic.api.server.CommandDispatcherServer;
import org.commandmosaic.http.servlet.common.HttpServletTransport;
import org.commandmosaic.http.servlet.common.factory.HttpServletTransportFactory;
import org.springframework.web.HttpRequestHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CommandDispatcherRequestHandler implements HttpRequestHandler {

    private final HttpServletTransport httpServletTransport;

    public CommandDispatcherRequestHandler(CommandDispatcherServer commandDispatcherServer) {
        HttpServletTransportFactory factory = HttpServletTransportFactory.getInstance();

        this.httpServletTransport = factory.getHttpServletTransport(commandDispatcherServer);
    }

    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        httpServletTransport.handleRequest(request, response);
    }
}
