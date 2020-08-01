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
import com.github.commandmosaic.core.server.model.CommandDispatchRequest;
import com.github.commandmosaic.core.server.model.CommandDispatchResponse;
import com.google.gson.Gson;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * The default {@code CommandDispatcherServer} implementation, which uses JSON format
 * for the {@link CommandDispatchRequest} and {@link CommandDispatchResponse}.
 */
public class DefaultCommandDispatcherServer extends AbstractCommandDispatcherServer {

    /**
     * Thread-safe according to the the JavaDoc of GSON:
     * "Gson instances are Thread-safe so you can reuse them freely across multiple threads."
     */
    private static final Gson gson = new Gson();

    public DefaultCommandDispatcherServer(CommandDispatcher commandDispatcher) {
        super(commandDispatcher);
    }

    @Override
    protected CommandDispatchRequest unmarshalRequest(InputStream requestInputStream) throws IOException {

        try (InputStreamReader inputStreamReader = new InputStreamReader(requestInputStream, StandardCharsets.UTF_8)) {
            return gson.fromJson(inputStreamReader, CommandDispatchRequest.class);
        }
        catch (IOException e) {
            throw new IOException("Failed to unmarshal request", e);
        }
    }

    @Override
    protected void marshalResponse(OutputStream responseOutputStream, Object response) throws IOException {

        String jsonString = gson.toJson(response);

        try (OutputStreamWriter writer = new OutputStreamWriter(responseOutputStream, StandardCharsets.UTF_8)) {
            writer.write(jsonString);
        }
        catch (IOException e) {
            throw new IOException("Failed to marshal response", e);
        }
    }

}
