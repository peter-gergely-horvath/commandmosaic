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


package org.commandmosaic.core.factory.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.Supplier;

public final class ServiceLoaderSupport<T> {

    private static final Logger log = LoggerFactory.getLogger(ServiceLoaderSupport.class);

    private final Class<T> classToLoad;

    public ServiceLoaderSupport(Class<T> classToLoad) {
        this.classToLoad = Objects.requireNonNull(classToLoad);
    }

    public T loadSingleServiceOrGetDefault(Supplier<T> defaultSupplier) {

        Objects.requireNonNull(defaultSupplier, "argument defaultSupplier cannot be null");

        String className = classToLoad.getName();
        log.debug("Constructing new instance of {}", className);


        log.trace("Performing ServiceLoader load for: {}", className);
        ServiceLoader<T> serviceLoader = ServiceLoader.load(classToLoad);

        log.trace("Discovering available {} types", className);
        Iterator<T> iterator = serviceLoader.iterator();

        T factory;
        if (!iterator.hasNext()) {
            log.debug("No custom factory is discovered by ServiceLoader, falling back to default");
            factory = defaultSupplier.get();
        } else {
            T singleExpectedFactory = iterator.next();
            log.trace("Factory discovered: {}", singleExpectedFactory);

            if (iterator.hasNext()) {
                T unexpectedAnotherFactoryFound = iterator.next();
                log.warn("Unexpected additional factory discovered: {}", unexpectedAnotherFactoryFound);

                throw new IllegalStateException(
                        "Multiple implementations found (this is caused by misconfigured dependencies): "
                                + singleExpectedFactory.getClass() + ", " + unexpectedAnotherFactoryFound.getClass());
            }

            factory = singleExpectedFactory;
        }

        log.debug("Factory used: {}", factory.getClass());

        return factory;
    }
}
