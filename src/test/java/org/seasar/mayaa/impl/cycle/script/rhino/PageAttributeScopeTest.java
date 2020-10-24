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

import java.math.BigDecimal;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.seasar.mayaa.impl.cycle.scope.ParamScope;
import org.seasar.mayaa.test.util.ManualProviderFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PageAttributeScopeTest {

    private ScriptEnvironmentImpl _scriptEnvironment;

    @Before
    public void setUp() {
        ManualProviderFactory.setUp(this);
        _scriptEnvironment = ManualProviderFactory.SCRIPT_ENVIRONMENT;
        _scriptEnvironment.addAttributeScope(new ParamScope());
        _scriptEnvironment.initScope();
        _scriptEnvironment.startScope(null);
    }

    @After
    public void tearDown() {
        ManualProviderFactory.tearDown();
    }

    @Test
    public void test() {
        Context cx = Context.enter();
        Scriptable baseScope = cx.initStandardObjects();
        PageAttributeScope scope1 = new PageAttributeScope();
        scope1.setPrototype(baseScope);
        PageAttributeScope scope2 = new PageAttributeScope();
        scope2.setParentScope(scope1);
        String script1 = "var obj = { run: function() { return 'hi'; } }; obj.run();";
        Object ret = cx.evaluateString(scope2, script1, null, 0, null);
        assertEquals("hi", Context.toString(ret));
        Context.exit();
    }

    @Test
    public void testNumber() {
        Context cx = Context.enter();
        Scriptable baseScope = cx.initStandardObjects();
        PageAttributeScope scope = new PageAttributeScope();
        scope.setPrototype(baseScope);
        scope.setAttribute("num", Integer.valueOf(3));

        String script1 = "var obj = { run: function() { return num + 1; } }; obj.run();";
        Object ret = cx.evaluateString(scope, script1, null, 0, null);
        assertEquals("4", Context.toString(ret));

        Object num = scope.getAttribute("num");
        assertEquals(Integer.class, num.getClass());

        String script2 = "num";
        Object ret2 = cx.evaluateString(scope, script2, null, 0, null);
        assertEquals("3", Context.toString(ret2));
        assertEquals(Integer.class, ret2.getClass());

        Context.exit();
    }

    @Test
    public void testNumber2() {
        Context cx = Context.enter();
        Scriptable baseScope = cx.initStandardObjects();
        PageAttributeScope scope = new PageAttributeScope();
        scope.setPrototype(baseScope);
        scope.setAttribute("num", new BigDecimal("3.12345678"));

        String script1 = "var obj = { run: function() { return num + 1.0001; } }; obj.run();";
        Object ret = cx.evaluateString(scope, script1, null, 0, null);
        assertEquals("4.12355678", Context.toString(ret));

        Object num = scope.getAttribute("num");
        assertEquals(BigDecimal.class, num.getClass());

        String script2 = "num";
        Object ret2 = cx.evaluateString(scope, script2, null, 0, null);
        assertEquals("3.12345678", Context.toString(ret2));
        assertEquals(BigDecimal.class, ret2.getClass());

        Context.exit();
    }

    @Test
    public void testNumber3_スクリプトでセット() {
        Context cx = Context.enter();
        Scriptable baseScope = cx.initStandardObjects();
        PageAttributeScope scope = new PageAttributeScope();
        scope.setPrototype(baseScope);

        String script1 = "var num = new java.lang.Integer(4);";
        cx.evaluateString(scope, script1, null, 0, null);

        Object num = scope.getAttribute("num");
        assertEquals(Integer.class, num.getClass());
        assertEquals("4", Context.toString(num));

        Context.exit();
    }

    @Test
    public void testNumber4() {
        Context cx = Context.enter();
        Scriptable baseScope = cx.initStandardObjects();
        PageAttributeScope scope = new PageAttributeScope();
        scope.setPrototype(baseScope);
        scope.setAttribute("num", Integer.valueOf(3));

        String script1 = "var obj = { run: function() { return num - 1; } }; obj.run();";
        Object ret = cx.evaluateString(scope, script1, null, 0, null);
        assertEquals("2", Context.toString(ret));

        Object num = scope.getAttribute("num");
        assertEquals(Integer.class, num.getClass());

        String script2 = "num";
        Object ret2 = cx.evaluateString(scope, script2, null, 0, null);
        assertEquals("3", Context.toString(ret2));
        assertEquals(Integer.class, ret2.getClass());

        Context.exit();
    }

}
