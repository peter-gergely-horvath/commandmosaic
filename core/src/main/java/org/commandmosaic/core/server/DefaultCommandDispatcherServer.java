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


package org.commandmosaic.core.server;

import org.commandmosaic.api.CommandContext;
import org.commandmosaic.api.CommandDispatcher;
import org.commandmosaic.api.server.*;
import org.commandmosaic.core.marshaller.Marshaller;
import org.commandmosaic.core.marshaller.MarshallerFactory;
import org.commandmosaic.core.server.context.DefaultCommandContext;
import org.commandmosaic.core.server.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DefaultCommandDispatcherServer implements CommandDispatcherServer {

    private final Logger logger = LoggerFactory.getLogger(DefaultCommandDispatcherServer.class);

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private final CommandDispatcher commandDispatcher;
    private final Marshaller marshaller;

    public DefaultCommandDispatcherServer(CommandDispatcher commandDispatcher) {
        this(commandDispatcher, MarshallerFactory.getInstance().getMarshaller());
    }

    protected DefaultCommandDispatcherServer(CommandDispatcher commandDispatcher, Marshaller marshaller) {
        Objects.requireNonNull(commandDispatcher, "commandDispatcher cannot be null");
        Objects.requireNonNull(marshaller, "marshaller cannot be null");

        this.commandDispatcher = commandDispatcher;
        this.marshaller = marshaller;
    }


    @Override
    public void serviceRequest(DispatchRequest dispatchRequest, DispatchResponse dispatchResponse)
            throws IOException, CommandException {

        Request request = unmarshalRequest(dispatchRequest.getInputStream());
        logger.trace("Servicing request {}", request);

        Object requestId = request.getId();

        try {
            String requestProtocol = request.getProtocol();
            String expectedProtocolVersion = ProtocolConstants.PROTOCOL_VERSION;
            if (!expectedProtocolVersion.equals(requestProtocol)) {
                logger.warn("Invalid protocol version: {}; dispatching rejected", request);
                throw new InvalidRequestException("Request protocol version is invalid: '"
                        + requestProtocol + "'; expected: " + expectedProtocolVersion);
            }

            String commandName = request.getCommand();
            if (commandName == null || commandName.trim().isEmpty()) {
                logger.warn("Command is not specified in request, dispatching rejected: {}", request);
                throw new InvalidRequestException("Command is not specified");
            }

            Map<String, Object> parameters = request.getParameters();
            logger.debug("Parameters: {}", parameters);

            Map<String, Object> auth = request.getAuth();
            logger.trace("Auth: {}", auth);

            CommandContext commandContext = new DefaultCommandContext(auth);


            Object result = commandDispatcher.dispatchCommand(commandName, parameters, commandContext);

            ResultResponse response = new ResultResponse(requestId, result);

            marshalResponse(dispatchResponse.getOutputStream(), response);
        } catch (CommandException e) {

            dispatchResponse.notifyErrorListeners(e);

            logger.warn("Command failed with exception", e);

            marshalFailure(dispatchResponse.getErrorStream(), requestId, e);

            /*
            Transports likely want to know if this was success or failure:
            propagate CommandException to them after we have written the
            error response payload
            */

            throw e;
        }
    }

    protected Request unmarshalRequest(InputStream requestInputStream) throws IOException {
        return marshaller.unmarshal(requestInputStream, Request.class);
    }


    protected void marshalResponse(OutputStream responseOutputStream, Object response) throws IOException {
        marshaller.marshal(responseOutputStream, response);
    }

    protected void marshalFailure(
            OutputStream responseOutputStream, Object id, Throwable throwable) throws IOException {

        Objects.requireNonNull(responseOutputStream, "responseOutputStream cannot be null");
        Objects.requireNonNull(throwable, "throwable cannot be null");

        List<String> stackTrace = convertThrowableStackTraceToString(throwable);

        ErrorModel model = new ErrorModel();
        model.setErrorMessage(throwable.getMessage());
        model.setErrorType(throwable.getClass().getCanonicalName());
        model.setStackTrace(stackTrace);

        ErrorResponse errorResponse = new ErrorResponse(id, model);

        marshaller.marshal(responseOutputStream, errorResponse);
    }

    private List<String> convertThrowableStackTraceToString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            throwable.printStackTrace(pw);
        }

        return Arrays.asList(sw.toString().split(LINE_SEPARATOR));
    }
}
