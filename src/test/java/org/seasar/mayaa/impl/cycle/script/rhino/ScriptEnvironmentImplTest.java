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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.Scriptable;
import org.seasar.mayaa.PositionAware;
import org.seasar.mayaa.cycle.script.CompiledScript;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.cycle.scope.ParamScope;
import org.seasar.mayaa.impl.cycle.script.ScriptBlock;
import org.seasar.mayaa.test.util.ManualProviderFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ScriptEnvironmentImplTest {

    private ScriptEnvironmentImpl _scriptEnvironment;

    private PositionAware _position;

    @Before
    public void setUp() {
        ManualProviderFactory.setUp(this);
        _scriptEnvironment = ManualProviderFactory.SCRIPT_ENVIRONMENT;
        _scriptEnvironment.addAttributeScope(new ParamScope());
        _scriptEnvironment.initScope();
        _scriptEnvironment.startScope(null);
        _position = new PositionAware() {
            private static final long serialVersionUID = 1L;

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

    @After
    public void tearDown() {
        ManualProviderFactory.tearDown();
    }

    @Test
    public void testCompile() {
        ScriptBlock block = new ScriptBlock(
                "var obj = { run: function() { return 'hi'; } }; obj.run();", false, "$");
        CompiledScript script = _scriptEnvironment.compile(block, _position, 1);
        script.setExpectedClass(String.class);
        assertEquals(String.class, script.getExpectedClass());
        assertFalse(script.isLiteral());
    }

    @Test
    public void testAddAttributeScope() {
        PageAttributeScope page =
            (PageAttributeScope)CycleUtil.getServiceCycle().getPageScope();
        Scriptable parent = page.getParentScope();
        Object param = parent.get("param", page);
        assertTrue(param instanceof ParamScope);
    }

}
