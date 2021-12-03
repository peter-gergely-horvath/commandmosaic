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


import org.commandmosaic.api.Command;
import org.commandmosaic.api.CommandContext;
import org.commandmosaic.api.executor.ParameterSource;
import org.commandmosaic.api.interceptor.InterceptorChain;
import org.commandmosaic.core.parameter.source.ParameterSources;
import org.commandmosaic.security.AccessDeniedException;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;

public class DefaultSecurityCommandInterceptorTest {

    private MockDefaultSecurityCommandInterceptor securityCommandInterceptor;
    private CommandContext mockCommandContext;
    private InterceptorChain mockInterceptorChain;
    private ParameterSource parameterSource;

    @Before
    public void beforeTest() {

        securityCommandInterceptor = new MockDefaultSecurityCommandInterceptor(
                        new UserNamePasswordIdentity(
                                "foo-user", "foo-password", "ROLE_USER"),
                        new UserNamePasswordIdentity(
                                "bar-user", "bar-password"),
                        new UserNamePasswordIdentity("admin-user",
                                "admin-password", "ROLE_ADMIN"));

        mockCommandContext = EasyMock.createStrictMock("CommandContext", CommandContext.class);
        mockInterceptorChain = EasyMock.createStrictMock("InterceptorChain", InterceptorChain.class);

        parameterSource = ParameterSources.mapParameterSource(Collections.emptyMap());
    }

    @After
    public void afterTest() {
        EasyMock.verify(mockCommandContext);
        EasyMock.verify(mockInterceptorChain);
    }

    @SuppressWarnings("unchecked")
    private void expectCommandIsExecuted(Class<? extends Command> commandClass) {
        EasyMock.expect(
                mockInterceptorChain.execute(
                        eq(commandClass),
                        eq(parameterSource),
                        anyObject(CommandContext.class)))
                .andReturn(null)
                .once();
    }


    @Test
    public void testPublicCommandWithNullAuthenticationIsAllowed() {

        expectCommandIsExecuted(PublicCommand.class);

        EasyMock.replay(mockCommandContext, mockInterceptorChain);

        securityCommandInterceptor.intercept(PublicCommand.class,
                parameterSource, mockCommandContext, mockInterceptorChain);
    }

    @Test
    public void testAuthenticationOnlyCommandWithNullAuthenticationIsDenied() {

        EasyMock.expect(mockCommandContext.getAuth()).andReturn(null).once();

        EasyMock.replay(mockCommandContext, mockInterceptorChain);

        Assert.assertThrows(AccessDeniedException.class, () ->
                securityCommandInterceptor.intercept(
                        AuthenticationOnlyCommand.class,
                        parameterSource,
                        mockCommandContext,
                        mockInterceptorChain));
    }

    @Test
    public void testAuthenticationOnlyCommandWithInvalidCredentialsIsDenied() {

        HashMap<String, Object> authMap = new HashMap<>();
        authMap.put("username", "foo-user");
        authMap.put("password", "NOT_THE_RIGHT_PASSWORD");

        EasyMock.expect(mockCommandContext.getAuth()).andReturn(authMap).once();

        EasyMock.replay(mockCommandContext, mockInterceptorChain);

        Assert.assertThrows(AccessDeniedException.class, () ->
                securityCommandInterceptor.intercept(AuthenticationOnlyCommand.class,
                        parameterSource, mockCommandContext, mockInterceptorChain));
    }

    @Test
    public void testAuthenticationOnlyCommandWithValidCredentialsAndNoRolesIsAllowed() {

        HashMap<String, Object> authMap = new HashMap<>();
        // NOTE: 'bar-user' has no roles associated with it
        authMap.put("username", "bar-user");
        authMap.put("password", "bar-password");

        EasyMock.expect(mockCommandContext.getAuth()).andReturn(authMap).once();

        expectCommandIsExecuted(AuthenticationOnlyCommand.class);

        EasyMock.replay(mockCommandContext, mockInterceptorChain);

        securityCommandInterceptor.intercept(AuthenticationOnlyCommand.class,
                parameterSource, mockCommandContext, mockInterceptorChain);
    }

