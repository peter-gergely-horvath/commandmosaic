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

package org.commandmosaic.http.servlet.common;

import org.commandmosaic.api.server.*;
import org.commandmosaic.core.marshaller.UnmarshalException;
import org.commandmosaic.core.server.DefaultDispatchContext;
import org.commandmosaic.core.server.DefaultDispatchRequest;
import org.commandmosaic.core.server.DefaultDispatchResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;


public class DefaultHttpServletTransport implements HttpServletTransport {

    private final CommandDispatcherServer commandDispatcherServer;

    public DefaultHttpServletTransport(CommandDispatcherServer commandDispatcherServer) {
        Objects.requireNonNull(commandDispatcherServer, "argument commandDispatcherServer cannot be null");
        this.commandDispatcherServer = commandDispatcherServer;
    }

    @Override
    public void handleRequest(HttpServletRequest httpServletRequest,
                              HttpServletResponse httpServletResponse) throws ServletException, IOException {
        try {

            DispatchRequest request = new DefaultDispatchRequest(httpServletRequest.getInputStream());
            DispatchContext context = new DefaultDispatchContext();
            DispatchResponse response = new DefaultDispatchResponse(httpServletResponse.getOutputStream());

            context.addFailureListener(failure -> onFailure(httpServletResponse, failure));

            commandDispatcherServer.serviceRequest(request, response, context);

        } catch (RuntimeException e) {
            throw new ServletException(e);
        }
    }

    protected void onFailure(HttpServletResponse httpServletResponse, Throwable failure) {

        if (failure instanceof InvalidRequestException
                || failure instanceof UnmarshalException) {

            httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        } else {

            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        }
    }
}
