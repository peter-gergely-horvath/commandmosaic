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

import org.commandmosaic.api.CommandContext;
import org.commandmosaic.security.AuthenticationException;
import org.commandmosaic.security.core.Identity;

public interface Authenticator {

    /**
     * Attempts to authenticate the caller based on information available in the
     * command context. Returns a representation of the caller's identity or <code>null</code>
     * if authentication information was not present.
     * Throws {@code AuthenticationException} if authentication
     * information was present, but authentication was not successful for any reason.
     *
     * @param commandContext the command context
     * @return a <code>Identity</code>, which represents the identity of the successfully authenticated user,
     * or <code>null</code>, if authentication information was not present
     * @throws AuthenticationException if authentication information was present, but authentication failed
     */
    Identity authenticate(CommandContext commandContext) throws AuthenticationException;
}
