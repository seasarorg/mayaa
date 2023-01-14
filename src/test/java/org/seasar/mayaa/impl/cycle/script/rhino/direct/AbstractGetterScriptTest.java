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
package org.seasar.mayaa.impl.cycle.script.rhino.direct;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seasar.mayaa.PositionAware;
import org.seasar.mayaa.cycle.scope.AttributeScope;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.cycle.scope.BindingScope;
import org.seasar.mayaa.impl.cycle.scope.HeaderScope;
import org.seasar.mayaa.impl.cycle.scope.ParamScope;
import org.seasar.mayaa.impl.cycle.script.rhino.OffsetLineRhinoException;
import org.seasar.mayaa.impl.cycle.script.rhino.TextCompiledScriptImpl;
import org.seasar.mayaa.impl.cycle.script.rhino.WalkStandardScope;
import org.seasar.mayaa.test.util.ManualProviderFactory;
import org.seasar.mayaa.test.util.TestObjectFactory.NameObject;

/**
 * @author Koji Suga (Gluegent Inc.)
 */
public class AbstractGetterScriptTest {

    protected PositionAware _position;

    @BeforeEach
    public void setUp() {
        ManualProviderFactory.setUp(this);
        ManualProviderFactory.SCRIPT_ENVIRONMENT.initScope();
        ManualProviderFactory.SCRIPT_ENVIRONMENT.startScope(null);
        ManualProviderFactory.SCRIPT_ENVIRONMENT.addAttributeScope(new ParamScope());
        ManualProviderFactory.SCRIPT_ENVIRONMENT.addAttributeScope(new HeaderScope());
        ManualProviderFactory.SCRIPT_ENVIRONMENT.addAttributeScope(new BindingScope());
        ManualProviderFactory.SCRIPT_ENVIRONMENT.addAttributeScope(new WalkStandardScope());
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
    public void testExtractLineSourcePosition() {
        String script1 = "foo.bar.baz";
        String expected1 = script1;
        int[] result1 = AbstractGetterScript.extractLineSourcePosition(script1, "baz");
        assertEquals(expected1, script1.subSequence(result1[0], result1[1]));

        String script2 = "  foo.\nbar.baz\n ";
        String expected2 = "bar.baz";
        int[] result2 = AbstractGetterScript.extractLineSourcePosition(script2, "baz");
        assertEquals(expected2, script2.subSequence(result2[0], result2[1]));

        String script3 = "  foo.\nbar.baz ";
        String expected3 = "bar.baz ";
        int[] result3 = AbstractGetterScript.extractLineSourcePosition(script3, "baz");
        assertEquals(expected3, script3.subSequence(result3[0], result3[1]));

        String script4 = "  foo.bar.baz\n ";
        String expected4 = "  foo.bar.baz";
        int[] result4 = AbstractGetterScript.extractLineSourcePosition(script4, "baz");
        assertEquals(expected4, script4.subSequence(result4[0], result4[1]));

        String script5 = "  foo.bar.\nbaz\n";
        String expected5 = "baz";
        int[] result5 = AbstractGetterScript.extractLineSourcePosition(script5, "baz");
        assertEquals(expected5, script5.subSequence(result5[0], result5[1]));
    }

    /**
     * プロパティ呼び出しのテスト(JavaScript)
     */
    @Test
    public void testGetPropertyFromUndefined() {
        String expectedMessage = "TypeError: Cannot read property \"bar\" from undefined in script=\npage. \n foo.bar \n ;";

        try {
            TextCompiledScriptImpl undef = new TextCompiledScriptImpl(
                    "page. \n foo.bar \n ;", _position, 1);
            undef.execute(null);
            fail();
        } catch (Throwable t) {
            assertTrue(t instanceof OffsetLineRhinoException);
            OffsetLineRhinoException e = (OffsetLineRhinoException) t;
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    @Test
    public void testGetPropertyFromMap() {
        AttributeScope current = CycleUtil.getCurrentPageScope();
        Map<String, String> map = new HashMap<>();
        map.put("name", "value");
        current.setAttribute("map", map);

        TestGetterScript script = new TestGetterScript("map.name", "map", "name");
        Object result = script.execute(null);
        assertEquals("value", result);
    }

    @Test
    public void testGetPropertyFromScriptObject() {
        TextCompiledScriptImpl setter = new TextCompiledScriptImpl(
                "var obj = {}; obj.name = 'value'", _position, 1);
        setter.execute(null);

        TestGetterScript script = new TestGetterScript("obj.name", "obj", "name");
        Object result = script.execute(null);
        assertEquals("value", result);
    }

    @Test
    public void testGetPropertyFromInnerDto() {
        AttributeScope current = CycleUtil.getCurrentPageScope();
        NameObject no = new NameObject("value");
        current.setAttribute("obj", no);

        TestGetterScript script = new TestGetterScript("obj.name", "obj", "name");
        Object result = script.execute(null);
        assertEquals("value", result);
    }

    @Test
    public void testGetPropertyFromPrivateInnerDto() {
        AttributeScope current = CycleUtil.getCurrentPageScope();
        PrivateNameObject no = new PrivateNameObject("value");
        current.setAttribute("obj", no);

        TestGetterScript script = new TestGetterScript("obj.name", "obj", "name");
        Object result = script.execute(null);
        assertEquals("value", result);
    }

    @Test
    public void testGetPropertyFromProtectedInnerDto() {
        AttributeScope current = CycleUtil.getCurrentPageScope();
        ProtectedNameObject no = new ProtectedNameObject("value");
        current.setAttribute("obj", no);

        TestGetterScript script = new TestGetterScript("obj.name", "obj", "name");
        Object result = script.execute(null);
        assertEquals("value", result);
    }

    @Test
    public void testGetPropertyFromDefaultInnerDto() {
        AttributeScope current = CycleUtil.getCurrentPageScope();
        DefaultNameObject no = new DefaultNameObject("value");
        current.setAttribute("obj", no);

        TestGetterScript script = new TestGetterScript("obj.name", "obj", "name");
        Object result = script.execute(null);
        assertEquals("value", result);
    }

    protected class TestGetterScript extends AbstractGetterScript {

        private static final long serialVersionUID = 1L;

        public TestGetterScript(String text,
                String attributeName, String propertyName) {
            super(text, _position, 1, null, attributeName, propertyName, null);
        }

        protected AttributeScope getScope() {
            return CycleUtil.getCurrentPageScope();
        }

    }

    private class PrivateNameObject {
        private String _name;

        public PrivateNameObject(String name) {
            _name = name;
        }

        @SuppressWarnings("unused") // JS内からの参照用
        public String getName() {
            return _name;
        }
    }

    protected class ProtectedNameObject {
        private String _name;

        public ProtectedNameObject(String name) {
            _name = name;
        }

        public String getName() {
            return _name;
        }
    }

    class DefaultNameObject {
        private String _name;

        public DefaultNameObject(String name) {
            _name = name;
        }

        public String getName() {
            return _name;
        }
    }

}
