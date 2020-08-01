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

 
package com.github.commandmosaic.core.server;

import com.github.commandmosaic.api.CommandDispatcher;
import com.github.commandmosaic.api.CommandContext;
import com.github.commandmosaic.api.server.CommandException;
import com.github.commandmosaic.api.server.CommandExecutionException;
import com.github.commandmosaic.api.server.CommandDispatcherServer;
import com.github.commandmosaic.api.server.InvalidRequestException;
import com.github.commandmosaic.core.server.model.CommandDispatchRequest;
import com.github.commandmosaic.core.server.model.CommandDispatchResponse;
import com.github.commandmosaic.core.server.model.DefaultCommandContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * An abstract base class for any {@code CommandDispatcherServer} implementation,
 * which unmarshals a {@link CommandDispatchRequest} from an arbitrary format
 * and responds with {@link CommandDispatchResponse} marshalled to an arbitrary format.
 */
public abstract class AbstractCommandDispatcherServer implements CommandDispatcherServer {

    private final CommandDispatcher commandDispatcher;

    protected AbstractCommandDispatcherServer(CommandDispatcher commandDispatcher) {
        this.commandDispatcher = commandDispatcher;
    }

    @Override
    public void serviceRequest(InputStream requestInputStream, OutputStream responseOutputStream)
            throws IOException, InvalidRequestException, CommandExecutionException {

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

        Object result;
        try {
            result = commandDispatcher.dispatchCommand(commandName, parameters, commandContext);
        }
        catch (CommandException ex) {
            throw ex;
        }
        catch (RuntimeException ex) {
            throw new CommandExecutionException("Execution of command failed", ex);
        }

        Object responseModel = buildResponseModel(result);

        marshalResponse(responseOutputStream, responseModel);
    }

    protected abstract CommandDispatchRequest unmarshalRequest(InputStream requestInputStream) throws IOException;

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

    protected abstract void marshalResponse(OutputStream responseOutputStream, Object response) throws IOException;
}
