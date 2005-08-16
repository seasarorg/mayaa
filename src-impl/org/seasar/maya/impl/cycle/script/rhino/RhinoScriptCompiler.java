/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which 
 * accompanies this distribution, and is available at
 * 
 *     http://homepage3.nifty.com/seasar/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package org.seasar.maya.impl.cycle.script.rhino;

import org.seasar.maya.cycle.script.CompiledScript;
import org.seasar.maya.impl.cycle.script.AbstractScriptCompiler;
import org.seasar.maya.impl.cycle.script.LiteralScript;
import org.seasar.maya.impl.cycle.script.ScriptBlock;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class RhinoScriptCompiler extends AbstractScriptCompiler {
    
    public void putParameter(String name, String value) {
        throw new UnsupportedOperationException();
    }

    protected CompiledScript compile(
            ScriptBlock scriptBlock, Class expectedType, String sourceName, int lineno) {
        if(scriptBlock == null || expectedType == null) {
            throw new IllegalArgumentException();
        }
        String text = scriptBlock.getBlockString();
        if(scriptBlock.isLiteral()) {
            return new LiteralScript(text, expectedType);
        }
        return new RhinoCompiledScript(getScriptResolver(), 
                text, expectedType, sourceName, lineno);
    }
    
}
