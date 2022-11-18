/*
 * Copyright 2004-2011 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.mayaa.impl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.junit.jupiter.api.Test;

public class IteratorUtilTest {

    /*
     * Test method for 'org.seasar.mayaa.impl.util.IteratorUtil.toIterator(Object)'
     */
    @Test
    public void testVoidToIterator() {
        Iterator<?> ite = IteratorUtil.toIterator(null);
        assertFalse(ite.hasNext());
    }

    /*
     * Test method for 'org.seasar.mayaa.impl.util.IteratorUtil.toIterator(Object)'
     */
    @Test
    public void testSingleToIterator() {
        Object o = new Object();
        Iterator<?> ite = IteratorUtil.toIterator(o);
        assertTrue(ite.hasNext());
        assertSame(o, ite.next());
        assertFalse(ite.hasNext());
    }

    /*
     * Test method for 'org.seasar.mayaa.impl.util.IteratorUtil.toIterator(Object)'
     */
    @Test
    public void testObjectArrayToIterator() {
        Object[] array = new Object[3];
        for (int i = 0; i < array.length; i++) {
            array[i] = new Object();
        }
        Iterator<?> ite = IteratorUtil.toIterator(array);
        assertTrue(ite.hasNext());
        assertSame(array[0], ite.next());
        assertTrue(ite.hasNext());
        assertSame(array[1], ite.next());
        assertTrue(ite.hasNext());
        assertSame(array[2], ite.next());
        assertFalse(ite.hasNext());
    }

    /*
     * Test method for 'org.seasar.mayaa.impl.util.IteratorUtil.toIterator(Object)'
     */
    @Test
    public void testMapToIterator() {
        Map<String, String> map = new TreeMap<>();
        map.put("1", "1val");
        map.put("2", "2val");
        map.put("3", "3val");
        @SuppressWarnings("unchecked")
        Iterator<Map.Entry<?, ?>> ite = (Iterator<Entry<?, ?>>) IteratorUtil.toIterator(map);
        assertTrue(ite.hasNext());
        Map.Entry<?,?> entry1 = ite.next();
        assertSame("1", entry1.getKey());
        assertSame("1val", entry1.getValue());
        assertTrue(ite.hasNext());
        Map.Entry<?,?> entry2 = ite.next();
        assertSame("2", entry2.getKey());
        assertSame("2val", entry2.getValue());
        assertTrue(ite.hasNext());
        Map.Entry<?,?> entry3 = ite.next();
        assertSame("3", entry3.getKey());
        assertSame("3val", entry3.getValue());
        assertFalse(ite.hasNext());
    }

    /*
     * Test method for 'org.seasar.mayaa.impl.util.IteratorUtil.toIterator(Object)'
     */
    @Test
    public void testBooleanArrayToIterator() {
        boolean[] array = new boolean[3];
        for (int i = 0; i < array.length; i++) {
            array[i] = i % 2 == 0;
        }
        Iterator<?> ite = IteratorUtil.toIterator(array);
        assertTrue(ite.hasNext());
        assertEquals(array[0], ((Boolean) ite.next()).booleanValue());
        assertTrue(ite.hasNext());
        assertEquals(array[1], ((Boolean) ite.next()).booleanValue());
        assertTrue(ite.hasNext());
        assertEquals(array[2], ((Boolean) ite.next()).booleanValue());
        assertFalse(ite.hasNext());
    }

    /*
     * Test method for 'org.seasar.mayaa.impl.util.IteratorUtil.toIterator(Object)'
     */
    @Test
    public void testByteArrayToIterator() {
        byte[] array = new byte[3];
        for (int i = 0; i < array.length; i++) {
            array[i] = (byte) i;
        }
        Iterator<?> ite = IteratorUtil.toIterator(array);
        assertTrue(ite.hasNext());
        assertEquals(array[0], ((Byte) ite.next()).byteValue());
        assertTrue(ite.hasNext());
        assertEquals(array[1], ((Byte) ite.next()).byteValue());
        assertTrue(ite.hasNext());
        assertEquals(array[2], ((Byte) ite.next()).byteValue());
        assertFalse(ite.hasNext());
    }

    /*
     * Test method for 'org.seasar.mayaa.impl.util.IteratorUtil.toIterator(Object)'
     */
    @Test
    public void testCharArrayToIterator() {
        char[] array = new char[3];
        for (int i = 0; i < array.length; i++) {
            array[i] = (char) i;
        }
        Iterator<?> ite = IteratorUtil.toIterator(array);
        assertTrue(ite.hasNext());
        assertEquals(array[0], ((Character) ite.next()).charValue());
        assertTrue(ite.hasNext());
        assertEquals(array[1], ((Character) ite.next()).charValue());
        assertTrue(ite.hasNext());
        assertEquals(array[2], ((Character) ite.next()).charValue());
        assertFalse(ite.hasNext());
    }

    /*
     * Test method for 'org.seasar.mayaa.impl.util.IteratorUtil.toIterator(Object)'
     */
    @Test
    public void testShortArrayToIterator() {
        short[] array = new short[3];
        for (int i = 0; i < array.length; i++) {
            array[i] = (short) i;
        }
        Iterator<?> ite = IteratorUtil.toIterator(array);
        assertTrue(ite.hasNext());
        assertEquals(array[0], ((Short) ite.next()).shortValue());
        assertTrue(ite.hasNext());
        assertEquals(array[1], ((Short) ite.next()).shortValue());
        assertTrue(ite.hasNext());
        assertEquals(array[2], ((Short) ite.next()).shortValue());
        assertFalse(ite.hasNext());
    }

    /*
     * Test method for 'org.seasar.mayaa.impl.util.IteratorUtil.toIterator(Object)'
     */
    @Test
    public void testIntArrayToIterator() {
        int[] array = new int[3];
        for (int i = 0; i < array.length; i++) {
            array[i] = i;
        }
        Iterator<?> ite = IteratorUtil.toIterator(array);
        assertTrue(ite.hasNext());
        assertEquals(array[0], ((Integer) ite.next()).intValue());
        assertTrue(ite.hasNext());
        assertEquals(array[1], ((Integer) ite.next()).intValue());
        assertTrue(ite.hasNext());
        assertEquals(array[2], ((Integer) ite.next()).intValue());
        assertFalse(ite.hasNext());
    }

    /*
     * Test method for 'org.seasar.mayaa.impl.util.IteratorUtil.toIterator(Object)'
     */
    @Test
    public void testLongArrayToIterator() {
        long[] array = new long[3];
        for (int i = 0; i < array.length; i++) {
            array[i] = i;
        }
        Iterator<?> ite = IteratorUtil.toIterator(array);
        assertTrue(ite.hasNext());
        assertEquals(array[0], ((Long) ite.next()).longValue());
        assertTrue(ite.hasNext());
        assertEquals(array[1], ((Long) ite.next()).longValue());
        assertTrue(ite.hasNext());
        assertEquals(array[2], ((Long) ite.next()).longValue());
        assertFalse(ite.hasNext());
    }

    /*
     * Test method for 'org.seasar.mayaa.impl.util.IteratorUtil.toIterator(Object)'
     */
    @Test
    public void testFloatArrayToIterator() {
        float[] array = new float[3];
        for (int i = 0; i < array.length; i++) {
            array[i] = i;
        }
        Iterator<?> ite = IteratorUtil.toIterator(array);
        assertTrue(ite.hasNext());
        assertEquals(array[0], ((Float) ite.next()).floatValue(), 10);
        assertTrue(ite.hasNext());
        assertEquals(array[1], ((Float) ite.next()).floatValue(), 10);
        assertTrue(ite.hasNext());
        assertEquals(array[2], ((Float) ite.next()).floatValue(), 10);
        assertFalse(ite.hasNext());
    }

    /*
     * Test method for 'org.seasar.mayaa.impl.util.IteratorUtil.toIterator(Object)'
     */
    @Test
    public void testDoubleArrayToIterator() {
        double[] array = new double[3];
        for (int i = 0; i < array.length; i++) {
            array[i] = i;
        }
        Iterator<?> ite = IteratorUtil.toIterator(array);
        assertTrue(ite.hasNext());
        assertEquals(array[0], ((Double) ite.next()).doubleValue(), 10);
        assertTrue(ite.hasNext());
        assertEquals(array[1], ((Double) ite.next()).doubleValue(), 10);
        assertTrue(ite.hasNext());
        assertEquals(array[2], ((Double) ite.next()).doubleValue(), 10);
        assertFalse(ite.hasNext());
    }

    /*
     * Test method for 'org.seasar.mayaa.impl.util.IteratorUtil.toIterator(Object)'
     */
    @Test
    public void testEnumerationToIterator() {
        Enumeration<?> enumeration = new StringTokenizer("11,22,33", ",");

        Iterator<?> ite = IteratorUtil.toIterator(enumeration);
        assertTrue(ite.hasNext());
        assertEquals("11", ite.next());
        assertTrue(ite.hasNext());
        assertEquals("22", ite.next());
        assertTrue(ite.hasNext());
        assertEquals("33", ite.next());
        assertFalse(ite.hasNext());
    }

}
