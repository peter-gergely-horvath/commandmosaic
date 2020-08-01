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

 
package com.github.commandmosaic.spring;

import com.github.commandmosaic.api.Command;
import com.github.commandmosaic.api.CommandContext;
import com.github.commandmosaic.api.executor.CommandExecutor;
import com.github.commandmosaic.api.executor.ParameterSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;

import java.util.Objects;

public class SpringCommandExecutor implements CommandExecutor {

    private static final Logger log = LoggerFactory.getLogger(SpringCommandExecutor.class);

    private final InjectingBeanPostProcessor injectingBeanPostProcessor;
    private final DefaultListableBeanFactory beanFactory;

    public SpringCommandExecutor(
            InjectingBeanPostProcessor injectingBeanPostProcessor,
            BeanFactory beanFactory) {

        this.injectingBeanPostProcessor = injectingBeanPostProcessor;

        if (!(beanFactory instanceof DefaultListableBeanFactory)) {
            throw new IllegalArgumentException("DefaultListableBeanFactory is required, but was: "
                    + beanFactory.getClass());
        }
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;

    }

    @Override
    public <R, C extends Command<R>> R execute(
            Class<C> commandClass, ParameterSource parameters, CommandContext context) {

        Objects.requireNonNull(commandClass, "argument commandClass cannot be null");

        String commandClassName = commandClass.getName();
        Objects.requireNonNull(commandClassName, "commandClassName cannot be null");

        Objects.requireNonNull(beanFactory, "beanFactory is not injected: this is illegal");

        log.debug("Requested execution of command {}", commandClass);

        if (!beanFactory.containsBeanDefinition(commandClassName)) {

            log.trace("Registering bean definition of command {}", commandClass);

            GenericBeanDefinition gbd = new GenericBeanDefinition();
            gbd.setBeanClass(commandClass);
            gbd.setScope(BeanDefinition.SCOPE_PROTOTYPE);

            beanFactory.registerBeanDefinition(commandClassName, gbd);
        }

        try {
            log.trace("Putting current parameters {}", parameters);
            injectingBeanPostProcessor.putCurrentParameters(parameters);

            log.trace("Retrieving command bean from beanFactory {}", commandClassName);
            @SuppressWarnings("unchecked")
            Command<R> commandBean = (Command<R>) beanFactory.getBean(commandClassName);

            log.trace("Calling execute on target command {}", commandBean);
            return commandBean.execute(context);
        }
        finally {
            log.trace("Clearing current parameters");
            injectingBeanPostProcessor.clearCurrentParameters();
        }
    }
}
