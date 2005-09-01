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

package org.seasar.maya.impl.cycle.script;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.seasar.maya.cycle.script.CompiledScript;
import org.seasar.maya.cycle.script.ScriptEnvironment;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class AbstractScriptEnvironment
        implements ScriptEnvironment {
    
    private String _blockSign = ScriptBlockIterator.BLOCK_SIGN_JSP;

    public void setBlockSign(String blockSign) {
        if(StringUtil.isEmpty(blockSign)) {
            throw new IllegalArgumentException();
        }
        _blockSign = blockSign;
    }
    
    protected abstract CompiledScript compile(
            ScriptBlock scriptBlock, String sourceName, int lineno);
    
    public CompiledScript compile(String script, String sourceName, int lineno) {
        if(StringUtil.isEmpty(script)) {
            throw new IllegalArgumentException();
        }
        List list = new ArrayList();
        for(Iterator it = new ScriptBlockIterator(script, _blockSign);
        	it.hasNext();) {
            ScriptBlock block = (ScriptBlock)it.next();
            list.add(compile(block, sourceName, lineno));
        }
        if(list.size() == 1) {
            return (CompiledScript)list.get(0);
    	}
	    CompiledScript[] compiled = 
            (CompiledScript[])list.toArray(new CompiledScript[list.size()]);
        return new ComplexScript(compiled);
    }

}
