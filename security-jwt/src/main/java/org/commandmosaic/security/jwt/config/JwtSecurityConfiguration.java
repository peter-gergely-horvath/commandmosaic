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

 
package org.commandmosaic.security.jwt.config;

import java.util.Arrays;

public class JwtSecurityConfiguration {

    private static final int DEFAULT_TOKEN_VALIDITY_IN_SECONDS = 3600; // one hour
    private static final int DEFAULT_TOKEN_VALIDITY_IN_SECONDS_FOR_REMEMBER_ME = 2592000; // 30 days

    private byte[] jwtKey;
    private long tokenValidityInSeconds;
    private long tokenValidityInSecondsForRememberMe;


    public byte[] getJwtKey() {
        return jwtKey;
    }

    public void setJwtKey(byte[] jwtKey) {
        this.jwtKey = Arrays.copyOf(jwtKey, jwtKey.length);
    }

    public long getTokenValidityInSeconds() {
        return tokenValidityInSeconds;
    }

    public void setTokenValidityInSeconds(long tokenValidityInSeconds) {
        this.tokenValidityInSeconds = tokenValidityInSeconds;
    }

    public long getTokenValidityInSecondsForRememberMe() {
        return tokenValidityInSecondsForRememberMe;
    }

    public void setTokenValidityInSecondsForRememberMe(long tokenValidityInSecondsForRememberMe) {
        this.tokenValidityInSecondsForRememberMe = tokenValidityInSecondsForRememberMe;
    }

    public static Builder builder() {
        return Builder.create();
    }

    public static final class Builder {

        private byte[] jwtKey;
        private Long tokenValidityInSeconds;
        private Long tokenValidityInSecondsForRememberMe;

        private Builder() {
            // instances can only be created via the factory method
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder setJwtKey(byte[] jwtKey) {
            this.jwtKey = jwtKey;

            return this;
        }

        public Builder setTokenValidityInSeconds(long tokenValidityInSeconds) {
            this.tokenValidityInSeconds = tokenValidityInSeconds;

            return this;
        }

        public Builder setTokenValidityInSecondsForRememberMe(long tokenValidityInSecondsForRememberMe) {
            this.tokenValidityInSecondsForRememberMe = tokenValidityInSecondsForRememberMe;

            return this;
        }

        public JwtSecurityConfiguration build() {
            if (jwtKey == null || jwtKey.length == 0) {
                throw new IllegalStateException("jwtKey must be specified and cannot be null");
            }


            JwtSecurityConfiguration configuration = new JwtSecurityConfiguration();
            configuration.setJwtKey(jwtKey);

            if (this.tokenValidityInSeconds != null) {
                configuration.setTokenValidityInSeconds(tokenValidityInSeconds);
            } else {
                configuration.setTokenValidityInSeconds(DEFAULT_TOKEN_VALIDITY_IN_SECONDS);
            }

            if (this.tokenValidityInSecondsForRememberMe != null) {
                configuration.setTokenValidityInSecondsForRememberMe(tokenValidityInSecondsForRememberMe);
            } else {
                configuration.setTokenValidityInSecondsForRememberMe(DEFAULT_TOKEN_VALIDITY_IN_SECONDS_FOR_REMEMBER_ME);
            }

            return configuration;
        }

    }



}
