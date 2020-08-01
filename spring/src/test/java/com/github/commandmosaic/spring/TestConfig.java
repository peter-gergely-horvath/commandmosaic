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

import com.github.commandmosaic.api.CommandDispatcher;
import com.github.commandmosaic.api.conversion.TypeConversionService;
import com.github.commandmosaic.core.conversion.DefaultTypeConversionService;
import com.github.commandmosaic.api.configuration.CommandDispatcherConfiguration;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionManager;

import javax.sql.DataSource;

public class TestConfig {

    @Bean
    public CommandDispatcherConfiguration commandDispatcherConfiguration() {
        return  CommandDispatcherConfiguration.builder()
                .rootPackageFromClass(TestConfig.class)
                .build();
    }

    @Bean
    public TypeConversionService typeConversionService(CommandDispatcherConfiguration configuration) {
        return new DefaultTypeConversionService(configuration.getTypeConversions());
    }

    @Bean
    public InjectingBeanPostProcessor injectingBeanPostProcessor(TypeConversionService typeConversionService) {
        return new InjectingBeanPostProcessor(typeConversionService);
    }

    @Bean
    public CommandDispatcher commandDispatcher(CommandDispatcherConfiguration configuration,
            InjectingBeanPostProcessor injectingBeanPostProcessor, BeanFactory beanFactory) {

        SpringCommandDispatcherFactory factory =
                new SpringCommandDispatcherFactory(injectingBeanPostProcessor, beanFactory);

        return factory.getCommandDispatcher(configuration);
    }

    @Bean
    @ConfigurationProperties(prefix="test.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public TransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

}
