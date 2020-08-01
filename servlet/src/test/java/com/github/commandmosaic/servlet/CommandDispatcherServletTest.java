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

 
package com.github.commandmosaic.servlet;

import com.github.commandmosaic.http.servlet.CommandDispatcherServlet;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Content;
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

    private static final int port = 12345;
    private static Server server;

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

        Gson gson = new Gson();
        Map<String, Object> request = new HashMap<>();
        request.put("command", "GreetCommand");
        request.put("parameters", Collections.singletonMap("name", "John Smith"));
        request.put("protocol", "CM/1.0");

        String requestString = gson.toJson(request);

        Content content = Request.Post("http://localhost:" + port)
                .bodyString(requestString, ContentType.APPLICATION_JSON)
                .execute().returnContent();

        String jsonResponse = content.toString();

        Assert.assertNotNull(jsonResponse);

        Type typeOfHashMap = new TypeToken<Map<String, Object>>() { }.getType();
        Map<String, Object> responseAsMap = gson.fromJson(jsonResponse, typeOfHashMap);

        Object resultObject = responseAsMap.get("result");
        Assert.assertNotNull(resultObject);

        String result = resultObject.toString();
        Assert.assertEquals("Hello John Smith", result);
    }

    @Test
    public void testNonExistentCommand() throws Exception {

        Gson gson = new Gson();
        Map<String, Object> request = new HashMap<>();
        request.put("command", "NonExistentCommand");
        request.put("parameters", Collections.singletonMap("name", "John Smith"));
        request.put("protocol", "CM/1.0");

        String requestString = gson.toJson(request);

        Request httpRequest = Request.Post("http://localhost:" + port)
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

        Gson gson = new Gson();
        Map<String, Object> request = new HashMap<>();
        request.put("command", "GreetCommand");
        request.put("parameters", Collections.singletonMap("name", "John Smith"));

        String requestString = gson.toJson(request);

        Request httpRequest = Request.Post("http://localhost:" + port)
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
