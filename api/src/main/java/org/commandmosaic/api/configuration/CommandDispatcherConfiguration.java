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

public class CommandDispatcherConfiguration {

    private String packageName;
    private List<Class<? extends CommandInterceptor>> interceptors;
    private LinkedHashSet<TypeConversion<?,?>> typeConversions;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
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

    public static Builder builder() {
        return Builder.create();
    }

    public static final class Builder {

        private String packageName;
        private LinkedList<Class<? extends CommandInterceptor>> interceptors;
        private LinkedHashSet<TypeConversion<?,?>> typeConversions;

        private Builder() {
            // instances can only be created via the factory method
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder rootPackage(String packageName) {
            this.packageName = packageName;

            return this;
        }

        public Builder rootPackageFromClass(Class<?> clazz) {
            this.packageName = clazz.getPackage().getName();

            return this;
        }

        public Builder interceptor(
                Class<? extends CommandInterceptor> commandInterceptorClass) {

            if (this.interceptors == null) {
                this.interceptors = new LinkedList<>();
            }

            this.interceptors.addLast(commandInterceptorClass);

            return this;
        }

        public <S, T> Builder typeConverter(Class<S> sourceType, Class<T> toType, TypeConverter<S, T> converter) {
            if (typeConversions == null) {
                typeConversions = new LinkedHashSet<>();
            }

            typeConversions.add(new TypeConversion<>(sourceType, toType, converter));

            return this;
        }

        public CommandDispatcherConfiguration build() {
            if (packageName == null) {
                throw new IllegalStateException("packageName must be specified");
            }

            CommandDispatcherConfiguration configuration = new CommandDispatcherConfiguration();
            configuration.setPackageName(packageName);

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
        sb.append("packageName='").append(packageName).append('\'');
        sb.append(", interceptors=").append(interceptors);
        sb.append(", typeConversions=").append(typeConversions);
        sb.append('}');
        return sb.toString();
    }
}
