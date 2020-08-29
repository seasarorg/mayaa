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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.seasar.mayaa.test.util.ManualProviderFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc)
 */
public class ObjectUtilTest {

    @Before
    public void setUp() {
        ManualProviderFactory.setUp(this);
        ManualProviderFactory.SCRIPT_ENVIRONMENT.initScope();
    }

    @After
    public void tearDown() {
        ManualProviderFactory.tearDown();
    }

    @Test
    public void testLoadClass1() {
        Class<?> clazz = ObjectUtil.loadClass("java.lang.String");
        assertEquals(String.class, clazz);
    }

    @Test
    public void testLoadClass2() {
        Class<?> clazz = ObjectUtil.loadClass("java.lang.String", String.class);
        assertEquals(String.class, clazz);
        try {
            ObjectUtil.loadClass("java.util.Date", String.class);
            fail();
        } catch(IllegalClassTypeException e) {
            // do nothing.
        }
    }

    @Test
    public void testLoadPrimitiveClass() {
        Class<?> clazz = ObjectUtil.loadPrimitiveClass("short");
        assertEquals(Short.TYPE, clazz);
        clazz = ObjectUtil.loadPrimitiveClass("int");
        assertEquals(Integer.TYPE, clazz);
        clazz = ObjectUtil.loadPrimitiveClass("long");
        assertEquals(Long.TYPE, clazz);
        clazz = ObjectUtil.loadPrimitiveClass("float");
        assertEquals(Float.TYPE, clazz);
        clazz = ObjectUtil.loadPrimitiveClass("double");
        assertEquals(Double.TYPE, clazz);
        clazz = ObjectUtil.loadPrimitiveClass("byte");
        assertEquals(Byte.TYPE, clazz);
        clazz = ObjectUtil.loadPrimitiveClass("char");
        assertEquals(Character.TYPE, clazz);
        clazz = ObjectUtil.loadPrimitiveClass("boolean");
        assertEquals(Boolean.TYPE, clazz);
        clazz = ObjectUtil.loadPrimitiveClass("void");
        assertEquals(Void.TYPE, clazz);
        clazz = ObjectUtil.loadPrimitiveClass("java.lang.String");
        assertNull(clazz);
    }

    @Test
    public void testGetClassSignature() {
        String signature = ObjectUtil.getClassSignature("short");
        assertEquals("short", signature);
        signature = ObjectUtil.getClassSignature("short[]");
        assertEquals("[S", signature);
        signature = ObjectUtil.getClassSignature("int[]");
        assertEquals("[I", signature);
        signature = ObjectUtil.getClassSignature("long[]");
        assertEquals("[J", signature);
        signature = ObjectUtil.getClassSignature("float[]");
        assertEquals("[F", signature);
        signature = ObjectUtil.getClassSignature("double[]");
        assertEquals("[D", signature);
        signature = ObjectUtil.getClassSignature("byte[]");
        assertEquals("[B", signature);
        signature = ObjectUtil.getClassSignature("boolean[]");
        assertEquals("[Z", signature);
        signature = ObjectUtil.getClassSignature("char[]");
        assertEquals("[C", signature);
        signature = ObjectUtil.getClassSignature("java.lang.Object");
        assertEquals("java.lang.Object", signature);
        signature = ObjectUtil.getClassSignature("java.lang.Object[]");
        assertEquals("[Ljava.lang.Object;", signature);
        signature = ObjectUtil.getClassSignature("java.lang.Object[][]");
        assertEquals("[[Ljava.lang.Object;", signature);
    }

