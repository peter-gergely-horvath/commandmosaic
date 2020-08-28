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
import org.commandmosaic.security.AuthenticationException;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.List;
import java.util.Set;


public class SpringSecurityCommandInterceptorTest {

    private SpringSecurityCommandInterceptor springSecurityCommandInterceptor;
    private CommandContext mockCommandContext;

    @Before
    public void beforeTest() {
        springSecurityCommandInterceptor = new SpringSecurityCommandInterceptor();

        mockCommandContext = EasyMock.createStrictMock(CommandContext.class);
    }

    @After
    public void afterTest() {
        EasyMock.verify(mockCommandContext);
    }

    @Test
    public void testNullAuthentication() {
        EasyMock.replay(mockCommandContext);

        SecurityContext securityContext = SecurityContextHolder.getContext();

        securityContext.setAuthentication(null);

        Assert.assertThrows(AuthenticationException.class, () ->
                springSecurityCommandInterceptor.attemptLogin(mockCommandContext));
    }

    @Test
    public void testAnonymousAuthenticationTokenAuthentication() {
        EasyMock.replay(mockCommandContext);


        GrantedAuthority anonymousAuthority = new SimpleGrantedAuthority("ROLE_anonymous");
        List<GrantedAuthority> grantedAuthorities = Collections.singletonList(anonymousAuthority);
        UserDetails user = new User("anonymous", "anonymous", grantedAuthorities);

        AnonymousAuthenticationToken anonymousAuthenticationToken =
                new AnonymousAuthenticationToken("anonymous", "anonymous", grantedAuthorities);

        SecurityContext securityContext = SecurityContextHolder.getContext();

        securityContext.setAuthentication(anonymousAuthenticationToken);

        Assert.assertThrows(AuthenticationException.class, () ->
                springSecurityCommandInterceptor.attemptLogin(mockCommandContext));
    }

    @Test
    public void testLoggedInAuthentication() {
        EasyMock.replay(mockCommandContext);


        GrantedAuthority anonymousAuthority = new SimpleGrantedAuthority("ROLE_STANDARD_USER");
        List<GrantedAuthority> grantedAuthorities = Collections.singletonList(anonymousAuthority);
        UserDetails user = new User("foobar", "foobar", grantedAuthorities);

        Authentication authentication =
                new TestingAuthenticationToken("foobar", "foobar", grantedAuthorities);

        SecurityContext securityContext = SecurityContextHolder.getContext();

        securityContext.setAuthentication(authentication);

        Set<String> roles = springSecurityCommandInterceptor.attemptLogin(mockCommandContext);

        Assert.assertEquals(1, roles.size());
        Assert.assertTrue(roles.contains("ROLE_STANDARD_USER"));
    }


}
