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
import org.mozilla.javascript.Scriptable;
import org.seasar.maya.cycle.script.resolver.ScriptResolver;
import org.seasar.maya.impl.cycle.script.AbstractCompiledScript;
import org.seasar.maya.impl.cycle.script.ConversionException;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class RhinoCompiledScript extends AbstractCompiledScript {

    private static final long serialVersionUID = 4793923040332838492L;
    private static ThreadLocal _scope = new ThreadLocal();
    private static Scriptable _standardObjects;
    
    private ScriptResolver _resolver;
    private Script _script;
    
    public RhinoCompiledScript(ScriptResolver resolver,
    		Script script, String text, Class expectedType) {
        super(text, expectedType);
        if(resolver == null || script == null) {
            throw new IllegalArgumentException();
        }
        _resolver = resolver;
        _script = script;
    }
    
    private Scriptable getScope() {
        Scriptable scope = (Scriptable)_scope.get();
        if(scope == null) {
            Context cx = Context.getCurrentContext();
            if(_standardObjects == null) {
                _standardObjects = cx.initStandardObjects(null, true);
            }
            scope = new GlobalScope(_resolver); 
            scope.setPrototype(_standardObjects);
            _scope.set(scope);
        }
        return scope;
    }
    
    public Object exec() {
        Object ret;
        Class expectedType = getExpectedType();
        Context cx = Context.enter();
        try {
            ret = _script.exec(cx, getScope());
        } finally {
            Context.exit();
        }
        if(expectedType == Void.class || ret == null) {
            return null;
        } else if(expectedType == String.class) {
            return ret.toString();
        } else if(expectedType.isAssignableFrom(ret.getClass())) {
            return ret;
        }
        throw new ConversionException(this, ret.getClass());
    }
    
}
