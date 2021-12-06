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


package org.commandmosaic.security.jwt.spring.autoconfiguration;

import org.commandmosaic.security.authenticator.Authenticator;
import org.commandmosaic.security.jwt.config.JwtSecurityConfiguration;
import org.commandmosaic.security.jwt.core.DefaultTokenProvider;
import org.commandmosaic.security.jwt.core.TokenProvider;
import org.commandmosaic.security.jwt.interceptor.JwtSecurityCommandInterceptor;
import org.commandmosaic.security.login.authentication.UserAuthenticationService;
import org.commandmosaic.security.login.authentication.UserNamePasswordAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityJwtSpringAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SecurityJwtSpringAutoConfiguration.class);

    public SecurityJwtSpringAutoConfiguration() {
        log.info("SecurityJwtSpringAutoConfiguration is created");
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(JwtSecurityConfiguration.class)
    public TokenProvider tokenProvider(JwtSecurityConfiguration configuration) {

        log.info("Auto-configuring TokenProvider using configuration: {}", configuration);

        return new DefaultTokenProvider(
                configuration.getJwtKey(),
                configuration.getTokenValidityInSeconds(),
                configuration.getTokenValidityInSecondsForRememberMe());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({JwtSecurityConfiguration.class, UserAuthenticationService.class})
    public Authenticator authenticator(UserAuthenticationService userAuthenticationService) {

        log.info("Auto-configuring UserNamePasswordAuthenticator using UserAuthenticationService: {}",
                userAuthenticationService);

        return new UserNamePasswordAuthenticator(userAuthenticationService);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(JwtSecurityConfiguration.class)
    public JwtSecurityCommandInterceptor jwtSecurityCommandInterceptor(
            TokenProvider tokenProvider, Authenticator authenticator) {

        log.info("Auto-configuring JwtSecurityCommandInterceptor using tokenProvider: {}, authenticator: {}",
                tokenProvider, authenticator);
        return new JwtSecurityCommandInterceptor(tokenProvider, authenticator);
    }

}
