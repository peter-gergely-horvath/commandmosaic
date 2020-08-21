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

 
package org.commandmosaic.http.servlet;

import org.commandmosaic.api.CommandDispatcher;
import org.commandmosaic.api.configuration.CommandDispatcherConfiguration;
import org.commandmosaic.api.factory.CommandDispatcherFactory;
import org.commandmosaic.api.interceptor.CommandInterceptor;
import org.commandmosaic.api.server.CommandDispatcherServer;
import org.commandmosaic.core.server.DefaultCommandDispatcherServer;
import org.commandmosaic.http.servlet.common.HttpServletRequestHandler;
import org.commandmosaic.plain.PlainCommandDispatcherFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CommandDispatcherServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String CONFIG_KEY_PREFIX = CommandDispatcherConfiguration.class.getName();

    public static final String COMMAND_DISPATCHER_ROOT_PACKAGE = CONFIG_KEY_PREFIX + ".rootPackage";
    public static final String COMMAND_DISPATCHER_INTERCEPTORS = CONFIG_KEY_PREFIX + ".interceptors";

    /*
     * We follow the same pattern as javax.servlet.GenericServlet#config,
     * where no external synchronisation is used.
     */
    private transient HttpServletRequestHandler httpServletRequestHandler;

    @Override
    public void init() throws ServletException {
        String rootPackageName = getInitParameter(COMMAND_DISPATCHER_ROOT_PACKAGE);
        if (rootPackageName == null || rootPackageName.trim().isEmpty()) {
            throw new ServletException("Command Dispatcher root package must be specified in Servlet Init Parameter: "
                    + COMMAND_DISPATCHER_ROOT_PACKAGE);
        }

        String commaSeparatedInterceptorClassNames = getInitParameter(COMMAND_DISPATCHER_INTERCEPTORS);

        CommandDispatcherConfiguration configuration =
                getConfiguration(rootPackageName, commaSeparatedInterceptorClassNames);

        CommandDispatcherFactory commandDispatcherFactory = getCommandDispatcherFactory();
        CommandDispatcher commandDispatcher = commandDispatcherFactory.getCommandDispatcher(configuration);

        CommandDispatcherServer dispatcherServer = new DefaultCommandDispatcherServer(commandDispatcher);

        this.httpServletRequestHandler = new HttpServletRequestHandler(dispatcherServer);
    }

    private CommandDispatcherConfiguration getConfiguration(
            String rootPackageName, String commaSeparatedInterceptorClassNames) throws ServletException {

        CommandDispatcherConfiguration.Builder configBuilder = CommandDispatcherConfiguration.builder();

        configBuilder.rootPackage(rootPackageName);

        if (commaSeparatedInterceptorClassNames != null && !commaSeparatedInterceptorClassNames.trim().isEmpty()) {
            String[] interceptorClassNames = commaSeparatedInterceptorClassNames.split(",");
            for (String interceptorClassName : interceptorClassNames) {

                String className = interceptorClassName.trim();

                try {
                    Class<?> theClass = Class.forName(className);
                    if (!(CommandInterceptor.class.isAssignableFrom(theClass))) {
                        throw new ServletException("A CommandInterceptor class must implement ["
                                + CommandInterceptor.class + "], but [" + theClass + "] does not adhere to this.");
                    }

                    @SuppressWarnings("unchecked")
                    Class<? extends CommandInterceptor> commandInterceptorClass =
                            (Class<? extends CommandInterceptor>) theClass;

                    configBuilder.interceptor(commandInterceptorClass);

                } catch (ClassNotFoundException e) {
                    throw new ServletException("Failed to load configured interceptor class: " + className, e);
                }

            }
        }

        return configBuilder.build();
    }

    /**
     * Template method to return the {@code CommandDispatcherFactory} used by this CommandDispatcherServlet
     *
     * @return {@code CommandDispatcherFactory} used by this CommandDispatcherServlet
     */
    protected CommandDispatcherFactory getCommandDispatcherFactory() {
        return PlainCommandDispatcherFactory.getInstance();
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        httpServletRequestHandler.handleRequest(request, response);
    }
}