    // public void testGetPropertyClass() {
    //     ObjectUtil.getPropertyClass(SubmitTag.class, "indexId");
    //     ObjectUtil.getPropertyClass(SubmitTag.class, "action");
    //     ObjectUtil.getPropertyClass(SubmitTag.class, "accesskey");
    //     ObjectUtil.getPropertyClass(SubmitTag.class, "alt");
    //     ObjectUtil.getPropertyClass(SubmitTag.class, "altKey");
    //     ObjectUtil.getPropertyClass(SubmitTag.class, "disabled");
    //     ObjectUtil.getPropertyClass(SubmitTag.class, "indexed");
    //     ObjectUtil.getPropertyClass(SubmitTag.class, "onblur");
    //     ObjectUtil.getPropertyClass(SubmitTag.class, "onchange");
    //     ObjectUtil.getPropertyClass(SubmitTag.class, "onclick");
    //     ObjectUtil.getPropertyClass(SubmitTag.class, "ondblclick");
    //     ObjectUtil.getPropertyClass(SubmitTag.class, "onfocus");
    //     ObjectUtil.getPropertyClass(SubmitTag.class, "onkeydown");
    //     ObjectUtil.getPropertyClass(SubmitTag.class, "onkeypress");
    //     ObjectUtil.getPropertyClass(SubmitTag.class, "onkeyup");
    //     ObjectUtil.getPropertyClass(SubmitTag.class, "onmousedown");
    //     ObjectUtil.getPropertyClass(SubmitTag.class, "onmousemove");
    //     ObjectUtil.getPropertyClass(SubmitTag.class, "onmouseout");
    //     ObjectUtil.getPropertyClass(SubmitTag.class, "onmouseover");
    //     ObjectUtil.getPropertyClass(SubmitTag.class, "onmouseup");
    //     ObjectUtil.getPropertyClass(SubmitTag.class, "property");
    //     ObjectUtil.getPropertyClass(SubmitTag.class, "style");
    //     ObjectUtil.getPropertyClass(SubmitTag.class, "styleClass");
    //     ObjectUtil.getPropertyClass(SubmitTag.class, "styleId");
    //     ObjectUtil.getPropertyClass(SubmitTag.class, "tabindex");
    //     ObjectUtil.getPropertyClass(SubmitTag.class, "title");
    //     ObjectUtil.getPropertyClass(SubmitTag.class, "titleKey");
    //     ObjectUtil.getPropertyClass(SubmitTag.class, "value");
    // }

    @Test
    public void testLoadClass() {
        Class<?> clazz = ObjectUtil.loadClass("short");
        assertEquals("short", clazz.getName());
        clazz = ObjectUtil.loadClass("short[]");
        assertEquals("[S", clazz.getName());
        clazz = ObjectUtil.loadClass("int");
        assertEquals("int", clazz.getName());
        clazz = ObjectUtil.loadClass("int[]");
        assertEquals("[I", clazz.getName());
        clazz = ObjectUtil.loadClass("long");
        assertEquals("long", clazz.getName());
        clazz = ObjectUtil.loadClass("long[]");
        assertEquals("[J", clazz.getName());
        clazz = ObjectUtil.loadClass("float[]");
        assertEquals("[F", clazz.getName());
        clazz = ObjectUtil.loadClass("double");
        assertEquals("double", clazz.getName());
        clazz = ObjectUtil.loadClass("double[]");
        assertEquals("[D", clazz.getName());
        clazz = ObjectUtil.loadClass("byte");
        assertEquals("byte", clazz.getName());
        clazz = ObjectUtil.loadClass("byte[]");
        assertEquals("[B", clazz.getName());
        clazz = ObjectUtil.loadClass("boolean");
        assertEquals("boolean", clazz.getName());
        clazz = ObjectUtil.loadClass("boolean[]");
        assertEquals("[Z", clazz.getName());
        clazz = ObjectUtil.loadClass("char");
        assertEquals("char", clazz.getName());
        clazz = ObjectUtil.loadClass("char[]");
        assertEquals("[C", clazz.getName());
        clazz = ObjectUtil.loadClass("java.lang.Object");
        assertEquals("java.lang.Object", clazz.getName());
        clazz = ObjectUtil.loadClass("java.lang.Object[]");
        assertEquals("[Ljava.lang.Object;", clazz.getName());
        clazz = ObjectUtil.loadClass("java.lang.Object[][]");
        assertEquals("[[Ljava.lang.Object;", clazz.getName());
    }

