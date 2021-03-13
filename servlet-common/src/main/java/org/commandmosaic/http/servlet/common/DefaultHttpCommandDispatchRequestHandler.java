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

package org.commandmosaic.http.servlet.common;

import org.commandmosaic.api.server.CommandDispatcherServer;
import org.commandmosaic.api.server.CommandException;
import org.commandmosaic.api.server.InvalidRequestException;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class DefaultHttpCommandDispatchRequestHandler implements HttpCommandDispatchRequestHandler {

    private final CommandDispatcherServer commandDispatcherServer;

    public DefaultHttpCommandDispatchRequestHandler(CommandDispatcherServer commandDispatcherServer) {
        this.commandDispatcherServer = commandDispatcherServer;
    }

    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            dispatchRequest(request, response);
        }
        catch (InvalidRequestException e) {
            sendStatusCodeResponse(response, e, HttpServletResponse.SC_BAD_REQUEST);
        }
        catch (CommandException e) {
            sendStatusCodeResponse(response, e, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        catch (RuntimeException e) {
            throw new ServletException(e);
        }
    }

    protected void dispatchRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletInputStream inputStream = request.getInputStream();
        ServletOutputStream outputStream = response.getOutputStream();

        commandDispatcherServer.serviceRequest(inputStream, outputStream);
    }

    protected void sendStatusCodeResponse(HttpServletResponse response, Throwable t, int statusCode)
            throws IOException {

        response.reset();
        response.setStatus(statusCode);
        t.printStackTrace(response.getWriter());
    }
}
