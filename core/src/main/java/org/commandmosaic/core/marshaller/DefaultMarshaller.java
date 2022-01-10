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

package org.commandmosaic.core.marshaller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

final class DefaultMarshaller implements Marshaller {

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public <T> T unmarshal(InputStream requestInputStream, Class<T> type) throws IOException {
        Objects.requireNonNull(requestInputStream, "requestInputStream cannot be null");
        Objects.requireNonNull(type, "type cannot be null");

        try (InputStreamReader inputStreamReader = new InputStreamReader(requestInputStream, StandardCharsets.UTF_8)) {
            return objectMapper.readValue(inputStreamReader, type);
        }
        catch (IOException e) {
            throw new IOException("Failed to unmarshal " + type, e);
        }
    }

    @Override
    public void marshal(OutputStream responseOutputStream, Object value) throws IOException {
        Objects.requireNonNull(responseOutputStream, "responseOutputStream cannot be null");
        Objects.requireNonNull(responseOutputStream, "value cannot be null");

        String jsonString = objectMapper.writeValueAsString(value);

        try (OutputStreamWriter writer = new OutputStreamWriter(responseOutputStream, StandardCharsets.UTF_8)) {
            writer.write(jsonString);
        }
        catch (IOException e) {
            throw new IOException("Failed to marshal: " + value, e);
        }
    }
}
