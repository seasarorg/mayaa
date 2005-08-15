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
import org.mozilla.javascript.ScriptableObject;
import org.seasar.maya.cycle.script.resolver.ScriptResolver;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class GlobalScope extends ScriptableObject {

    private static final long serialVersionUID = -3701752195372455053L;

    private ScriptResolver _resolver;
    
    public GlobalScope(ScriptResolver resolver) {
    	if(resolver == null) {
    		throw new IllegalArgumentException();
    	}
    	_resolver = resolver;
    }
    
    public String getClassName() {
        return "GlobalScope";
    }

	public Object get(String name, Scriptable scope) {
		Object obj = _resolver.getVariable(name);
		if(ScriptResolver.UNDEFINED.equals(obj) == false) {
			return obj;
		}
		return super.get(name, scope);
	}

	public boolean has(String name, Scriptable scope) {
		Object obj = _resolver.getVariable(name);
		if(ScriptResolver.UNDEFINED.equals(obj) == false) {
			return true;
		}
		return super.has(name, scope);
	}

	public void put(String name, Scriptable scope, Object value) {
		super.put(name, scope, value);
	}
    
}
