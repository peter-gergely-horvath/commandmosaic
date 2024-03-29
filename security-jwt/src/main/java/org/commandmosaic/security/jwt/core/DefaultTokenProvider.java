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

package org.commandmosaic.security.jwt.core;

import com.google.common.collect.ImmutableSet;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.commandmosaic.security.core.Identity;
import org.commandmosaic.security.core.SimpleIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

/**
 * <p>
 * The default, framework-provided implementation of {@link TokenProvider}.
 * </p>
 *
 * <p>
 * Based on JHipster Sample application TokenProvider:
 * <p>
 * https://github.com/jhipster/jhipster-sample-app/blob/master/src/main/java/io/github/jhipster/sample/security/jwt/TokenProvider.java
 * </p>
 */
public class DefaultTokenProvider implements TokenProvider {

    private static final Logger log = LoggerFactory.getLogger(DefaultTokenProvider.class);

    private static final String AUTHORITIES_KEY = "authorities";
    private static final String MULTI_VALUE_SEPARATOR = ",";

    private final Key key;

    private final long tokenValidityInMilliseconds;

    private final long tokenValidityInMillisecondsForRememberMe;


    public DefaultTokenProvider(byte[] keyBytes,
                                long tokenValidityInSeconds,
                                long tokenValidityInSecondsForRememberMe) {
        if (tokenValidityInSeconds <= 0) {
            throw new IllegalArgumentException("tokenValidityInSeconds must be a positive number");
        }
        if (tokenValidityInSecondsForRememberMe <= 0) {
            throw new IllegalArgumentException("tokenValidityInSecondsForRememberMe must be a positive number");
        }

        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.tokenValidityInMilliseconds = 1000 * tokenValidityInSeconds;
        this.tokenValidityInMillisecondsForRememberMe = 1000 * tokenValidityInSecondsForRememberMe;
    }


    @Override
    public String createToken(Identity authentication, boolean rememberMe) {
        String authorities = String.join(MULTI_VALUE_SEPARATOR, authentication.getAuthorities());

        long now = new Date().getTime();
        Date validity;
        if (rememberMe) {
            validity = new Date(now + this.tokenValidityInMillisecondsForRememberMe);
        } else {
            validity = new Date(now + this.tokenValidityInMilliseconds);
        }

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(validity)
                .compact();
    }

    @Override
    public Optional<Identity> getCallerIdentity(String token) {

        if (token == null || token.trim().isEmpty()) {
            return Optional.empty();
        }

        Claims claims;
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            log.info("Invalid JWT token.");
            log.trace("Invalid JWT token trace.", e);

            return Optional.empty();
        }

        String authoritiesMultiValueString = claims.get(AUTHORITIES_KEY).toString();

        Set<String> authoritiesSet = Arrays.stream(authoritiesMultiValueString.split(MULTI_VALUE_SEPARATOR))
                .collect(ImmutableSet.toImmutableSet());

        return Optional.of(new SimpleIdentity(claims.getSubject(), authoritiesSet));
    }
}
