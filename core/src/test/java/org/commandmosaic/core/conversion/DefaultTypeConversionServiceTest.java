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

 
package org.commandmosaic.core.conversion;

import org.commandmosaic.api.conversion.TypeConversionException;
import org.commandmosaic.api.conversion.TypeConversionService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

public class DefaultTypeConversionServiceTest {

    private DefaultTypeConversionService defaultTypeConversionService;

    @Before
    public void beforeTest() {
        defaultTypeConversionService = new DefaultTypeConversionService();
    }

    @Test
    public void testConversionToShort() {

        Short expectedValue = (short) 42;
        Class<Short> targetClass = Short.class;
        assertConverted(expectedValue, "42", targetClass);
        assertConverted(expectedValue, 42, targetClass);
        assertConverted(expectedValue, (long)42, targetClass);
        assertConverted(expectedValue, 42.123d, targetClass);
        assertConverted(expectedValue, 42.999d, targetClass);

        assertConversionFails(new Timestamp(42L), targetClass);
    }

    @Test
    public void testConversionToInteger() {

        Integer expectedValue = 42;
        Class<Integer> targetClass = Integer.class;
        assertConverted(expectedValue, "42", targetClass);
        assertConverted(expectedValue, (short)42, targetClass);
        assertConverted(expectedValue, (long)42, targetClass);
        assertConverted(expectedValue, 42.123d, targetClass);
        assertConverted(expectedValue, 42.999d, targetClass);

        assertConversionFails(new Timestamp(42L), targetClass);
    }

    @Test
    public void testConversionToLong() {

        Long expectedValue = 42L;
        Class<Long> targetClass = Long.class;
        assertConverted(expectedValue, "42", targetClass);
        assertConverted(expectedValue, (short)42, targetClass);
        assertConverted(expectedValue, 42, targetClass);
        assertConverted(expectedValue, 42.123d, targetClass);
        assertConverted(expectedValue, 42.999d, targetClass);

        assertConverted(expectedValue, new Date(expectedValue), targetClass);

        assertConverted(expectedValue, new Timestamp(expectedValue), targetClass);
    }

    @Test
    public void testConversionToDouble() {

        Class<Double> targetClass = Double.class;
        assertConverted(42.123, "42.123", targetClass);
        assertConverted(42.0, (short)42, targetClass);
        assertConverted(42.0, 42, targetClass);
        assertConverted(42.0, 42L, targetClass);
        assertConverted(42.123, 42.123d, targetClass);

        assertConversionFails(new Date(42L), targetClass);
    }

    @Test
    public void testConversionToBigDecimal() {

        BigDecimal expectedValue = new BigDecimal("42.123");
        Class<BigDecimal> targetClass = BigDecimal.class;
        assertConverted(expectedValue, "42.123", targetClass);
        assertConverted(new BigDecimal(42), (short)42, targetClass);
        assertConverted(new BigDecimal(42), 42, targetClass);
        assertConverted(expectedValue, 42.123d, targetClass);

        assertConverted(new BigDecimal(42), new Date(42), targetClass);
        assertConverted(new BigDecimal(42), new Timestamp(42), targetClass);
    }

    @Test
    public void testConversionToDate() {

        Date expectedValue = new Date(42);
        Class<Date> targetClass = Date.class;

        assertConverted(expectedValue, 42L, targetClass);

        assertConversionFails("42", targetClass);
        assertConversionFails(42, targetClass);
    }

    @Test
    public void testConversionFromDate() {

        assertConverted(42L, new Date(42), Long.class);

        assertConversionFails(42, Date.class);
        assertConversionFails(new Date(42), Integer.class);
    }

    @Test
    public void testMapConversion() {

        HashMap<String, Object> map = new HashMap<>();
        map.put("foo", "bar");
        map.put("bar", 42);

        TreeMap<String, Object> expectedMap = new TreeMap<>();
        expectedMap.put("foo", "bar");
        expectedMap.put("bar", 42);


        assertConverted(expectedMap, map, TreeMap.class);
    }

    public static class FooBarPOJO {
        private String foo;
        private int bar;

        public String getFoo() {
            return foo;
        }

        public void setFoo(String foo) {
            this.foo = foo;
        }

        public int getBar() {
            return bar;
        }

        public void setBar(int bar) {
            this.bar = bar;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FooBarPOJO that = (FooBarPOJO) o;
            return bar == that.bar && Objects.equals(foo, that.foo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(foo, bar);
        }
    }

    @Test
    public void testPOJOConversion() {

        HashMap<String, Object> map = new HashMap<>();
        map.put("foo", "bar");
        map.put("bar", 42);

        FooBarPOJO fooBarPOJO = new FooBarPOJO();
        fooBarPOJO.setFoo("bar");
        fooBarPOJO.setBar(42);


        assertConverted(fooBarPOJO, map, FooBarPOJO.class);
    }




    private <T> void assertConverted(T expected, Object value, Class<T> targetClass) {
        T convertedInteger = defaultTypeConversionService.convert(value, targetClass);
        Assert.assertEquals(expected, convertedInteger);
    }

    private static  <T> void assertConverted(T expected, Object value, Class<T> targetClass,
                                             TypeConversionService defaultTypeConversionService) {
        T convertedInteger = defaultTypeConversionService.convert(value, targetClass);
        Assert.assertEquals(expected, convertedInteger);
    }

    private <T> void assertConversionFails(Object value, Class<T> targetClass) {
        assertConversionFails(value, targetClass, defaultTypeConversionService);
    }

    private static <T> void assertConversionFails(Object value, Class<T> targetClass,
                                                  TypeConversionService defaultTypeConversionService) {
        Assert.assertThrows(TypeConversionException.class, () ->
                defaultTypeConversionService.convert(value, targetClass));
    }

}
