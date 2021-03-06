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

 
package org.commandmosaic.core.server;

import org.commandmosaic.api.CommandContext;
import org.commandmosaic.api.CommandDispatcher;
import org.commandmosaic.api.server.CommandDispatcherServer;
import org.commandmosaic.api.server.InvalidRequestException;
import org.commandmosaic.core.marshaller.Marshaller;
import org.commandmosaic.core.marshaller.MarshallerFactory;
import org.commandmosaic.core.server.model.CommandDispatchRequest;
import org.commandmosaic.core.server.model.CommandDispatchResponse;
import org.commandmosaic.core.server.model.DefaultCommandContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Objects;

public class DefaultCommandDispatcherServer implements CommandDispatcherServer {

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
    public void serviceRequest(InputStream requestInputStream, OutputStream responseOutputStream)
            throws IOException, InvalidRequestException {

        CommandDispatchRequest request = unmarshalRequest(requestInputStream);

        String requestProtocol = request.getProtocol();
        String expectedProtocolVersion = ProtocolConstants.PROTOCOL_VERSION;
        if (!expectedProtocolVersion.equals(requestProtocol)) {
            throw new InvalidRequestException("Request protocol version is invalid: '"
                    + requestProtocol +"'; expected: " + expectedProtocolVersion);
        }

        String commandName = request.getCommand();
        if (commandName == null || commandName.trim().isEmpty()) {
            throw new InvalidRequestException("Command is not specified");
        }

        Map<String, Object> parameters = request.getParameters();
        Map<String, Object> auth = request.getAuth();

        CommandContext commandContext = new DefaultCommandContext(auth);

        Object result = commandDispatcher.dispatchCommand(commandName, parameters, commandContext);

        Object responseModel = buildResponseModel(result);

        marshalResponse(responseOutputStream, responseModel);
    }

    protected CommandDispatchRequest unmarshalRequest(InputStream requestInputStream) throws IOException {
        return marshaller.unmarshal(requestInputStream, CommandDispatchRequest.class);
    }


    /**
     * Template method to build the object representation that will be
     * passed to {@link #marshalResponse(OutputStream, Object)} for marshaling back to the caller.
     *
     * @param result the result of the command execution
     *
     * @return the object, the representation of which will be sent back to the client
     */
    protected Object buildResponseModel(Object result) {
        return new CommandDispatchResponse(result);
    }


    protected void marshalResponse(OutputStream responseOutputStream, Object response) throws IOException {
        marshaller.marshal(responseOutputStream, response);
    }
}
