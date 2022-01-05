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

package org.commandmosaic.security.authorizer.factory;

import org.commandmosaic.api.Command;
import org.commandmosaic.api.CommandContext;
import org.commandmosaic.api.executor.ParameterSource;
import org.commandmosaic.security.AccessDeniedException;
import org.commandmosaic.security.authorizer.Authorizer;
import org.commandmosaic.security.core.Identity;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

public class DefaultAuthorizerFactoryTest {

    static final String USER_ROLE_NAME = "ROLE_USER";

    private DefaultAuthorizerFactory defaultAuthorizerFactory;

    @Before
    public void beforeTest() {
        defaultAuthorizerFactory = new DefaultAuthorizerFactory();
    }

    @Test
    public void testPublicAuthorizer() {

        ParameterSource mockParameterSource = EasyMock.createStrictMock(ParameterSource.class);
        CommandContext mockCommandContext = EasyMock.createStrictMock(CommandContext.class);

        Identity mockUserIdentity = EasyMock.createStrictMock(Identity.class);

        EasyMock.expect(mockUserIdentity.getAuthorities())
                .andReturn(Collections.emptySet())
                .anyTimes();


        EasyMock.replay(mockParameterSource, mockCommandContext, mockUserIdentity);

        Authorizer authorizer = defaultAuthorizerFactory.getAuthorizer(PublicCommand.class);

        Assert.assertFalse(authorizer.isAuthenticationRequired());

        authorizer.checkAuthorization(PublicCommand.class, null, mockParameterSource, mockCommandContext);

        authorizer.checkAuthorization(PublicCommand.class, mockUserIdentity, mockParameterSource, mockCommandContext);

        EasyMock.verify(mockParameterSource, mockCommandContext, mockUserIdentity);
    }

    @Test
    public void testRequiresAuthenticationAuthorizer() {

        ParameterSource mockParameterSource = EasyMock.createStrictMock(ParameterSource.class);
        CommandContext mockCommandContext = EasyMock.createStrictMock(CommandContext.class);

        Identity mockUserIdentity = EasyMock.createStrictMock(Identity.class);

        EasyMock.expect(mockUserIdentity.getAuthorities())
                .andReturn(Collections.emptySet())
                .anyTimes();

        EasyMock.replay(mockParameterSource, mockCommandContext, mockUserIdentity);

        Authorizer authorizer = defaultAuthorizerFactory.getAuthorizer(RequiresAuthenticationCommand.class);

        Assert.assertTrue(authorizer.isAuthenticationRequired());

        Assert.assertThrows(AccessDeniedException.class, () ->
                authorizer.checkAuthorization(RequiresAuthenticationCommand.class,
                        null, mockParameterSource, mockCommandContext));


        authorizer.checkAuthorization(RequiresAuthenticationCommand.class, mockUserIdentity,
                mockParameterSource, mockCommandContext);

        EasyMock.verify(mockParameterSource, mockCommandContext, mockUserIdentity);
    }

    @Test
    public void testAuthorityAuthorizer() {

        ParameterSource mockParameterSource = EasyMock.createStrictMock(ParameterSource.class);
        CommandContext mockCommandContext = EasyMock.createStrictMock(CommandContext.class);

        Identity mockUserIdentity = EasyMock.createStrictMock(Identity.class);
        Identity mockGuestIdentity = EasyMock.createStrictMock(Identity.class);

        EasyMock.expect(mockUserIdentity.getAuthorities())
                .andReturn(Collections.singleton(USER_ROLE_NAME))
                .anyTimes();


        EasyMock.expect(mockGuestIdentity.getAuthorities())
                .andReturn(Collections.singleton("ROLE_GUEST"))
                .anyTimes();


        EasyMock.replay(mockParameterSource, mockCommandContext, mockUserIdentity, mockGuestIdentity);


        assertAuthorizerRequiresAuthenticatorAndUserRole(UserCommand1.class, mockUserIdentity, mockGuestIdentity,
                mockParameterSource, mockCommandContext);

        assertAuthorizerRequiresAuthenticatorAndUserRole(UserCommand2.class, mockUserIdentity, mockGuestIdentity,
                mockParameterSource, mockCommandContext);

        assertAuthorizerRequiresAuthenticatorAndUserRole(UserCommand3.class, mockUserIdentity, mockGuestIdentity,
                mockParameterSource, mockCommandContext);


        assertAuthorizerRequiresAuthenticatorAndUserRole(UserCommand3.class, mockUserIdentity, mockGuestIdentity,
                mockParameterSource, mockCommandContext);

        EasyMock.verify(mockParameterSource, mockCommandContext, mockUserIdentity, mockGuestIdentity);
    }

    private void assertAuthorizerRequiresAuthenticatorAndUserRole(Class<? extends Command<?>> commandClass,
                                                                  Identity mockUserIdentity,
                                                                  Identity mockGuestIdentity,
                                                                  ParameterSource mockParameterSource,
                                                                  CommandContext mockCommandContext) {

        Authorizer authorizer = defaultAuthorizerFactory.getAuthorizer(commandClass);

        Assert.assertTrue(authorizer.isAuthenticationRequired());

        Assert.assertThrows(AccessDeniedException.class, () ->
                authorizer.checkAuthorization(UserCommand1.class,
                        null, mockParameterSource, mockCommandContext));

        authorizer.checkAuthorization(UserCommand1.class, mockUserIdentity, mockParameterSource, mockCommandContext);

        Assert.assertThrows(AccessDeniedException.class, () ->
                authorizer.checkAuthorization(UserCommand1.class,
                        mockGuestIdentity, mockParameterSource, mockCommandContext));
    }


    @Test
    public void testSameAuthorityAnnotationValuesHaveTheSameAuthorizer() {

        final Authorizer authorizer1 = defaultAuthorizerFactory.getAuthorizer(UserCommand1.class);

        final Authorizer authorizer2 = defaultAuthorizerFactory.getAuthorizer(UserCommand2.class);

        final Authorizer authorizer3 = defaultAuthorizerFactory.getAuthorizer(UserCommand3.class);

        final Authorizer authorizer4 = defaultAuthorizerFactory.getAuthorizer(UserCommand4.class);


        Assert.assertTrue("Authorizers should be the same if commands have the same role requirement",
                authorizer1 == authorizer2 && authorizer2 == authorizer3 && authorizer3 == authorizer4);
    }

    @Test
    public void testInvalidAccessAnnotationsThrowsIllegalStateException() {

        Assert.assertThrows(IllegalStateException.class, () ->
                defaultAuthorizerFactory.getAuthorizer(
                        InvalidPublicAndAuthenticatedCommand.class));

        Assert.assertThrows(IllegalStateException.class, () ->
                defaultAuthorizerFactory.getAuthorizer(
                        InvalidPublicAndRequiresAuthorityCommand.class));

        Assert.assertThrows(IllegalStateException.class, () ->
                defaultAuthorizerFactory.getAuthorizer(
                        InvalidPublicAndRequiresAnyOfTheAuthoritiesCommand.class));

        Assert.assertThrows(IllegalStateException.class, () ->
                defaultAuthorizerFactory.getAuthorizer(
                        InvalidRequiresAuthorityAndRequiresAnyOfTheAuthoritiesCommand.class));
    }

    @Test
    public void testMissingAccessAnnotationsThrowsIllegalStateException() {

        Assert.assertThrows(IllegalStateException.class, () ->
                defaultAuthorizerFactory.getAuthorizer(
                        NotAnnotatedCommand.class));
    }


}
