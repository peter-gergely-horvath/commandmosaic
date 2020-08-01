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

 
package com.github.commandmosaic.spring.web;

import com.github.commandmosaic.api.server.CommandDispatcherServer;
import com.github.commandmosaic.http.servlet.common.HttpServletRequestHandler;
import org.springframework.web.HttpRequestHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CommandDispatcherRequestHandler implements HttpRequestHandler {

    private final HttpServletRequestHandler httpServletRequestHandler;

    public CommandDispatcherRequestHandler(CommandDispatcherServer commandDispatcherServer) {
        this.httpServletRequestHandler = new HttpServletRequestHandler(commandDispatcherServer);
    }

    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        httpServletRequestHandler.handleRequest(request, response);
    }
}