    @Test
    public void testAuthenticationOnlyCommandWithValidCredentialsAndRolesIsAllowed() {

        HashMap<String, Object> authMap = new HashMap<>();
        authMap.put("username", "foo-user");
        authMap.put("password", "foo-password");

        EasyMock.expect(mockCommandContext.getAuth()).andReturn(authMap).once();

        expectCommandIsExecuted(AuthenticationOnlyCommand.class);

        EasyMock.replay(mockCommandContext, mockInterceptorChain);

        securityCommandInterceptor.intercept(AuthenticationOnlyCommand.class,
                parameterSource, mockCommandContext, mockInterceptorChain);
    }


    @Test
    public void testUserCommandWithNullAuthenticationIsDenied() {

        EasyMock.expect(mockCommandContext.getAuth()).andReturn(null).once();

        EasyMock.replay(mockCommandContext, mockInterceptorChain);

        Assert.assertThrows(AccessDeniedException.class, () ->
                securityCommandInterceptor.intercept(UserCommand.class,
                        parameterSource, mockCommandContext, mockInterceptorChain));
    }

    @Test
    public void testUserCommandWithInvalidCredentialsIsDenied() {

        HashMap<String, Object> authMap = new HashMap<>();
        authMap.put("username", "foo-user");
        authMap.put("password", "NOT_THE_RIGHT_PASSWORD");

        EasyMock.expect(mockCommandContext.getAuth()).andReturn(authMap).once();

        EasyMock.replay(mockCommandContext, mockInterceptorChain);

        Assert.assertThrows(AccessDeniedException.class, () ->
                securityCommandInterceptor.intercept(UserCommand.class,
                        parameterSource, mockCommandContext, mockInterceptorChain));
    }

    @Test
    public void testUserCommandWithValidCredentialsButNoRolesIsDenied() {

        HashMap<String, Object> authMap = new HashMap<>();
        authMap.put("username", "bar-user");
        authMap.put("password", "bar-password");

        EasyMock.expect(mockCommandContext.getAuth()).andReturn(authMap).once();

        EasyMock.replay(mockCommandContext, mockInterceptorChain);

        Assert.assertThrows(AccessDeniedException.class, () ->
                securityCommandInterceptor.intercept(UserCommand.class,
                        parameterSource, mockCommandContext, mockInterceptorChain));
    }

    @Test
    public void testUserCommandWithValidCredentialsAndRolesIsAllowed() {

        HashMap<String, Object> authMap = new HashMap<>();
        authMap.put("username", "foo-user");
        authMap.put("password", "foo-password");

        EasyMock.expect(mockCommandContext.getAuth()).andReturn(authMap).once();

        expectCommandIsExecuted(UserCommand.class);

        EasyMock.replay(mockCommandContext, mockInterceptorChain);

        securityCommandInterceptor.intercept(UserCommand.class,
                parameterSource, mockCommandContext, mockInterceptorChain);
    }


    @Test
    public void testAdminCommandWithNullAuthenticationIsDenied() {

        EasyMock.expect(mockCommandContext.getAuth()).andReturn(null).once();

        EasyMock.replay(mockCommandContext, mockInterceptorChain);

        Assert.assertThrows(AccessDeniedException.class, () ->
                securityCommandInterceptor.intercept(AdminCommand.class,
                        parameterSource, mockCommandContext, mockInterceptorChain));
    }

    @Test
    public void testAdminCommandWithInvalidCredentialsIsDenied() {

        HashMap<String, Object> authMap = new HashMap<>();
        authMap.put("username", "foo-user");
        authMap.put("password", "NOT_THE_RIGHT_PASSWORD");

        EasyMock.expect(mockCommandContext.getAuth()).andReturn(authMap).once();

        EasyMock.replay(mockCommandContext, mockInterceptorChain);

        Assert.assertThrows(AccessDeniedException.class, () ->
                securityCommandInterceptor.intercept(AdminCommand.class,
                        parameterSource, mockCommandContext, mockInterceptorChain));
    }

