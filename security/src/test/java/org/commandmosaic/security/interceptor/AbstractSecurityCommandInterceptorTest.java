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

 
package org.commandmosaic.security.interceptor;

import org.commandmosaic.api.CommandContext;
import org.commandmosaic.api.CommandDispatcher;
import org.commandmosaic.api.configuration.CommandDispatcherConfiguration;
import org.commandmosaic.core.server.model.DefaultCommandContext;
import org.commandmosaic.plain.PlainCommandDispatcherFactory;
import org.commandmosaic.security.AuthenticationException;
import org.commandmosaic.security.AccessDeniedException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class AbstractSecurityCommandInterceptorTest {

    private CommandDispatcher commandDispatcher;

    public static final class MockAbstractSecurityCommandInterceptor extends AbstractSecurityCommandInterceptor {


        @Override
        protected Set<String> attemptLogin(CommandContext commandContext) {
            Map<String, Object> auth = commandContext.getAuth();
            if (auth != null) {
                String userName = (String) auth.get("username");
                String password = (String) auth.get("password");

                if ((!"johnsmith".equals(userName) || !"foobar".equals(password))) {
                    throw new AuthenticationException("Authentication failed");
                }

                @SuppressWarnings("unchecked")
                Set<String> roles = (Set<String>) auth.get("roles");


                return roles != null ? roles : Collections.emptySet();
            }

            throw new AuthenticationException("Failed to login: no auth");
        }
    }

    @Before
    public void beforeTest() {

        CommandDispatcherConfiguration configuration = CommandDispatcherConfiguration.builder()
                .rootPackageFromClass(AbstractSecurityCommandInterceptorTest.class)
                .interceptor(MockAbstractSecurityCommandInterceptor.class)
                .build();

        commandDispatcher = PlainCommandDispatcherFactory.getInstance().getCommandDispatcher(configuration);

    }

    @Test
    public void testAuthenticationOnlyWithMissingCredentials() {

        Map<String, Object> authMap = Collections.emptyMap();

        Assert.assertThrows(AuthenticationException.class, () -> commandDispatcher.dispatchCommand(
                AuthenticationOnlyCommand.class, null, new DefaultCommandContext(authMap)));
    }

    @Test
    public void testAuthenticationOnlyWithCorrectCredentials() {

        Map<String, Object> authMap = new HashMap<>();
        authMap.put("username", "johnsmith");
        authMap.put("password", "foobar");

        String response = commandDispatcher.dispatchCommand(
                AuthenticationOnlyCommand.class, null, new DefaultCommandContext(authMap));

        Assert.assertEquals("Response from AuthenticationOnlyCommand", response);
    }

    @Test
    public void testAuthenticationOnlyWithInvalidUserName() {

        Map<String, Object> authMap = new HashMap<>();
        authMap.put("username", "NOT_THE_RIGHT_ONE");
        authMap.put("password", "foobar");

        Assert.assertThrows(AuthenticationException.class, () -> commandDispatcher.dispatchCommand(
                AuthenticationOnlyCommand.class, null, new DefaultCommandContext(authMap)));
    }

    @Test
    public void testAuthenticationOnlyWithInvalidPassword() {

        Map<String, Object> authMap = new HashMap<>();
        authMap.put("username", "johnsmith");
        authMap.put("password", "NOT_THE_RIGHT_ONE");

        Assert.assertThrows(AuthenticationException.class, () -> commandDispatcher.dispatchCommand(
                AuthenticationOnlyCommand.class, null, new DefaultCommandContext(authMap)));
    }

    @Test
    public void testRolesWithAuthenticationOnly() {

        Map<String, Object> authMap = new HashMap<>();
        authMap.put("username", "johnsmith");
        authMap.put("password", "foobar");

        Assert.assertThrows(AccessDeniedException.class, () -> commandDispatcher.dispatchCommand(
                RolesCommand.class, null, new DefaultCommandContext(authMap)));
    }

    @Test
    public void testRolesWithInvalidUserName() {

        Map<String, Object> authMap = new HashMap<>();
        authMap.put("username", "NOT_THE_RIGHT_ONE");
        authMap.put("password", "foobar");

        Assert.assertThrows(AuthenticationException.class, () -> commandDispatcher.dispatchCommand(
                RolesCommand.class, null, new DefaultCommandContext(authMap)));
    }

    @Test
    public void testRolesWithInvalidPassword() {

        Map<String, Object> authMap = new HashMap<>();
        authMap.put("username", "johnsmith");
        authMap.put("password", "NOT_THE_RIGHT_ONE");

        Assert.assertThrows(AuthenticationException.class, () -> commandDispatcher.dispatchCommand(
                RolesCommand.class, null, new DefaultCommandContext(authMap)));
    }


    @Test
    public void testRolesWithCorrectAuthenticationButPartialAuthorization() {

        Map<String, Object> authMap = new HashMap<>();
        authMap.put("username", "johnsmith");
        authMap.put("password", "foobar");
        authMap.put("roles", Collections.singleton("FOO"));

        Assert.assertThrows(AccessDeniedException.class, () -> commandDispatcher.dispatchCommand(
                RolesCommand.class, null, new DefaultCommandContext(authMap)));
    }

    @Test
    public void testRolesWithCorrectAuthenticationAndCorrectAuthorization() {

        Map<String, Object> authMap = new HashMap<>();
        authMap.put("username", "johnsmith");
        authMap.put("password", "foobar");
        authMap.put("roles", new HashSet<>(Arrays.asList("FOO", "BAR")));

        String result = commandDispatcher.dispatchCommand(
                RolesCommand.class, null, new DefaultCommandContext(authMap));

        Assert.assertEquals("Response from RolesCommand", result);
    }

    @Test
    public void testRolesWithCorrectAuthenticationAndAdditionalAuthorization() {

        Map<String, Object> authMap = new HashMap<>();
        authMap.put("username", "johnsmith");
        authMap.put("password", "foobar");
        authMap.put("roles", new HashSet<>(Arrays.asList("FOO", "BAR", "BAZ")));

        String result = commandDispatcher.dispatchCommand(
                RolesCommand.class, null, new DefaultCommandContext(authMap));

        Assert.assertEquals("Response from RolesCommand", result);
    }

    @Test
    public void testMisconfiguredCommand() {

        Map<String, Object> authMap = new HashMap<>();
        authMap.put("username", "johnsmith");
        authMap.put("password", "foobar");
        authMap.put("roles", new HashSet<>(Arrays.asList("FOO", "BAR", "BAZ")));

        Assert.assertThrows(IllegalStateException.class, () ->
                commandDispatcher.dispatchCommand(
                MisconfiguredCommand.class, null, new DefaultCommandContext(authMap)));
    }

    @Test
    public void testNotAnnotatedCommand() {

        Map<String, Object> authMap = new HashMap<>();
        authMap.put("username", "johnsmith");
        authMap.put("password", "foobar");
        authMap.put("roles", new HashSet<>(Arrays.asList("FOO", "BAR", "BAZ")));

        Assert.assertThrows(IllegalStateException.class, () ->
                commandDispatcher.dispatchCommand(
                        NotAnnotatedCommand.class, null, new DefaultCommandContext(authMap)));
    }
}
