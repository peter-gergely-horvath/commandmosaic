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

 
package org.commandmosaic.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import org.commandmosaic.api.CommandDispatcher;
import org.commandmosaic.api.server.CommandDispatcherServer;
import org.commandmosaic.api.server.CommandException;
import org.commandmosaic.core.server.DefaultCommandDispatcherServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class LambdaCommandDispatcherRequestHandler implements RequestStreamHandler {

    protected final CommandDispatcherServer commandDispatcherServer;

    protected LambdaCommandDispatcherRequestHandler(CommandDispatcher commandDispatcher) {
        this(new DefaultCommandDispatcherServer(commandDispatcher));
    }

    protected LambdaCommandDispatcherRequestHandler(CommandDispatcherServer commandDispatcherServer) {
        this.commandDispatcherServer = commandDispatcherServer;
    }

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        try {
            commandDispatcherServer.serviceRequest(input, output);
        }
        catch (CommandException e) {
            throw new RuntimeException(String.format("%s: %s", e.getClass().getSimpleName(), e.getMessage()), e);
        }
        catch (IOException | RuntimeException ex) {
            throw new RuntimeException("Error: Failed to dispatch command", ex);
        }
    }
}
