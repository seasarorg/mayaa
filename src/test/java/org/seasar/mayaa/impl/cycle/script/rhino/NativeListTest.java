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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaAdapter;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class NativeListTest {

    private Context _cx;
    private Scriptable _scope;
    private List _list;

    @Before
    public void setUp() {
        _cx = Context.enter();
        _cx.setWrapFactory(new WrapFactoryImpl());
        _scope = _cx.initStandardObjects();
        _list = new ArrayList();
        Object obj = Context.javaToJS(_list, _scope);
        ScriptableObject.putProperty(_scope, "list", obj);
    }

    @After
    public void tearDown() {
        Context.exit();
    }

    @Test
    public void testGet1() {
        Object obj = _cx.evaluateString(_scope, "list", null, 0, null);
        Object o = JavaAdapter.convertResult(obj, Object.class);
        assertTrue(o instanceof List);
    }

    @Test
    public void testGet2() {
        _list.add("testValue");
        Object obj = _cx.evaluateString(_scope, "list[0]", null, 0, null);
        String s = (String)JavaAdapter.convertResult(obj, String.class);
        assertEquals("testValue", s);
    }

    @Test
    public void testPut1() {
        _cx.evaluateString(_scope, "list[0] = 'testValue'", null, 0, null);
        Object obj = _cx.evaluateString(_scope, "list[0]", null, 0, null);
        String s = (String)JavaAdapter.convertResult(obj, String.class);
        assertEquals("testValue", s);
    }

    @Test
    public void testPut2() {
        _cx.evaluateString(_scope, "list[3] = 'testValue'", null, 0, null);
        Object obj = _cx.evaluateString(_scope, "list[3]", null, 0, null);
        String s = (String)JavaAdapter.convertResult(obj, String.class);
        assertEquals("testValue", s);
    }

}