    @Test
    public void testAdminCommandWithInvalidAdminCredentialsIsDenied() {

        HashMap<String, Object> authMap = new HashMap<>();
        authMap.put("username", "admin-user");
        authMap.put("password", "NOT_THE_RIGHT_PASSWORD");

        EasyMock.expect(mockCommandContext.getAuth()).andReturn(authMap).once();

        EasyMock.replay(mockCommandContext, mockInterceptorChain);

        Assert.assertThrows(AccessDeniedException.class, () ->
                securityCommandInterceptor.intercept(AdminCommand.class,
                        parameterSource, mockCommandContext, mockInterceptorChain));
    }

    @Test
    public void testAdminCommandWithValidUserCredentialsAndNoRolesIsDenied() {

        HashMap<String, Object> authMap = new HashMap<>();
        // NOTE: 'bar-user' has no roles associated with it
        authMap.put("username", "bar-user");
        authMap.put("password", "bar-password");

        EasyMock.expect(mockCommandContext.getAuth()).andReturn(authMap).once();

        EasyMock.replay(mockCommandContext, mockInterceptorChain);

        Assert.assertThrows(AccessDeniedException.class, () ->
                securityCommandInterceptor.intercept(AdminCommand.class,
                        parameterSource, mockCommandContext, mockInterceptorChain));
    }

    @Test
    public void testAdminCommandWithValidUserCredentialsAndRolesIsDenied() {

        HashMap<String, Object> authMap = new HashMap<>();
        authMap.put("username", "foo-user");
        authMap.put("password", "foo-password");

        EasyMock.expect(mockCommandContext.getAuth()).andReturn(authMap).once();


        EasyMock.replay(mockCommandContext, mockInterceptorChain);

        Assert.assertThrows(AccessDeniedException.class, () ->
                securityCommandInterceptor.intercept(AdminCommand.class,
                        parameterSource, mockCommandContext, mockInterceptorChain));
    }

    @Test
    public void testAdminCommandWithValidAdminCredentialsAndRolesIsAllowed() {

        HashMap<String, Object> authMap = new HashMap<>();
        authMap.put("username", "admin-user");
        authMap.put("password", "admin-password");

        EasyMock.expect(mockCommandContext.getAuth()).andReturn(authMap).once();

        expectCommandIsExecuted(AdminCommand.class);

        EasyMock.replay(mockCommandContext, mockInterceptorChain);

        securityCommandInterceptor.intercept(AdminCommand.class,
                parameterSource, mockCommandContext, mockInterceptorChain);
    }

    @Test
    public void testMisconfiguredCommandFailsWithIllegalStateException() {

        EasyMock.replay(mockCommandContext, mockInterceptorChain);

        Assert.assertThrows(IllegalStateException.class, () ->
                securityCommandInterceptor.intercept(MisconfiguredCommand.class,
                        parameterSource, mockCommandContext, mockInterceptorChain));
    }


    @Test
    public void testNotAnnotatedCommandFailsWithIllegalStateExceptionWhenAuthenticated() {

        HashMap<String, Object> authMap = new HashMap<>();
        authMap.put("username", "admin-user");
        authMap.put("password", "admin-password");

        EasyMock.replay(mockCommandContext, mockInterceptorChain);

        Assert.assertThrows(IllegalStateException.class, () ->
                securityCommandInterceptor.intercept(NotAnnotatedCommand.class,
                        parameterSource, mockCommandContext, mockInterceptorChain));
    }

    @Test
    public void testNotAnnotatedCommandFailsWithIllegalStateExceptionWhenNotLoggedIn() {

        EasyMock.replay(mockCommandContext, mockInterceptorChain);

        Assert.assertThrows(IllegalStateException.class, () ->
                securityCommandInterceptor.intercept(NotAnnotatedCommand.class,
                        parameterSource, mockCommandContext, mockInterceptorChain));
    }
}
