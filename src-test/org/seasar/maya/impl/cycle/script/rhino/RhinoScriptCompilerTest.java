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

import junit.framework.TestCase;

import org.seasar.maya.cycle.script.CompiledScript;
import org.seasar.maya.cycle.script.resolver.ScriptResolver;
import org.seasar.maya.impl.cycle.script.ScriptBlock;
import org.seasar.maya.impl.cycle.script.resolver.ScopedAttributeScriptResolver;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class RhinoScriptCompilerTest extends TestCase {

    private RhinoScriptCompiler _compiler;
    
    public RhinoScriptCompilerTest(String test) {
        super(test);
    }
    
    protected void setUp() {
        _compiler = new RhinoScriptCompiler();
    }
    
    public void testGetScriptResolver() {
        ScriptResolver resolver = new ScopedAttributeScriptResolver();
        _compiler.addScriptResolver(resolver);
        assertNotNull(_compiler.getScriptResolver());
    }

    public void testCompile() {
        ScriptBlock block = new ScriptBlock(
                "obj = { run: function() { return 'hi'; } }; obj.run();", false);
        CompiledScript script = _compiler.compile(block, String.class, null, 0);
        assertEquals(String.class, script.getExpectedType());
        assertEquals("obj = { run: function() { return 'hi'; } }; obj.run();", script.getText());
        assertFalse(script.isLiteral());
    }
    
}