    @Test
    public void testNumberValue() {
        assertEquals(Double.valueOf(10.1),
                ObjectUtil.numberValue(Double.valueOf(10.1), null));
        assertEquals(new BigDecimal("10.1"),
                ObjectUtil.numberValue("10.1", null));
        assertEquals(new BigInteger("10"),
                ObjectUtil.numberValue("10", null));
        assertEquals(Integer.valueOf("10"),
                ObjectUtil.numberValue("10a", Integer.valueOf("10")));
        assertEquals(Integer.valueOf("10"),
                ObjectUtil.numberValue(null, Integer.valueOf("10")));
        try {
            ObjectUtil.numberValue("10a", null);
        } catch (IllegalArgumentException e) {
            assertEquals("\"10a\" is not number.", e.getMessage());
        }
        try {
            ObjectUtil.numberValue(new int[] { 1 }, null);
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testSetProperty() {
        TestBean bean = new TestBean();
        ObjectUtil.setProperty(bean, "shortArray", new short[] { 5, 6 } );
        assertEquals(5, bean.getShortArray()[0]);
        assertEquals(6, bean.getShortArray()[1]);
        ObjectUtil.setProperty(bean, "dateArray", new Date[] { new Date() });
        assertNotNull(bean.getDateArray()[0]);
        ObjectUtil.setProperty(bean, "stringArray",
                new String[][] { { "test1", "test2" }, { "test3", "test4" } });
        assertEquals("test1", bean.getStringArray()[0][0]);
        assertEquals("test2", bean.getStringArray()[0][1]);
        assertEquals("test3", bean.getStringArray()[1][0]);
        assertEquals("test4", bean.getStringArray()[1][1]);

        ObjectUtil.setProperty(bean, "QName", "testQ");
        assertEquals("testQ", bean.getQName());
    }

    @Test
    public void testConvert() {
    	Date date = new Date();
    	assertSame(date, ObjectUtil.convert(Date.class, date));
    	assertNull(ObjectUtil.convert(Date.class, null));

    	java.sql.Date sqlDate = new java.sql.Date(date.getTime());
    	assertSame(sqlDate, ObjectUtil.convert(java.sql.Date.class, sqlDate));
    	assertNull(ObjectUtil.convert(java.sql.Date.class, null));

    	java.sql.Time sqlTime = new java.sql.Time(date.getTime());
    	assertSame(sqlTime, ObjectUtil.convert(java.sql.Time.class, sqlTime));
    	assertNull(ObjectUtil.convert(java.sql.Time.class, null));

    	java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(date.getTime());
    	assertSame(sqlTimestamp, ObjectUtil.convert(java.sql.Timestamp.class, sqlTimestamp));
    	assertNull(ObjectUtil.convert(java.sql.Timestamp.class, null));

    	assertNull(ObjectUtil.convert(String.class, null));

    	assertEquals(Integer.valueOf(0), ObjectUtil.convert(int.class, null));
    	assertEquals(Long.valueOf(0), ObjectUtil.convert(long.class, null));
    	assertEquals(Float.valueOf(0), ObjectUtil.convert(float.class, null));
    	assertEquals(Double.valueOf(0), ObjectUtil.convert(double.class, null));
    	assertEquals(Integer.valueOf(0), ObjectUtil.convert(Integer.class, null));
    	assertEquals(Long.valueOf(0), ObjectUtil.convert(Long.class, null));
    	assertEquals(Float.valueOf(0), ObjectUtil.convert(Float.class, null));
    	assertEquals(Double.valueOf(0.0), ObjectUtil.convert(Double.class, null));
    	assertEquals(BigDecimal.valueOf(0), ObjectUtil.convert(BigDecimal.class, null));
    	assertEquals(BigInteger.ZERO, ObjectUtil.convert(BigInteger.class, null));
    }

    @Test
    public void testArraycopy() {
        Integer[] src0 = new Integer[0];
        Integer[] copy0 = (Integer[]) ObjectUtil.arraycopy(src0, Integer.class);
        assertNotSame(src0, copy0);
        for (int i = 0; i < src0.length; i++) {
            assertEquals("0:" + i, src0[i], copy0[i]);
        }

        Integer[] src1 = new Integer[] { Integer.valueOf(1) };
        Integer[] copy1 = (Integer[]) ObjectUtil.arraycopy(src1, Integer.class);
        assertNotSame(src1, copy1);
        for (int i = 0; i < src1.length; i++) {
            assertEquals("0:" + i, src1[i], copy1[i]);
        }

        Integer[] src3 = new Integer[] { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3) };
        Integer[] copy3 = (Integer[]) ObjectUtil.arraycopy(src3, Integer.class);
        assertNotSame(src3, copy3);
        for (int i = 0; i < src3.length; i++) {
            assertEquals("0:" + i, src3[i], copy3[i]);
        }
    }

    @Test
    public void testBooleanValue() {
        assertTrue(ObjectUtil.booleanValue("true", false));
        assertTrue(ObjectUtil.booleanValue("yes", false));
        assertTrue(ObjectUtil.booleanValue("y", false));
        assertTrue(ObjectUtil.booleanValue("on", false));
        assertTrue(ObjectUtil.booleanValue("1", false));
        assertFalse(ObjectUtil.booleanValue("false", true));
        assertFalse(ObjectUtil.booleanValue("no", true));
        assertFalse(ObjectUtil.booleanValue("n", true));
        assertFalse(ObjectUtil.booleanValue("off", true));
        assertFalse(ObjectUtil.booleanValue("0", true));

        assertTrue(ObjectUtil.booleanValue("2", true));
        assertFalse(ObjectUtil.booleanValue("3", false));
        assertTrue(ObjectUtil.booleanValue(null, true));
        assertFalse(ObjectUtil.booleanValue(null, false));

        assertTrue(ObjectUtil.booleanValue(Boolean.TRUE, true));
        assertFalse(ObjectUtil.booleanValue(Boolean.FALSE, true));

        assertTrue(ObjectUtil.booleanValue(Integer.valueOf(1), false));
        assertFalse(ObjectUtil.booleanValue(Integer.valueOf(0), true));
        assertTrue(ObjectUtil.booleanValue(Long.valueOf(1), false));
        assertFalse(ObjectUtil.booleanValue(Long.valueOf(0), true));
        assertTrue(ObjectUtil.booleanValue(Short.valueOf((short) 1), false));
        assertFalse(ObjectUtil.booleanValue(Short.valueOf((short) 0), true));
        assertTrue(ObjectUtil.booleanValue(Byte.valueOf((byte) 1), false));
        assertFalse(ObjectUtil.booleanValue(Byte.valueOf((byte) 0), true));
    }

    @Test
    public void testCanBooleanConvert() {
        assertTrue(ObjectUtil.canBooleanConvert("true"));
        assertTrue(ObjectUtil.canBooleanConvert("yes"));
        assertTrue(ObjectUtil.canBooleanConvert("y"));
        assertTrue(ObjectUtil.canBooleanConvert("on"));
        assertTrue(ObjectUtil.canBooleanConvert("1"));
        assertTrue(ObjectUtil.canBooleanConvert("false"));
        assertTrue(ObjectUtil.canBooleanConvert("no"));
        assertTrue(ObjectUtil.canBooleanConvert("n"));
        assertTrue(ObjectUtil.canBooleanConvert("off"));
        assertTrue(ObjectUtil.canBooleanConvert("0"));
        assertTrue(ObjectUtil.canBooleanConvert(Boolean.TRUE));
        assertTrue(ObjectUtil.canBooleanConvert(Boolean.FALSE));
        assertTrue(ObjectUtil.canBooleanConvert(Integer.valueOf(1)));
        assertTrue(ObjectUtil.canBooleanConvert(Integer.valueOf(0)));

        assertFalse(ObjectUtil.canBooleanConvert(Integer.valueOf(2)));
        assertFalse(ObjectUtil.canBooleanConvert("foo"));
        assertFalse(ObjectUtil.canBooleanConvert(null));
        assertFalse(ObjectUtil.canBooleanConvert(new TestBean()));
    }

    public class TestBean {
        short[] _shortArray;
        Date[] _dateArray;
        String[][] _stringArray;
        String _qName;

        public void setShortArray(short[] shortArray) {
            _shortArray = shortArray;
        }

        public short[] getShortArray() {
            return _shortArray;
        }

        public void setDateArray(Date[] dateArray) {
            _dateArray = dateArray;
        }

        public Date[] getDateArray() {
            return _dateArray;
        }

        public void setStringArray(String[][] stringArray) {
            _stringArray = stringArray;
        }

        public String[][] getStringArray() {
            return _stringArray;
        }

        public void setQName(String qName) {
            _qName = qName;
        }

        public String getQName() {
            return _qName;
        }
    }

}
