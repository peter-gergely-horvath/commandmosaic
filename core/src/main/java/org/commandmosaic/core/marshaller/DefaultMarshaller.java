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

package org.commandmosaic.core.marshaller;

import com.google.gson.Gson;
import org.commandmosaic.core.marshaller.model.FailureModel;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

final class DefaultMarshaller implements Marshaller {

    /**
     * Thread-safe according to the the JavaDoc of GSON:
     * "Gson instances are Thread-safe so you can reuse them freely across multiple threads."
     */
    private static final Gson gson = new Gson();

    @Override
    public <T> T unmarshal(InputStream requestInputStream, Class<T> type) throws IOException {
        Objects.requireNonNull(requestInputStream, "requestInputStream cannot be null");
        Objects.requireNonNull(type, "type cannot be null");

        try (InputStreamReader inputStreamReader = new InputStreamReader(requestInputStream, StandardCharsets.UTF_8)) {
            return gson.fromJson(inputStreamReader, type);
        }
        catch (IOException e) {
            throw new IOException("Failed to unmarshal " + type, e);
        }
    }

    @Override
    public void marshal(OutputStream responseOutputStream, Object response) throws IOException {
        Objects.requireNonNull(responseOutputStream, "responseOutputStream cannot be null");

        String jsonString = gson.toJson(response);

        try (OutputStreamWriter writer = new OutputStreamWriter(responseOutputStream, StandardCharsets.UTF_8)) {
            writer.write(jsonString);
        }
        catch (IOException e) {
            throw new IOException("Failed to marshal " + response.getClass(), e);
        }
    }

    @Override
    public void marshalFailure(OutputStream responseOutputStream, Throwable throwable) throws IOException {
        Objects.requireNonNull(responseOutputStream, "responseOutputStream cannot be null");
        Objects.requireNonNull(throwable, "throwable cannot be null");

        List<String> stackTrace = convertThrowableStackTraceToString(throwable);

        FailureModel model = new FailureModel();

        model.setErrorMessage(throwable.getMessage());
        model.setErrorType(throwable.getClass().getCanonicalName());
        model.setStackTrace(stackTrace);


        marshal(responseOutputStream, model);
    }

    private List<String> convertThrowableStackTraceToString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        try(PrintWriter pw = new PrintWriter(sw)) {
            throwable.printStackTrace(pw);
        }

        return Arrays.asList(sw.toString().split("\n"));
    }
}
