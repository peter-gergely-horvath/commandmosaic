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

package org.commandmosaic.security.authenticator;

import com.google.common.collect.ImmutableList;
import org.commandmosaic.api.CommandContext;
import org.commandmosaic.security.AuthenticationException;
import org.commandmosaic.security.core.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AuthenticatorChain implements Authenticator {

    private final Logger logger = LoggerFactory.getLogger(AuthenticatorChain.class);

    private final List<Authenticator> authenticators;

    public AuthenticatorChain(List<Authenticator> authenticators) {
        if (authenticators == null || authenticators.isEmpty()) {
            throw new IllegalArgumentException("at least one authenticator is required");
        }
        this.authenticators = ImmutableList.copyOf(authenticators);
    }


    @Override
    public Identity authenticate(CommandContext commandContext) throws AuthenticationException {

        for (Authenticator authenticator : authenticators) {

            logger.trace("Trying to authenticate using {}", authenticator);

            final Identity identity = authenticator.authenticate(commandContext);
            if (identity != null) {
                logger.trace("Successful authentication using: {}", authenticator);

                logger.debug("Authenticated as: {}", identity);

                return identity;
            }
        }

        logger.trace("No authenticator could authenticate: returning null as Identity");

        return null;
    }
}
