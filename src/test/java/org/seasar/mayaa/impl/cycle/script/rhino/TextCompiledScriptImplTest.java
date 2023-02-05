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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seasar.mayaa.PositionAware;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.test.util.ManualProviderFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TextCompiledScriptImplTest {

    private PositionAware _position;

    @BeforeEach
    public void setUp() {
        ManualProviderFactory.setUp(this);
        ManualProviderFactory.SCRIPT_ENVIRONMENT.initScope();
        ManualProviderFactory.SCRIPT_ENVIRONMENT.startScope(null);
        _position = new PositionAware() {

            public void setSystemID(String systemID) {
                // no-op
            }
            public String getSystemID() {
                return null;
            }
            public void setLineNumber(int lineNumber) {
                // no-op
            }
            public int getLineNumber() {
                return 0;
            }
            public void setOnTemplate(boolean onTemplate) {
                // no-op
            }
            public boolean isOnTemplate() {
                return false;
            }
        };
    }

    @AfterEach
    public void tearDown() {
        ManualProviderFactory.tearDown();
    }

    @Test
    public void testExec1() {
        TextCompiledScriptImpl script = new TextCompiledScriptImpl(
                "var obj = { run: function() { return 'hi'; } }; obj.run();",
                _position, 1);
        Object obj = script.execute(String.class, null);
        assertTrue(obj instanceof String);
        assertEquals("hi", obj);
    }

    @Test
    public void testGetRequestAttribute() {
        CycleUtil.getRequestScope().setAttribute("foo", "bar");
        TextCompiledScriptImpl script = new TextCompiledScriptImpl(
                "request.foo",
                _position, 1);
        Object obj = script.execute(Object.class, null);
        assertTrue(obj instanceof String);
        assertEquals("bar", obj);
    }

    @Test
    public void testGetPageAttribute1() {
        CycleUtil.getPageScope().setAttribute("foo", "bar");
        TextCompiledScriptImpl script = new TextCompiledScriptImpl(
                "foo", _position, 1);
        Object obj = script.execute(Object.class, null);
        assertTrue(obj instanceof String);
        assertEquals("bar", obj);
    }

    @Test
    public void testGetPageAttribute2() {
        CycleUtil.getPageScope().setAttribute("foo", "bar");
        TextCompiledScriptImpl script = new TextCompiledScriptImpl(
                "page.foo", _position, 1);
        Object obj = script.execute(Object.class, null);
        assertTrue(obj instanceof String);
        assertEquals("bar", obj);
    }

    @Test
    public void testGetPageAttribute3() {
        TextCompiledScriptImpl prepareScript = new TextCompiledScriptImpl(
                "var foo = 'bar'", _position, 1);
        prepareScript.execute(Object.class, null);

        TextCompiledScriptImpl script = new TextCompiledScriptImpl(
                "page.foo", _position, 1);
        Object obj = script.execute(Object.class, null);
        assertTrue(obj instanceof String);
        assertEquals("bar", obj);
    }

}
