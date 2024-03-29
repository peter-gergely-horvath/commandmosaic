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

package org.commandmosaic.security.web;

import org.commandmosaic.api.server.CommandDispatcherServer;
import org.commandmosaic.http.servlet.common.DefaultHttpServletTransport;
import org.commandmosaic.security.AccessDeniedException;
import org.commandmosaic.security.AuthenticationException;

import javax.servlet.http.HttpServletResponse;

public class SecurityAwareHttpServletTransport extends DefaultHttpServletTransport {

    public SecurityAwareHttpServletTransport(CommandDispatcherServer commandDispatcherServer) {
        super(commandDispatcherServer);
    }

    protected void onFailure(HttpServletResponse httpServletResponse, Throwable failure) {

        if (failure instanceof AuthenticationException) {
            httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        } else if (failure instanceof AccessDeniedException) {
            httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);

        } else {
            super.onFailure(httpServletResponse, failure);

        }
    }
}
