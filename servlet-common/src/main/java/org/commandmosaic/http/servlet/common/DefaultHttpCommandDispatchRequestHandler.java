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
import org.commandmosaic.core.server.DefaultDispatchRequest;
import org.commandmosaic.core.server.DefaultDispatchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Supplier;


public class DefaultHttpCommandDispatchRequestHandler implements HttpCommandDispatchRequestHandler {

    private final Logger log = LoggerFactory.getLogger(DefaultHttpCommandDispatchRequestHandler.class);

    private final CommandDispatcherServer commandDispatcherServer;

    public DefaultHttpCommandDispatchRequestHandler(CommandDispatcherServer commandDispatcherServer) {
        this.commandDispatcherServer = commandDispatcherServer;
    }

    @Override
    public void handleRequest(HttpServletRequest httpServletRequest,
                              HttpServletResponse httpServletResponse) throws ServletException, IOException {
        try {

            DispatchRequest request = new DefaultDispatchRequest(httpServletRequest.getInputStream());
            DispatchResponse response = new DefaultDispatchResponse(getAsSupplier(httpServletResponse));

            response.addListener(failure -> onFailure(httpServletResponse, failure));

            commandDispatcherServer.serviceRequest(request, response);

        } catch (CommandException e) {
            /*
            The server has written the expected error response body already,
            the listener has set the response status code already in
            onFailure(HttpServletResponse, Throwable)

            Since the response payload has been written already,
            we CANNOT change the status code here: we only log here.
             */
            log.debug("Request failed", e);

        } catch (RuntimeException e) {
            throw new ServletException(e);
        }
    }

    protected void onFailure(HttpServletResponse httpServletResponse, Throwable failure) {

        if (failure instanceof InvalidRequestException) {
            httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        } else {
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        }
    }

    private Supplier<OutputStream> getAsSupplier(HttpServletResponse httpServletResponse) {
        return () -> {
            try {
                return httpServletResponse.getOutputStream();
            } catch (IOException e) {
                throw new RuntimeException("Could not get OutputStream from HttpServletResponse", e);
            }
        };
    }

}
