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
     * dispatching a command.</p>
     *
     * <p>
     * The dispatch request is read from the {@code InputStream} passed in
     * the {@code DispatchRequest}; it is then dispatched and the result
     * is written back to the {@code OutputStream} passed in the
     * {@code DispatchResponse}. </p>
     *
     * @param request the request object
     * @param response the response object
     * @param context provides context information and operations
     *
     * @throws IOException if IOException is raised while reading the request or writing the response
     *                      (e.g. the container closes the streams due to a network connection abort)
     */
    void serviceRequest(DispatchRequest request,
                        DispatchResponse response,
                        DispatchContext context) throws IOException;
}
