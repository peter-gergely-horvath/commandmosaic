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

 
package org.commandmosaic.http.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandDispatcherServletTest {

    private static final Type HASHMAP_TYPE = new TypeToken<Map<String, Object>>() {}.getType();

    private static final int port = 12345;
    private static final String localAddress = "http://localhost:" + port;

    private static Server server;

    // Gson is used to parse responses easily
    private final Gson gson = new GsonBuilder()
            .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .create();

    @BeforeClass
    public static void beforeTests() throws Exception {

        server = new Server(port);

        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);

        ServletHolder servletHolder = handler.addServletWithMapping(CommandDispatcherServlet.class, "/*");
        servletHolder.setInitParameter(CommandDispatcherServlet.COMMAND_DISPATCHER_ROOT_PACKAGE,
                CommandDispatcherServletTest.class.getPackage().getName());

        server.start();
    }

    @AfterClass
    public static void afterTests() throws Exception {

        server.stop();
    }

    @Test
    public void testRequestHandler() throws Exception {

        final long requestId = 42;

        Map<String, Object> request = new HashMap<>();
        request.put("id", requestId);
        request.put("command", "GreetCommand");
        request.put("parameters", Collections.singletonMap("name", "John Smith"));
        request.put("protocol", "CM/1.0");

        String requestString = gson.toJson(request);

        HttpResponse httpResponse = Request.Post(localAddress)
                .bodyString(requestString, ContentType.APPLICATION_JSON)
                .execute().returnResponse();

        final int statusCode = httpResponse.getStatusLine().getStatusCode();

        final int requestOKStatusCode = 200;
        Assert.assertEquals(requestOKStatusCode, statusCode);

        String jsonResponse = getResponseBodyAsString(httpResponse);
        Assert.assertNotNull(jsonResponse);

        Map<String, Object> responseAsMap = gson.fromJson(jsonResponse, HASHMAP_TYPE);


        Object resultObject = responseAsMap.get("result");
        Assert.assertNotNull(resultObject);

        Object requestIdObject = responseAsMap.get("id");
        Assert.assertNotNull(requestIdObject);
        Assert.assertEquals(requestId, requestIdObject);

        Assert.assertEquals("Hello John Smith", resultObject);
    }

    @Test
    public void testInvalidPayloadRequest() throws Exception {

        String requestString = "Hello world!";

        Request httpRequest = Request.Post(localAddress)
                .bodyString(requestString, ContentType.APPLICATION_JSON);

        HttpResponse httpResponse = httpRequest.execute().returnResponse();

        final int statusCode = httpResponse.getStatusLine().getStatusCode();

        final int badRequestStatus = 400;
        Assert.assertEquals(badRequestStatus, statusCode);

        String jsonResponse = getResponseBodyAsString(httpResponse);
        Assert.assertNotNull(jsonResponse);

        Map<String, Object> responseAsMap = gson.fromJson(jsonResponse, HASHMAP_TYPE);
        Assert.assertNotNull(responseAsMap);

        Object resultObject = responseAsMap.get("result");
        Assert.assertNull(resultObject); // if request failed, result must be null

        Object errorObject = responseAsMap.get("error");
        Assert.assertNotNull(errorObject);

        @SuppressWarnings("unchecked")
        Map<String, Object> error = (Map<String, Object>) errorObject;

        Object errorMessage = error.get("errorMessage");
        Assert.assertNotNull(errorMessage);
        Assert.assertEquals("Failed to unmarshal class org.commandmosaic.core.server.model.Request", errorMessage);


        Object message = error.get("errorType");
        Assert.assertNotNull(message);
        Assert.assertEquals("org.commandmosaic.core.marshaller.UnmarshalException", message);
    }

    @Test
    public void testNonExistentCommand() throws Exception {

        Gson gson = new Gson();
        Map<String, Object> request = new HashMap<>();
        request.put("command", "NonExistentCommand");
        request.put("parameters", Collections.singletonMap("name", "John Smith"));
        request.put("protocol", "CM/1.0");

        String requestString = gson.toJson(request);

        Request httpRequest = Request.Post(localAddress)
                .bodyString(requestString, ContentType.APPLICATION_JSON);

        HttpResponse httpResponse = httpRequest.execute().returnResponse();

        String responseText = getResponseBodyAsString(httpResponse);

        final int statusCode = httpResponse.getStatusLine().getStatusCode();

        final int badRequestStatus = 400;
        Assert.assertEquals(badRequestStatus, statusCode);

        Assert.assertTrue(responseText.contains("No such command: NonExistentCommand"));
    }

    @Test
    public void testMissingProtocolRequest() throws Exception {

        Map<String, Object> request = new HashMap<>();
        request.put("command", "GreetCommand");
        request.put("parameters", Collections.singletonMap("name", "John Smith"));

        String requestString = gson.toJson(request);

        Request httpRequest = Request.Post(localAddress)
                .bodyString(requestString, ContentType.APPLICATION_JSON);

        HttpResponse httpResponse = httpRequest.execute().returnResponse();

        String responseText = getResponseBodyAsString(httpResponse);

        final int statusCode = httpResponse.getStatusLine().getStatusCode();

        final int badRequestStatus = 400;
        Assert.assertEquals(badRequestStatus, statusCode);

        Assert.assertTrue(responseText.contains("Request protocol version is invalid"));
    }

    private String getResponseBodyAsString(HttpResponse httpResponse) throws IOException {
        HttpEntity entity = httpResponse.getEntity();
        InputStream content = entity.getContent();

        return new BufferedReader(
                new InputStreamReader(content, StandardCharsets.UTF_8)).lines()
                .collect(Collectors.joining("\n"));
    }
}
