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

package org.commandmosaic.aws.lambda.transport;

import org.commandmosaic.api.server.CommandDispatcherServer;
import org.commandmosaic.api.server.DispatchRequest;
import org.commandmosaic.api.server.DispatchResponse;
import org.commandmosaic.core.server.DefaultDispatchRequest;
import org.commandmosaic.core.server.DefaultDispatchResponse;
import org.commandmosaic.core.server.EmptyDispatchContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class LambdaTransport {

    private final CommandDispatcherServer commandDispatcherServer;

    public LambdaTransport(CommandDispatcherServer commandDispatcherServer) {
        this.commandDispatcherServer =
                Objects.requireNonNull(commandDispatcherServer, "argument commandDispatcherServer cannot be null");
    }

    public void handleRequest(InputStream input, OutputStream output) throws IOException {

        DispatchRequest request = new DefaultDispatchRequest(input);
        DispatchResponse response = new DefaultDispatchResponse(output);

        commandDispatcherServer.serviceRequest(request, response, EmptyDispatchContext.INSTANCE);
    }
}
