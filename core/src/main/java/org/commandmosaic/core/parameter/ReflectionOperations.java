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

 
package org.commandmosaic.core.parameter;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * <p>
 * Utility class to simplify working with refection APIs.
 * </p>
 *
 * <p>
 * Partially based on Spring ReflectionUtils.
 * </p>
 */
class ReflectionOperations {

    private static final Field[] EMPTY_FIELD_ARRAY = new Field[0];

    private final LoadingCache<Class<?>, Field[]> declaredFieldsCache = CacheBuilder.newBuilder()
            .weakKeys().softValues().build(new CacheLoader<Class<?>, Field[]>() {
                @Override
                public Field[] load(Class<?> clazz) {
                    return loadDeclaredFields(clazz);
                }
            });

    @FunctionalInterface
    public interface FieldCallback {

        /**
         * Perform an operation using the given field.
         *
         * @param field the field to operate on
         */
        void doWith(Field field) throws IllegalArgumentException, IllegalAccessException;
    }

    public void doWithLocalFields(Class<?> clazz, FieldCallback fc) {
        for (Field field : getDeclaredFields(clazz)) {
            try {
                fc.doWith(field);
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException("Not allowed to access field '" + field.getName() + "': " + ex);
            }
        }
    }

    private Field[] getDeclaredFields(Class<?> clazz) {
        Objects.requireNonNull(clazz, "Class must not be null");
        try {
            return declaredFieldsCache.get(clazz);
        } catch (ExecutionException e) {
            throw new RuntimeException("Failed to load declared fields of Class " + clazz.getName(), e);
        }
    }

    private Field[] loadDeclaredFields(Class<?> clazz) {
        try {
            Field[] declaredFields = clazz.getDeclaredFields();
            return declaredFields.length > 0 ? declaredFields : EMPTY_FIELD_ARRAY;
        } catch (Throwable ex) {
            throw new IllegalStateException("Failed to get declared fields of Class [" + clazz.getName() +
                    "] from ClassLoader [" + clazz.getClassLoader() + "]", ex);
        }
    }

    static void makeAccessible(Field field) {
        if ((!Modifier.isPublic(field.getModifiers()) ||
                !Modifier.isPublic(field.getDeclaringClass().getModifiers()) ||
                Modifier.isFinal(field.getModifiers())) && !field.isAccessible()) {

            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    field.setAccessible(true);
                    return null;
                }
            });
        }
    }
}
