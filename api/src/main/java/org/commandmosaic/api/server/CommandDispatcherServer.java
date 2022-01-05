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

 
package org.commandmosaic.api.server;

import org.commandmosaic.api.CommandDispatcher;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <p>
 * A component, that wraps a {@link CommandDispatcher}.
 * It dispatches commands according to incoming requests read from the {@code InputStream}
 * and writes the return value back to the {@code OutputStream}.
 * </p>
 *
 * <p>
 * This interface does not specify anything regarding the message structure, format or encoding used.
 * Neither any assumption is made regarding the underlying transport (e.g. HTTP/REST call, MQ message etc.)
 * </p>
 *
 * <p>
 * Abstract implementations of this interface provided by the framework allow very quick addition
 * of support for new runtime environments.
 * </p>
 */
public interface CommandDispatcherServer {

    /**
     * <p>
     * Called to service an incoming message that contains a request for
     * dispatching a command. The dispatch request is read from the
     * {@code InputStream}, the command is dispatched and the result of the
     * command execution is written to the {@code OutputStream}.
     * </p>
     *
     * <p>
     * Implementors can provide their own error handling mechanism
     * and simply write a customized error message payload to the {@code OutputStream}
     * provided, however still should consider edge cases where this is unsuccessful
     * (E.g. a {@code IOException} is raised due to a disconnected socket connection)
     * and an Exception is propagated back to the container running the server.
     * </p>
     *
     * @param requestInputStream the {@code InputStream} to read the request from (never {@code null})
     * @param responseOutputStream the {@code OutputStream} to write the response to (never {@code null})
     *
     * @throws IOException if an I/O error occurs during unmarshalling of the request or the marshaling of the response,
     *                      including errors caused by a communications link failure. (E.g. the client disconnects
     *                      while request payload is being read or while the command is being executed and hence,
     *                      later on response payload cannot be written to the {@code OutputStream})
     *
     * @throws InvalidRequestException if the request was invalid (e.g does not contain the command name)
     */
    void serviceRequest(InputStream requestInputStream, OutputStream responseOutputStream)
            throws IOException, InvalidRequestException;
}
