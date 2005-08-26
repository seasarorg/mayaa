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

import org.mozilla.javascript.Scriptable;
import org.seasar.maya.cycle.AttributeScope;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.script.CompiledScript;
import org.seasar.maya.impl.cycle.script.AbstractScriptCompiler;
import org.seasar.maya.impl.cycle.script.LiteralScript;
import org.seasar.maya.impl.cycle.script.ScriptBlock;
import org.seasar.maya.impl.provider.UnsupportedParameterException;
import org.seasar.maya.impl.util.CycleUtil;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class RhinoScriptCompiler extends AbstractScriptCompiler {
    
    public void setParameter(String name, String value) {
        throw new UnsupportedParameterException(name);
    }

    protected CompiledScript compile(ScriptBlock scriptBlock, 
            Class expectedType, String sourceName, int lineno) {
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

    public CompiledScript compile(
            SourceDescriptor source, String encoding, Class expectedType) {
        if(source == null || expectedType == null) {
            throw new IllegalArgumentException();
        }
        return new RhinoCompiledScript(
                getScriptResolver(), source, encoding, expectedType);
    }

    public void startScope() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        AttributeScope scope = cycle.getAttributeScope("page");
        if(scope == null || scope instanceof PageAttributeScope) {
            PageAttributeScope pageScope = (PageAttributeScope)scope;
            PageAttributeScope newScope = new PageAttributeScope();
            newScope.setParentScope(pageScope);
            cycle.putAttributeScope("page", newScope);
        } else {
            throw new IllegalStateException();
        }
    }

    public void endScope() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        AttributeScope scope = cycle.getAttributeScope("page");
        if(scope instanceof PageAttributeScope) {
            PageAttributeScope pageScope = (PageAttributeScope)scope;
            Scriptable parent = pageScope.getParentScope();
            if(parent instanceof PageAttributeScope) {
                PageAttributeScope parentScope = (PageAttributeScope)parent;
                cycle.putAttributeScope("page", parentScope);
                return;
            }
        }
        throw new IllegalStateException();
    }
    
}
