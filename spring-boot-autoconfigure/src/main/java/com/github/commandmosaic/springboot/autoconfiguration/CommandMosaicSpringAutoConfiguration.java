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

 
package com.github.commandmosaic.springboot.autoconfiguration;

import com.github.commandmosaic.api.CommandDispatcher;
import com.github.commandmosaic.api.configuration.CommandDispatcherConfiguration;
import com.github.commandmosaic.api.conversion.TypeConversionService;
import com.github.commandmosaic.api.factory.CommandDispatcherFactory;
import com.github.commandmosaic.api.server.CommandDispatcherServer;
import com.github.commandmosaic.core.conversion.DefaultTypeConversionService;
import com.github.commandmosaic.core.server.DefaultCommandDispatcherServer;
import com.github.commandmosaic.spring.InjectingBeanPostProcessor;
import com.github.commandmosaic.spring.SpringCommandDispatcherFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class CommandMosaicSpringAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(CommandMosaicSpringAutoConfiguration.class);

    public CommandMosaicSpringAutoConfiguration() {
        log.info("CommandMosaicSpringAutoConfiguration is created");
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(CommandDispatcherConfiguration.class)
    public TypeConversionService typeConversionService(CommandDispatcherConfiguration configuration) {

        log.info("Auto-configuring TypeConversionService from configuration");
        return new DefaultTypeConversionService(configuration.getTypeConversions());
    }

    @Bean
    public InjectingBeanPostProcessor injectingBeanPostProcessor(TypeConversionService typeConversionService) {
        log.info("Auto-configuring InjectingBeanPostProcessor");
        return new InjectingBeanPostProcessor(typeConversionService);
    }

    @Bean
    @ConditionalOnBean(CommandDispatcherConfiguration.class)
    public CommandDispatcher springCommandDispatcher(CommandDispatcherFactory factory,
                                                     CommandDispatcherConfiguration config) {

        log.info("Auto-configuring CommandDispatcher from configuration: {}", config);
        return factory.getCommandDispatcher(config);
    }

    @Bean
    @ConditionalOnMissingBean
    @Lazy
    public CommandDispatcherFactory commandDispatcherFactory(InjectingBeanPostProcessor injectingBeanPostProcessor,
                                                             BeanFactory beanFactory) {

        log.info("Auto-configuring CommandDispatcherFactory");
        return new SpringCommandDispatcherFactory(injectingBeanPostProcessor, beanFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    @Lazy
    public CommandDispatcherServer commandDispatcherServer(CommandDispatcher commandDispatcher) {

        log.info("Auto-configuring CommandDispatcherServer");
        return new DefaultCommandDispatcherServer(commandDispatcher);
    }
}
