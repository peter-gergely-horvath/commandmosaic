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

 
package com.github.commandmosaic.spring.container;

import com.github.commandmosaic.api.CommandDispatcher;
import com.github.commandmosaic.core.server.DefaultCommandDispatcherServer;
import org.springframework.beans.factory.BeanFactory;

/**
 * <p>
 * A {@code CommandDispatcherServer}, which wraps a Spring Framework {@code BeanFactory}.
 * Used as a bridge in cases, where the Spring context (e.g. a Spring Boot Application)
 * is created and managed by our framework.
 * </p>
 *
 * <p>
 * NOTE: you do NOT need this class when working with Spring Framework or Spring Boot.
 * Only use in cases, where the Spring Context is bootstrapped by non-Spring application code.
 * For example: AWS Lambda Request Handler starting a Spring Boot application.
 * Mainly intended for internal use within the framework and not by end user code.
 * </p>
 */
public class SpringContainerCommandDispatcherServer extends DefaultCommandDispatcherServer {

    private final BeanFactory beanFactory;

    public SpringContainerCommandDispatcherServer(BeanFactory beanFactory) {
        super(beanFactory.getBean(CommandDispatcher.class));
        this.beanFactory = beanFactory;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SpringContainerCommandDispatcherServer{");
        sb.append("beanFactory=").append(beanFactory);
        sb.append('}');
        return sb.toString();
    }
}
