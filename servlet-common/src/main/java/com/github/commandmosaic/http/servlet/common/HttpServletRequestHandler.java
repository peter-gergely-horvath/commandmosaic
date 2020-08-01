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

 
package com.github.commandmosaic.http.servlet.common;

import com.github.commandmosaic.api.server.CommandDispatcherServer;
import com.github.commandmosaic.api.server.CommandException;
import com.github.commandmosaic.api.server.InvalidRequestException;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class HttpServletRequestHandler {

    private final CommandDispatcherServer commandDispatcherServer;

    public HttpServletRequestHandler(CommandDispatcherServer commandDispatcherServer) {
        this.commandDispatcherServer = commandDispatcherServer;
    }

    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            ServletInputStream inputStream = request.getInputStream();
            ServletOutputStream outputStream = response.getOutputStream();

            commandDispatcherServer.serviceRequest(inputStream, outputStream);
        }
        catch (InvalidRequestException e) {
            response.reset();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            e.printStackTrace(response.getWriter());
        }
        catch (CommandException e) {
            response.reset();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace(response.getWriter());
        }
        catch (RuntimeException e) {
            throw new ServletException(e);
        }
    }
}
