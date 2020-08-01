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

import com.github.commandmosaic.api.configuration.CommandDispatcherConfiguration;
import com.github.commandmosaic.api.conversion.TypeConversionService;
import com.github.commandmosaic.api.executor.CommandExecutor;
import com.github.commandmosaic.api.interceptor.CommandInterceptor;
import com.github.commandmosaic.core.factory.AbstractCommandDispatcherFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

public class SpringCommandDispatcherFactory extends AbstractCommandDispatcherFactory {

    private static final Logger log = LoggerFactory.getLogger(SpringCommandDispatcherFactory.class);

    private final InjectingBeanPostProcessor injectingBeanPostProcessor;
    private final BeanFactory beanFactory;

    public SpringCommandDispatcherFactory(InjectingBeanPostProcessor injectingBeanPostProcessor,
                                          BeanFactory beanFactory) {

        this.injectingBeanPostProcessor = injectingBeanPostProcessor;
        this.beanFactory = beanFactory;
    }

    @Override
    protected CommandExecutor getCommandExecutor(CommandDispatcherConfiguration configuration) {
        // TypeConversionService is injected into InjectingBeanPostProcessor already!
        return new SpringCommandExecutor(injectingBeanPostProcessor, beanFactory);
    }

    @Override
    protected CommandExecutor getCommandExecutor(TypeConversionService typeConversionService) {
        throw new IllegalStateException("This method should not be called");
    }

    @Override
    protected CommandInterceptor getCommandInterceptor(Class<? extends CommandInterceptor> commandInterceptorClass) {
        CommandInterceptor interceptorInstance;
        try {
            log.debug("Trying lo get CommandInterceptor from Spring BeanFactory: {}", commandInterceptorClass);
            interceptorInstance = beanFactory.getBean(commandInterceptorClass);
        } catch (NoSuchBeanDefinitionException ex) {
            log.debug("Falling back to default lookup, as CommandInterceptor was not found in Spring BeanFactory: {}",
                    commandInterceptorClass);
            interceptorInstance = super.getCommandInterceptor(commandInterceptorClass);
        }
        return interceptorInstance;
    }


}
