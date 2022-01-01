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

 
package org.commandmosaic.api.configuration;

import org.commandmosaic.api.configuration.conversion.TypeConversion;
import org.commandmosaic.api.conversion.TypeConverter;
import org.commandmosaic.api.interceptor.CommandInterceptor;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * A {@code CommandDispatcherConfiguration} describes settings used by a
 * {@code CommandDispatcher}: the root package, interceptors and custom type
 * conversions.
 */
public class CommandDispatcherConfiguration {

    private String rootPackageName;
    private List<Class<? extends CommandInterceptor>> interceptors;
    private LinkedHashSet<TypeConversion<?,?>> typeConversions;

    public String getRootPackageName() {
        return rootPackageName;
    }

    public void setRootPackageName(String rootPackageName) {
        this.rootPackageName = rootPackageName;
    }

    public List<Class<? extends CommandInterceptor>> getInterceptors() {
        return interceptors;
    }

    public void setInterceptors(List<Class<? extends CommandInterceptor>> interceptors) {
        this.interceptors = interceptors;
    }

    public LinkedHashSet<TypeConversion<?,?>> getTypeConversions() {
        return typeConversions;
    }

    public void setTypeConversions(LinkedHashSet<TypeConversion<?,?>> typeConversions) {
        this.typeConversions = typeConversions;
    }

    /**
     * Constructs a {@link CommandDispatcherConfiguration.Builder Builder},
     * which offers a fluent API for creating a {@code CommandDispatcherConfiguration}
     *
     * @return a new {@link CommandDispatcherConfiguration.Builder Builder}
     */
    public static Builder builder() {
        return Builder.create();
    }


    /**
     * A {@code Builder} is a fluent API for creating a {@code CommandDispatcherConfiguration}.
     * Once all configuration has been performed, call {@link Builder#build()}
     */
    public static final class Builder {

        private String rootPackageName;
        private LinkedList<Class<? extends CommandInterceptor>> interceptors;
        private LinkedHashSet<TypeConversion<?,?>> typeConversions;

        private Builder() {
            // instances can only be created via the factory method
        }

        /**
         * Constructs a {@link CommandDispatcherConfiguration.Builder Builder},
         * which offers a fluent API for creating a {@code CommandDispatcherConfiguration}
         *
         * @return a new {@link CommandDispatcherConfiguration.Builder Builder}
         */
        public static Builder create() {
            return new Builder();
        }

        /**
         * <p>
         * Specifies the root package for commands. The root package is always
         * one particular Java package name. The package name of the class will
         * be used as root package name.
         * </p>
         *
         * @param clazz the class, the package name of which is to be used as root package
         *
         * @return {@code this} builder (for method chaining)
         */
        public Builder rootPackageFromClass(Class<?> clazz) {
            return rootPackage(clazz.getPackage().getName());
        }

        /**
         * <p>
         * Specifies the root package for commands. The root package is always
         * one particular Java package name. Its name might be referenced as
         * one single parameter, or can be constructed from multiple arguments.
         * If multiple arguments are used, the string values are concatenated,
         * with a dot separator, resulting in one Java package name.
         * </p>
         *
         * <p>
         * For example, the following calls are equivalent, and cause
         * {@code foo.bar.commands} to be used as root package.
         *
         * <ul>
         *   <li>{@code rootPackage("foo.bar.commands")}</li>
         *   <li>{@code rootPackage("foo.bar", "commands")}</li>
         *   <li>{@code rootPackage("foo", "bar", "commands")}</li>
         * </ul>
         * </p>
         *
         *
         *
         * @param packageName the root package name (never {@code null})
         * @param relativePackageNames additional sub-package name(s), relative to {@code packageName}
         *                              (optional: might be {@code null} or empty)
         *
         * @return {@code this} builder (for method chaining)
         */
        public Builder rootPackage(String packageName, String... relativePackageNames) {

            Objects.requireNonNull(packageName, "argument packageName cannot be null");

            StringBuilder stringBuilder = new StringBuilder(packageName);

            if (relativePackageNames != null) {
                for(String relativePackage : relativePackageNames) {
                    stringBuilder.append(".");
                    stringBuilder.append(relativePackage);
                }
            }

            this.rootPackageName = stringBuilder.toString();

            return this;
        }

        /**
         * Adds the specified interceptor to the interceptor chain.
         * The interceptor will be instantiated depending on the
         * implementation of the command dispatcher. If Spring
         * based command dispatcher is used, the class might be
         * defined as a Spring bean, which will then be picked up
         * by the command dispatcher.
         *
         * @param commandInterceptorClass the class of the interceptor
         *
         * @return {@code this} builder (for method chaining)
         */
        public Builder interceptor(
                Class<? extends CommandInterceptor> commandInterceptorClass) {

            if (this.interceptors == null) {
                this.interceptors = new LinkedList<>();
            }

            this.interceptors.addLast(commandInterceptorClass);

            return this;
        }

        /**
         * Registers a custom type {@link TypeConverter} used while populating {@code @Parameter}
         * field of commands.
         *
         * @param sourceType the source type
         * @param targetType the target type
         * @param converter the {@code TypeConverter} class that performs the conversion
         *
         * @param <S> class of source type
         * @param <T> class of target type
         *
         * @return {@code this} builder (for method chaining)
         */
        public <S, T> Builder typeConverter(Class<S> sourceType, Class<T> targetType, TypeConverter<S, T> converter) {
            if (typeConversions == null) {
                typeConversions = new LinkedHashSet<>();
            }

            typeConversions.add(new TypeConversion<>(sourceType, targetType, converter));

            return this;
        }

        /**
         * Constructs a {@code CommandDispatcherConfiguration} out of the settings
         * provided via the {@link Builder}s fluent API.
         *
         * @return a new {@code CommandDispatcherConfiguration} built out of the settings provided in the builder
         */
        public CommandDispatcherConfiguration build() {
            if (rootPackageName == null) {
                throw new IllegalStateException("packageName must be specified");
            }

            CommandDispatcherConfiguration configuration = new CommandDispatcherConfiguration();
            configuration.setRootPackageName(rootPackageName);

            if (this.interceptors != null) {
                configuration.setInterceptors(interceptors);
            }

            if (this.typeConversions != null) {
                configuration.setTypeConversions(typeConversions);
            }

            return configuration;
        }

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CommandDispatcherConfiguration{");
        sb.append("packageName='").append(rootPackageName).append('\'');
        sb.append(", interceptors=").append(interceptors);
        sb.append(", typeConversions=").append(typeConversions);
        sb.append('}');
        return sb.toString();
    }
}
