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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.WrapDynaBean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaAdapter;
import org.mozilla.javascript.NativeJavaMethod;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.seasar.mayaa.test.TestBean;

/**
 * @author Koji Suga (Gluegent Inc.)
 */
public class NativeDynaBeanTest {

    private Context _cx;
    private Scriptable _scope;
    private TestBean _bean;
    private DynaBean _dynaBean;

    @BeforeEach
    public void setUp() {
        _cx = Context.enter();
        _cx.setWrapFactory(new WrapFactoryImpl());
        _scope = _cx.initStandardObjects();
        _bean = new TestBean("name1", "name2", true);
        _dynaBean = new WrapDynaBean(_bean);
        Object obj = Context.javaToJS(_dynaBean, _scope);
        ScriptableObject.putProperty(_scope, "bean", obj);
    }

    @AfterEach
    public void tearDown() {
        Context.exit();
    }

    @Test
    public void testGet1() {
        Object obj = _cx.evaluateString(_scope, "bean", null, 0, null);
        Object o = JavaAdapter.convertResult(obj, Object.class);
        assertTrue(o instanceof DynaBean);
    }

    @Test
    public void testGet2() {
        Object obj = _cx.evaluateString(_scope, "bean.name", null, 0, null);
        Object o = JavaAdapter.convertResult(obj, String.class);
        assertEquals("name1", o);
    }

    @Test
    public void testGet2_get() {
        Object obj = _cx.evaluateString(_scope, "bean.get('name')", null, 0, null);
        Object o = JavaAdapter.convertResult(obj, String.class);
        assertEquals("name1", o);
    }

    @Test
    public void testGet3() {
        Object obj = _cx.evaluateString(_scope, "bean.QName", null, 0, null);
        Object o = JavaAdapter.convertResult(obj, String.class);
        assertEquals("name2", o);
    }

    @Test
    public void testGet3_get() {
        Object obj = _cx.evaluateString(_scope, "bean.get('QName')", null, 0, null);
        Object o = JavaAdapter.convertResult(obj, String.class);
        assertEquals("name2", o);
    }

    @Test
    public void testGet4() {
        Object obj = _cx.evaluateString(_scope, "bean.test", null, 0, null);
        Boolean b = (Boolean) JavaAdapter.convertResult(obj, Boolean.class);
        assertTrue(b.booleanValue());
    }

    @Test
    public void testGet4_get() {
        Object obj = _cx.evaluateString(_scope, "bean.get('test')", null, 0, null);
        Boolean b = (Boolean) JavaAdapter.convertResult(obj, Boolean.class);
        assertTrue(b.booleanValue());
    }

    @Test
    public void testGet5() {
        // Boolean: getter is "getTest1()", but "isTest1()"
        try {
            _cx.evaluateString(_scope, "bean.test1", null, 0, null);
            fail("invalid");
        } catch (IllegalArgumentException expected) {
            // ok
        }
    }

    @Test
    public void testGet6() {
        Object obj = _cx.evaluateString(_scope, "bean.test2", null, 0, null);
        Boolean b = (Boolean) JavaAdapter.convertResult(obj, Boolean.class);
        assertTrue(b.booleanValue());
    }

    @Test
    public void testSet2() {
        _cx.evaluateString(_scope, "bean.name = 'newname1'", null, 0, null);
        assertEquals("newname1", _bean.getName());
    }

    @Test
    public void testSet2_set() {
        _cx.evaluateString(_scope, "bean.set('name', 'newname1')", null, 0, null);
        assertEquals("newname1", _bean.getName());
    }

    @Test
    public void testSet3() {
        _cx.evaluateString(_scope, "bean.QName = 'newname2'", null, 0, null);
        assertEquals("newname2", _bean.getQName());
    }

    @Test
    public void testSet3_set() {
        _cx.evaluateString(_scope, "bean.set('QName', 'newname2')", null, 0, null);
        assertEquals("newname2", _bean.getQName());
    }

    @Test
    public void testSet4() {
        _cx.evaluateString(_scope, "bean.test = false", null, 0, null);
        assertFalse(_bean.isTest());
    }

    @Test
    public void testSet4_set() {
        _cx.evaluateString(_scope, "bean.set('test', false)", null, 0, null);
        assertFalse(_bean.isTest());
    }

    @Test
    public void testSet6() {
        _cx.evaluateString(_scope, "bean.test2 = false", null, 0, null);
        assertFalse(_bean.getTest2().booleanValue());
    }

    @Test
    public void testSet6_set() {
        _cx.evaluateString(_scope, "bean.set('test2', false)", null, 0, null);
        assertFalse(_bean.getTest2().booleanValue());
    }

    @Test
    public void testMethod() {
        Object obj = _cx.evaluateString(_scope, "bean.dynaClass", null, 0, null);
        Object o = JavaAdapter.convertResult(obj, Object.class);
        assertTrue(o instanceof DynaClass);
    }

    @Test
    public void testMethod2() {
        Object obj = _cx.evaluateString(_scope, "bean.getDynaClass()", null, 0, null);
        Object o = JavaAdapter.convertResult(obj, Object.class);
        assertTrue(o instanceof DynaClass);
    }

    @Test
    public void testMethod3() {
        Object obj = _cx.evaluateString(_scope, "bean.remove", null, 0, null);
        Object o = JavaAdapter.convertResult(obj, Object.class);
        assertTrue(o instanceof NativeJavaMethod);
    }

}
