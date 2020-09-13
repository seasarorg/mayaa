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
package org.seasar.mayaa.impl.cycle.script.rhino;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;
import org.seasar.mayaa.test.util.TestObjectFactory.AbstractNameObject;

/**
 * @author Koji Suga (Gluegent Inc.)
 */
public class RhinoUtilTest {

    @Test
    public void testIsNumber() {
        assertTrue(RhinoUtil.isNumber(Number.class, Byte.valueOf((byte) 1)));
        assertTrue(RhinoUtil.isNumber(Number.class, Short.valueOf((short) 1)));
        assertTrue(RhinoUtil.isNumber(Number.class, Integer.valueOf(1)));
        assertTrue(RhinoUtil.isNumber(Number.class, Long.valueOf(1l)));
        assertTrue(RhinoUtil.isNumber(Number.class, Float.valueOf(1f)));
        assertTrue(RhinoUtil.isNumber(Number.class, Double.valueOf(1d)));
        assertTrue(RhinoUtil.isNumber(Number.class, new BigInteger("1")));
        assertTrue(RhinoUtil.isNumber(Number.class, new BigDecimal("1")));

        assertTrue(RhinoUtil.isNumber(Object.class, Byte.valueOf((byte) 1)));
        assertTrue(RhinoUtil.isNumber(Object.class, Short.valueOf((short) 1)));
        assertTrue(RhinoUtil.isNumber(Object.class, Integer.valueOf(1)));
        assertTrue(RhinoUtil.isNumber(Object.class, Long.valueOf(1l)));
        assertTrue(RhinoUtil.isNumber(Object.class, Float.valueOf(1f)));
        assertTrue(RhinoUtil.isNumber(Object.class, Double.valueOf(1d)));
        assertTrue(RhinoUtil.isNumber(Object.class, new BigInteger("1")));
        assertTrue(RhinoUtil.isNumber(Object.class, new BigDecimal("1")));

        assertTrue(RhinoUtil.isNumber(Byte.class, Byte.valueOf((byte) 1)));
        assertTrue(RhinoUtil.isNumber(Short.class, Short.valueOf((short) 1)));
        assertTrue(RhinoUtil.isNumber(Integer.class, Integer.valueOf(1)));
        assertTrue(RhinoUtil.isNumber(Long.class, Long.valueOf(1l)));
        assertTrue(RhinoUtil.isNumber(Float.class, Float.valueOf(1f)));
        assertTrue(RhinoUtil.isNumber(Double.class, Double.valueOf(1d)));
        assertTrue(RhinoUtil.isNumber(BigInteger.class, new BigInteger("1")));
        assertTrue(RhinoUtil.isNumber(BigDecimal.class, new BigDecimal("1")));

        assertFalse(RhinoUtil.isNumber(BigDecimal.class, Byte.valueOf((byte) 1)));
        assertFalse(RhinoUtil.isNumber(Byte.class, Short.valueOf((short) 1)));
        assertFalse(RhinoUtil.isNumber(Short.class, Integer.valueOf(1)));
        assertFalse(RhinoUtil.isNumber(Integer.class, Long.valueOf(1l)));
        assertFalse(RhinoUtil.isNumber(Long.class, Float.valueOf(1f)));
        assertFalse(RhinoUtil.isNumber(Float.class, Double.valueOf(1d)));
        assertFalse(RhinoUtil.isNumber(Double.class, new BigInteger("1")));
        assertFalse(RhinoUtil.isNumber(BigInteger.class, new BigDecimal("1")));
    }

    @Test
    public void testGetSimplePropertyPublic() {
        AbstractNameObject bean = AbstractNameObject.createPublicInstance("myname", "myqname");

        assertEquals(bean.getName(), RhinoUtil.getSimpleProperty(bean, "name"));
        assertEquals(bean.getQName(), RhinoUtil.getSimpleProperty(bean, "QName"));
        assertEquals(bean.getQName(), RhinoUtil.getSimpleProperty(bean, "qName"));
        assertEquals(Boolean.TRUE, RhinoUtil.getSimpleProperty(bean, "myName"));
        assertEquals(Boolean.TRUE, RhinoUtil.getSimpleProperty(bean, "yourName"));
        assertEquals(bean.publicName, RhinoUtil.getSimpleProperty(bean, "publicName"));
        try {
            RhinoUtil.getSimpleProperty(bean, "qname");
            fail();
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof NoSuchMethodException);
        }
        try {
            RhinoUtil.getSimpleProperty(bean, "notName");
            fail();
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof NoSuchMethodException);
        }
        try {
            RhinoUtil.getSimpleProperty(bean, "privateName");
            fail();
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof NoSuchMethodException);
        }
        try {
            RhinoUtil.getSimpleProperty(bean, "protectedName");
            fail();
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof NoSuchMethodException);
        }
        try {
            RhinoUtil.getSimpleProperty(bean, "defaultName");
            fail();
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof NoSuchMethodException);
        }
    }

    @Test
    public void testGetSimplePropertyPrivate() {
        AbstractNameObject bean = AbstractNameObject.createPrivateInstance("myname", "myqname");

        assertEquals(bean.getName(), RhinoUtil.getSimpleProperty(bean, "name"));
        assertEquals(bean.getQName(), RhinoUtil.getSimpleProperty(bean, "QName"));
        assertEquals(bean.getQName(), RhinoUtil.getSimpleProperty(bean, "qName"));
        try {
            RhinoUtil.getSimpleProperty(bean, "qname");
            fail();
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof NoSuchMethodException);
        }
    }

    @Test
    public void testGetSimplePropertyProtected() {
        AbstractNameObject bean = AbstractNameObject.createProtectedInstance("myname", "myqname");

        assertEquals(bean.getName(), RhinoUtil.getSimpleProperty(bean, "name"));
        assertEquals(bean.getQName(), RhinoUtil.getSimpleProperty(bean, "QName"));
        assertEquals(bean.getQName(), RhinoUtil.getSimpleProperty(bean, "qName"));
        try {
            RhinoUtil.getSimpleProperty(bean, "qname");
            fail();
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof NoSuchMethodException);
        }
    }

    @Test
    public void testGetSimplePropertyDefault() {
        AbstractNameObject bean = AbstractNameObject.createDefaultInstance("myname", "myqname");

        assertEquals(bean.getName(), RhinoUtil.getSimpleProperty(bean, "name"));
        assertEquals(bean.getQName(), RhinoUtil.getSimpleProperty(bean, "QName"));
        assertEquals(bean.getQName(), RhinoUtil.getSimpleProperty(bean, "qName"));
        try {
            RhinoUtil.getSimpleProperty(bean, "qname");
            fail();
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof NoSuchMethodException);
        }
    }

}
