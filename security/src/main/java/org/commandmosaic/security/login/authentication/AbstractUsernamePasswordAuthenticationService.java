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

package org.commandmosaic.security.login.authentication;

import org.commandmosaic.security.AuthenticationException;
import org.commandmosaic.security.core.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public abstract class AbstractUsernamePasswordAuthenticationService<T>
        implements UsernamePasswordAuthenticationService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String dummyEncodedPassword;

    protected AbstractUsernamePasswordAuthenticationService(Function<String, String> passwordEncodeFunction) {
        Objects.requireNonNull(passwordEncodeFunction, "argument passwordEncodeFunction cannot be null");
        this.dummyEncodedPassword = passwordEncodeFunction.apply("userNotFoundPassword");
    }

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
                // to avoid timing attacks, even if the user is not found,
                // we still compare the password to this dummy value
                if (password != null) {
                    checkPasswordMatches(password, dummyEncodedPassword);
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

    protected abstract Optional<T> loadUserByUsername(String userName) throws AuthenticationException;

    protected abstract String getEncodedPassword(T user) throws AuthenticationException;

    protected abstract Identity mapToIdentity(T user);

    protected abstract boolean checkPasswordMatches(String password, String passwordHash);

}
