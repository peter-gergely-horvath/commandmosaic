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

package org.commandmosaic.spring.security.adapter;


import org.commandmosaic.api.CommandContext;
import org.commandmosaic.api.executor.CommandExecutor;
import org.commandmosaic.api.executor.ParameterSource;
import org.commandmosaic.core.parameter.source.ParameterSources;
import org.commandmosaic.security.AccessDeniedException;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;

public class SpringSecurityCommandInterceptorTest {

    private SpringSecurityCommandInterceptor springSecurityCommandInterceptor;
    private CommandContext mockCommandContext;
    private CommandExecutor mockInterceptorChain;
    private ParameterSource parameterSource;

    @Before
    public void beforeTest() {
        springSecurityCommandInterceptor = new SpringSecurityCommandInterceptor();

        mockCommandContext = EasyMock.createStrictMock(CommandContext.class);
        mockInterceptorChain = EasyMock.createStrictMock(CommandExecutor.class);

        parameterSource = ParameterSources.mapParameterSource(Collections.emptyMap());
    }

    @After
    public void afterTest() {
        EasyMock.verify(mockCommandContext, mockInterceptorChain);
    }


    @Test
    public void testPublicCommandWithNullAuthentication() {

        EasyMock.expect(
                mockInterceptorChain.execute(PublicCommand.class, parameterSource, mockCommandContext))
                .andReturn(null)
                .once();

        EasyMock.replay(mockCommandContext, mockInterceptorChain);

        SecurityContext securityContext = SecurityContextHolder.getContext();

        securityContext.setAuthentication(null);

        springSecurityCommandInterceptor.intercept(PublicCommand.class,
                parameterSource, mockCommandContext, mockInterceptorChain);
    }

    @Test
    public void testPublicCommandWithAnonymousAuthentication() {

        EasyMock.expect(
                mockInterceptorChain.execute(PublicCommand.class, parameterSource, mockCommandContext))
                .andReturn(null)
                .once();

        EasyMock.replay(mockCommandContext, mockInterceptorChain);

        GrantedAuthority anonymousAuthority = new SimpleGrantedAuthority("ROLE_anonymous");
        List<GrantedAuthority> grantedAuthorities = Collections.singletonList(anonymousAuthority);

        AnonymousAuthenticationToken anonymousAuthenticationToken =
                new AnonymousAuthenticationToken("anonymous", "anonymous", grantedAuthorities);

        SecurityContext securityContext = SecurityContextHolder.getContext();

        securityContext.setAuthentication(anonymousAuthenticationToken);

        springSecurityCommandInterceptor.intercept(PublicCommand.class,
                parameterSource, mockCommandContext, mockInterceptorChain);
    }

    @Test
    public void testPublicCommandWithUserAuthentication() {

        EasyMock.expect(
                mockInterceptorChain.execute(PublicCommand.class, parameterSource, mockCommandContext))
                .andReturn(null)
                .once();

        EasyMock.replay(mockCommandContext, mockInterceptorChain);

        GrantedAuthority anonymousAuthority = new SimpleGrantedAuthority("ROLE_USER");
        List<GrantedAuthority> grantedAuthorities = Collections.singletonList(anonymousAuthority);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken("user", "user", grantedAuthorities);

        SecurityContext securityContext = SecurityContextHolder.getContext();

        securityContext.setAuthentication(authentication);

        springSecurityCommandInterceptor.intercept(PublicCommand.class,
                parameterSource, mockCommandContext, mockInterceptorChain);
    }

    @Test
    public void testPublicCommandWithAdminAuthentication() {

        EasyMock.expect(
                mockInterceptorChain.execute(PublicCommand.class, parameterSource, mockCommandContext))
                .andReturn(null)
                .once();

        EasyMock.replay(mockCommandContext, mockInterceptorChain);

        GrantedAuthority anonymousAuthority = new SimpleGrantedAuthority("ROLE_ADMIN");
        List<GrantedAuthority> grantedAuthorities = Collections.singletonList(anonymousAuthority);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken("admin", "admin", grantedAuthorities);

        SecurityContext securityContext = SecurityContextHolder.getContext();

        securityContext.setAuthentication(authentication);

        springSecurityCommandInterceptor.intercept(PublicCommand.class,
                parameterSource, mockCommandContext, mockInterceptorChain);
    }

    @Test
    public void testUserCommandWithNullAuthentication() {

        EasyMock.replay(mockCommandContext, mockInterceptorChain);

        SecurityContext securityContext = SecurityContextHolder.getContext();

        securityContext.setAuthentication(null);

        Assert.assertThrows(AccessDeniedException.class, () ->
                springSecurityCommandInterceptor.intercept(UserCommand.class,
                        parameterSource, mockCommandContext, mockInterceptorChain));
    }

