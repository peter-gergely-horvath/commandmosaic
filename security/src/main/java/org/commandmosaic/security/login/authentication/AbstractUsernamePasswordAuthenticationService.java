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

package org.commandmosaic.security.login.authentication;

import org.commandmosaic.security.AuthenticationException;
import org.commandmosaic.security.core.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public abstract class AbstractUsernamePasswordAuthenticationService<T>
        implements UsernamePasswordAuthenticationService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private volatile String dummyEncodedPassword;

    @Override
    public final Identity authenticateUser(String userName, String password) throws AuthenticationException {

        try {
            Identity identity = null;

            Optional<T> userResult = loadUserByUsername(userName);
            if (userResult.isPresent()) {
                logger.debug("User '{}' is found", userName);
                T user = userResult.get();

                String encodedPassword = getEncodedPassword(user);

                if (checkPasswordMatches(password, encodedPassword)) {
                    logger.debug("User '{}' is authenticated successfully", userName);
                    identity = mapToIdentity(user);
                } else {
                    logger.debug("Password for user '{}' does not match stored one", userName);
                }
            } else {
                logger.debug("User '{}' is not found", userName);
                // Artificial password encoding delay in case the user is not found.
                // Used to avoid timing attacks, where an attacker can detect if a user name is valid or not
                // by measuring a longer response time in case the user is found due to password encoding
                // happening. If the user is not found, we still compare the password to a dummy value.
                if (password != null) {
                    checkPasswordMatches(password, getDummyPassword());
                }
            }

            if (identity == null) {
                throw new AuthenticationException("Invalid username or password");
            }

            return identity;

        } catch (AuthenticationException ex) {
            logger.warn("Authentication of user '{}' failed", userName);
            throw ex;
        } catch (RuntimeException ex) {
            logger.warn("Authentication of user '" + userName + "' failed due to exception", ex);
            throw new AuthenticationException("Failed to authenticate", ex);
        }
    }

    private String getDummyPassword() {
        // NOTE: dummyEncodedPassword is volatile
        if (dummyEncodedPassword == null) {
            dummyEncodedPassword = encodePassword("userNotFoundPassword");
        }

        return dummyEncodedPassword;
    }

    protected abstract Optional<T> loadUserByUsername(String userName) throws AuthenticationException;

    protected abstract String encodePassword(String clearTextPassword);

    protected abstract String getEncodedPassword(T user) throws AuthenticationException;

    protected abstract Identity mapToIdentity(T user);

    protected abstract boolean checkPasswordMatches(String password, String passwordHash);

}
