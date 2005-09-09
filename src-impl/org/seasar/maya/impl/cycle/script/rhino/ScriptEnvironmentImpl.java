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
import org.mozilla.javascript.JavaAdapter;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrapFactory;
import org.seasar.maya.cycle.Application;
import org.seasar.maya.cycle.AttributeScope;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.script.CompiledScript;
import org.seasar.maya.engine.Engine;
import org.seasar.maya.impl.cycle.AbstractServiceCycle;
import org.seasar.maya.impl.cycle.script.AbstractScriptEnvironment;
import org.seasar.maya.impl.cycle.script.LiteralScript;
import org.seasar.maya.impl.cycle.script.ScriptBlock;
import org.seasar.maya.impl.engine.EngineImpl;
import org.seasar.maya.impl.engine.specification.SpecificationImpl;
import org.seasar.maya.impl.provider.IllegalParameterValueException;
import org.seasar.maya.impl.provider.UnsupportedParameterException;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ProviderFactory;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ScriptEnvironmentImpl extends AbstractScriptEnvironment {

    private static Scriptable _standardObjects;
    private static ThreadLocal _parent = new ThreadLocal();

    private WrapFactory _wrap;
    
    protected CompiledScript compile(
            ScriptBlock scriptBlock, String sourceName, int lineno) {
        if(scriptBlock == null) {
            throw new IllegalArgumentException();
        }
        String text = scriptBlock.getBlockString();
        if(scriptBlock.isLiteral()) {
            return new LiteralScript(text);
        }
        CompiledScriptImpl compiled = new CompiledScriptImpl(
                text, scriptBlock.getBlockSign(), sourceName, lineno);
        if(_wrap != null) {
            compiled.setWrapFactory(_wrap);
        }
        return compiled;
    }

    protected String getSourceMimeType(SourceDescriptor source) {
        if(source == null) {
            throw new IllegalArgumentException();
        }
        String systemID = source.getSystemID();
        ServiceProvider provider = ProviderFactory.getServiceProvider();
        Application application = provider.getApplication();
        return application.getMimeType(systemID);
    }

    public CompiledScript compile(SourceDescriptor source, String encoding) {
        if(source == null) {
            throw new IllegalArgumentException();
        }
        CompiledScriptImpl compiled = new CompiledScriptImpl(
                source, encoding);
        if(_wrap != null) {
            compiled.setWrapFactory(_wrap);
        }
        return compiled;
    }
    
    protected Scriptable getStandardObjects() {
        if(_standardObjects == null) {
            Context cx = Context.enter();
            _standardObjects = cx.initStandardObjects(null, true);
            Context.exit();
        }
        return _standardObjects;
    }

    protected void setModelToPrototype(Object model, Scriptable scope) {
        if(scope == null) {
            throw new IllegalArgumentException();
        }
        if(model != null) {
            Context cx = Context.enter();
            if(_wrap != null) {
                cx.setWrapFactory(_wrap);
            }
            Scriptable prototype = cx.getWrapFactory().wrapAsJavaObject(
                    cx, getStandardObjects(), model, model.getClass());
            Context.exit();
            scope.setPrototype(prototype);
        }
    }
    
    public void initScope() {
        Scriptable parent = (Scriptable)_parent.get();
        if(parent == null) {
            Context cx = Context.enter();
            if(_wrap != null) {
                cx.setWrapFactory(_wrap);
            }
            ServiceCycle cycle = AbstractServiceCycle.getServiceCycle();
            parent = cx.getWrapFactory().wrapAsJavaObject(
                    cx, getStandardObjects(), cycle, ServiceCycle.class);
            Context.exit();
            _parent.set(parent);
        }
        PageAttributeScope scope = new PageAttributeScope();
        scope.setParentScope(parent);
        Engine engine = EngineImpl.getEngine();
        Object model = SpecificationImpl.getSpecificationModel(engine);
        setModelToPrototype(model, scope);
        ServiceCycle cycle = AbstractServiceCycle.getServiceCycle();
        cycle.setPageScope(scope);
    }

    public void startScope(Object model) {
        ServiceCycle cycle = AbstractServiceCycle.getServiceCycle();
        AttributeScope scope = cycle.getPageScope();
        if(scope instanceof PageAttributeScope) {
            PageAttributeScope pageScope = (PageAttributeScope)scope;
            PageAttributeScope newPageScope = new PageAttributeScope();
            newPageScope.setParentScope(pageScope);
            setModelToPrototype(model, newPageScope);
            cycle.setPageScope(newPageScope);
        } else {
            throw new IllegalStateException();
        }
    }

    public void endScope() {
        ServiceCycle cycle = AbstractServiceCycle.getServiceCycle();
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
    
    public Object convertFromScriptObject(Object scriptObject) {
        return JavaAdapter.convertResult(scriptObject, Object.class);
    }

    public void setParameter(String name, String value) {
        if("wrapFactory".equals(name)) {
            if(StringUtil.isEmpty(value)) {
                throw new IllegalParameterValueException(getClass(), name);
            }
            Class clazz = ObjectUtil.loadClass(value, WrapFactory.class);
            _wrap = (WrapFactory)ObjectUtil.newInstance(clazz);
        } else {
            throw new UnsupportedParameterException(getClass(), name);
        }
    }
    
}
