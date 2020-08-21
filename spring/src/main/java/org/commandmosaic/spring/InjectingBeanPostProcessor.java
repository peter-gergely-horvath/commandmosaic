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

 
package org.commandmosaic.spring;

import org.commandmosaic.api.Command;
import org.commandmosaic.api.conversion.TypeConversionService;
import org.commandmosaic.api.executor.ParameterSource;
import org.commandmosaic.core.parameter.ParameterInjector;
import org.commandmosaic.api.CommandDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

import java.util.Stack;

public class InjectingBeanPostProcessor implements BeanPostProcessor, PriorityOrdered {

    private static final Logger log = LoggerFactory.getLogger(InjectingBeanPostProcessor.class);

    /**
     * We store the parameters in a static {@code ThreadLocal}: we have one singleton instance
     * of InjectingBeanPostProcessor managed by Spring and an arbitrary number of worker
     * threads that use the same {@link CommandDispatcher}.
     *
     * We require a {@code Stack} in the {@code ThreadLocal}, as a command might optionally
     * dispatch further commands to complete its task.
     */
    private static final ThreadLocal<Stack<ParameterSource>> parameterStackHolder =
            ThreadLocal.withInitial(Stack::new);

    private final ParameterInjector parameterInjector;

    public InjectingBeanPostProcessor(TypeConversionService typeConversionService) {
        parameterInjector = new ParameterInjector(typeConversionService);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // this call will be executed every time a Command bean is retrieved as we use
        // the "PROTOTYPE" scope for these beans. We only care about our own Command beans.
        if(bean instanceof Command) {

            log.trace("Injecting parameters to command bean: {}", bean);

            ParameterSource currentParameters = getCurrentParameters();
            parameterInjector.processInjection(bean, currentParameters);
        }

        return bean;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    void putCurrentParameters(ParameterSource parameterSource) {
        parameterStackHolder.get().push(parameterSource);
    }


    private ParameterSource getCurrentParameters() {
        Stack<ParameterSource> parameterSources = parameterStackHolder.get();
        if (parameterSources.empty()) {
            throw new IllegalArgumentException("No current parameters set");
        }

        return parameterSources.peek();
    }

    void clearCurrentParameters() {
        Stack<?> parameterStack = parameterStackHolder.get();
        parameterStack.pop();


        if (parameterStack.isEmpty()) {
            /*
            Clear the ThreadLocal in case there are no further elements
            so that the ThreadLocal will not e.g. pin an unused ClassLoader
            after shutting down an application running in a shared container
            at the negligible cost of having to re-initialize on next use.
            */
            parameterStackHolder.remove();
        }
    }

}