    @Test
    public void testUserCommandWithAnonymousAuthentication() {

        EasyMock.replay(mockCommandContext, mockInterceptorChain);

        GrantedAuthority anonymousAuthority = new SimpleGrantedAuthority("ROLE_anonymous");
        List<GrantedAuthority> grantedAuthorities = Collections.singletonList(anonymousAuthority);

        AnonymousAuthenticationToken anonymousAuthenticationToken =
                new AnonymousAuthenticationToken("anonymous", "anonymous", grantedAuthorities);

        SecurityContext securityContext = SecurityContextHolder.getContext();

        securityContext.setAuthentication(anonymousAuthenticationToken);

        Assert.assertThrows(AccessDeniedException.class, () ->
                springSecurityCommandInterceptor.intercept(UserCommand.class,
                        parameterSource, mockCommandContext, mockInterceptorChain));
    }

    @Test
    public void testUserCommandWithUserAuthentication() {

        EasyMock.expect(
                mockInterceptorChain.execute(
                        eq(UserCommand.class),
                        eq(parameterSource),
                        anyObject(CommandContext.class)))
                .andReturn(null)
                .once();

        EasyMock.replay(mockCommandContext, mockInterceptorChain);

        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_USER");
        List<GrantedAuthority> grantedAuthorities = Collections.singletonList(grantedAuthority);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken("user", "user", grantedAuthorities);

        SecurityContext securityContext = SecurityContextHolder.getContext();

        securityContext.setAuthentication(authentication);

        springSecurityCommandInterceptor.intercept(UserCommand.class,
                parameterSource, mockCommandContext, mockInterceptorChain);
    }

    @Test
    public void testUserCommandWithAdminAuthentication() {

        EasyMock.expect(
                mockInterceptorChain.execute(
                        eq(UserCommand.class),
                        eq(parameterSource),
                        anyObject(CommandContext.class)))
                .andReturn(null)
                .once();

        EasyMock.replay(mockCommandContext, mockInterceptorChain);

        GrantedAuthority anonymousAuthority = new SimpleGrantedAuthority("ROLE_ADMIN");
        List<GrantedAuthority> grantedAuthorities = Collections.singletonList(anonymousAuthority);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken("admin", "admin", grantedAuthorities);

        SecurityContext securityContext = SecurityContextHolder.getContext();

        securityContext.setAuthentication(authentication);

        springSecurityCommandInterceptor.intercept(UserCommand.class,
                parameterSource, mockCommandContext, mockInterceptorChain);
    }

    @Test
    public void testAdminCommandWithNullAuthentication() {

        EasyMock.replay(mockCommandContext, mockInterceptorChain);

        SecurityContext securityContext = SecurityContextHolder.getContext();

        securityContext.setAuthentication(null);

        Assert.assertThrows(AccessDeniedException.class, () ->
                springSecurityCommandInterceptor.intercept(AdminCommand.class,
                        parameterSource, mockCommandContext, mockInterceptorChain));
    }

    @Test
    public void testAdminCommandWithAnonymousAuthentication() {

        EasyMock.replay(mockCommandContext, mockInterceptorChain);

        GrantedAuthority anonymousAuthority = new SimpleGrantedAuthority("ROLE_anonymous");
        List<GrantedAuthority> grantedAuthorities = Collections.singletonList(anonymousAuthority);

        AnonymousAuthenticationToken anonymousAuthenticationToken =
                new AnonymousAuthenticationToken("anonymous", "anonymous", grantedAuthorities);

        SecurityContext securityContext = SecurityContextHolder.getContext();

        securityContext.setAuthentication(anonymousAuthenticationToken);

        Assert.assertThrows(AccessDeniedException.class, () ->
                springSecurityCommandInterceptor.intercept(AdminCommand.class,
                        parameterSource, mockCommandContext, mockInterceptorChain));
    }

    @Test
    public void testAdminCommandWithUserAuthentication() {

        EasyMock.replay(mockCommandContext, mockInterceptorChain);

        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_USER");
        List<GrantedAuthority> grantedAuthorities = Collections.singletonList(grantedAuthority);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken("user", "user", grantedAuthorities);

        SecurityContext securityContext = SecurityContextHolder.getContext();

        securityContext.setAuthentication(authentication);

        Assert.assertThrows(AccessDeniedException.class, () ->
                springSecurityCommandInterceptor.intercept(AdminCommand.class,
                        parameterSource, mockCommandContext, mockInterceptorChain));
    }

    @Test
    public void testAdminCommandWithAdminAuthentication() {

        EasyMock.expect(
                mockInterceptorChain.execute(
                        eq(AdminCommand.class),
                        eq(parameterSource),
                        anyObject(CommandContext.class)))
                .andReturn(null)
                .once();

        EasyMock.replay(mockCommandContext, mockInterceptorChain);

        GrantedAuthority anonymousAuthority = new SimpleGrantedAuthority("ROLE_ADMIN");
        List<GrantedAuthority> grantedAuthorities = Collections.singletonList(anonymousAuthority);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken("admin", "admin", grantedAuthorities);

        SecurityContext securityContext = SecurityContextHolder.getContext();

        securityContext.setAuthentication(authentication);

        springSecurityCommandInterceptor.intercept(AdminCommand.class,
                parameterSource, mockCommandContext, mockInterceptorChain);
    }
}
