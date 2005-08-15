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

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
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

    protected CompiledScript compile(ScriptBlock scriptBlock, Class expectedType) {
        if(scriptBlock == null || expectedType == null) {
            throw new IllegalArgumentException();
        }
        String expression = scriptBlock.getBlockString();
        if(scriptBlock.isLiteral()) {
            return new LiteralScript(expression, expectedType);
        }
        Context cx = Context.enter();
        // TODO ファイル名、行の設定。
        Script script = cx.compileString(expression, null, 0, null);
        Context.exit();
        return new RhinoCompiledExpression(getScriptResolver(),
        		script, expression, expectedType);
    }
    
}
