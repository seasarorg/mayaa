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
import org.mozilla.javascript.Scriptable;
import org.seasar.maya.cycle.AttributeScope;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.script.CompiledScript;
import org.seasar.maya.engine.Engine;
import org.seasar.maya.impl.cycle.script.AbstractScriptEnvironment;
import org.seasar.maya.impl.cycle.script.LiteralScript;
import org.seasar.maya.impl.cycle.script.ScriptBlock;
import org.seasar.maya.impl.provider.UnsupportedParameterException;
import org.seasar.maya.impl.util.CycleUtil;
import org.seasar.maya.impl.util.SpecificationUtil;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class RhinoScriptEnvironment extends AbstractScriptEnvironment {

    private static Scriptable _standardObjects;
    private static ThreadLocal _parent = new ThreadLocal();
    
    public void setParameter(String name, String value) {
        throw new UnsupportedParameterException(name);
    }

    protected CompiledScript compile(
            ScriptBlock scriptBlock, String sourceName, int lineno) {
        if(scriptBlock == null) {
            throw new IllegalArgumentException();
        }
        String text = scriptBlock.getBlockString();
        if(scriptBlock.isLiteral()) {
            return new LiteralScript(text);
        }
        return new RhinoCompiledScript(text,
                scriptBlock.getBlockSign(), sourceName, lineno);
    }

    public CompiledScript compile(SourceDescriptor source, String encoding) {
        if(source == null) {
            throw new IllegalArgumentException();
        }
        return new RhinoCompiledScript(source, encoding);
    }

    public void initScope() {
        Scriptable parent = (Scriptable)_parent.get();
        if(parent == null) {
            Context cx = Context.enter(); 
            if(_standardObjects == null) {
                _standardObjects = cx.initStandardObjects(null, true);
            }
            ServiceCycle cycle = CycleUtil.getServiceCycle();
            parent = cx.getWrapFactory().wrapAsJavaObject(
                    cx, _standardObjects, cycle, ServiceCycle.class);
            Engine engine = SpecificationUtil.getEngine();
            Object model = SpecificationUtil.findSpecificationModel(engine);
            if(model != null) {
                parent = cx.getWrapFactory().wrapAsJavaObject(
                        cx, parent, model, model.getClass());
            }
            _parent.set(parent);
            Context.exit();
        }
        PageAttributeScope scope = new PageAttributeScope();
        scope.setPrototype(parent);
        scope.setParentScope(_standardObjects);
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.setPageScope(scope);
    }

    public void startScope(Object model) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        AttributeScope scope = cycle.getPageScope();
        if(scope instanceof PageAttributeScope) {
            PageAttributeScope pageScope = (PageAttributeScope)scope;
            PageAttributeScope newScope = new PageAttributeScope();
            newScope.setParentScope(pageScope);
            if(model != null) {
                Context cx = Context.enter(); 
                Scriptable prototype = cx.getWrapFactory().wrapAsJavaObject(
                        cx, _standardObjects, model, model.getClass());
                newScope.setPrototype(prototype);
                Context.exit();
            }
            cycle.setPageScope(newScope);
        } else {
            throw new IllegalStateException();
        }
    }

    public void endScope() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        AttributeScope scope = cycle.getPageScope();
        if(scope instanceof PageAttributeScope) {
            PageAttributeScope pageScope = (PageAttributeScope)scope;
            Scriptable parent = pageScope.getParentScope();
            if(parent instanceof PageAttributeScope) {
                PageAttributeScope parentScope = (PageAttributeScope)parent;
                cycle.setPageScope(parentScope);
                return;
            }
        }
        throw new IllegalStateException();
    }
    
}
