/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License"); you may
 * not use this file except in compliance with the License which accompanies
 * this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.seasar.maya.impl.cycle.script.rhino;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.seasar.maya.cycle.script.resolver.ScriptResolver;
import org.seasar.maya.impl.cycle.script.resolver.CompositeScriptResolver;

import junit.framework.TestCase;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class RhinoCompiledScriptTest extends TestCase {

    private RhinoCompiledScript _script;
    
    public RhinoCompiledScriptTest(String test) {
        super(test);
    }
    
    protected void setUp() {
        ScriptResolver resolver = new CompositeScriptResolver();
        Context cx = Context.enter();
        Script scr = cx.compileString(
                "obj = { run: function() { return 'hi'; } }; obj.run();", null, 0, null);
        _script = new RhinoCompiledScript(
                resolver, scr, "obj = { run: function() { return 'hi'; } }; obj.run();", String.class);
    }
    
    public void testGetText() {
        assertEquals("obj = { run: function() { return 'hi'; } }; obj.run();", _script.getText());
    }
    
    public void testGetExpectedType() {
        assertEquals(String.class, _script.getExpectedType());
    }
    
    public void testIsLiteralText() {
        assertFalse(_script.isLiteral());        
    }
    
    public void testExec() {
        Object obj = _script.execute();
        assertTrue(obj instanceof String);
        assertEquals("hi", obj);
    }

}
