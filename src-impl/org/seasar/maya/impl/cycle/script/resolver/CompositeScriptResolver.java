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
package org.seasar.maya.impl.cycle.script.resolver;

import java.util.ArrayList;
import java.util.List;

import org.seasar.maya.cycle.script.resolver.ScriptResolver;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CompositeScriptResolver implements ScriptResolver {

    private List _resolvers = new ArrayList();
    
    public void add(ScriptResolver resolver) {
        if(resolver == null) {
            throw new IllegalArgumentException();
        }
        synchronized (_resolvers) {
            _resolvers.add(resolver);
        }
    }
    
    public void setParameter(String name, String value) {
        throw new UnsupportedOperationException();
    }

    public Object getVariable(String name) {
        for(int i = 0; i < _resolvers.size(); i++) {
            ScriptResolver resolver = (ScriptResolver)_resolvers.get(i);
            Object obj = resolver.getVariable(name);
            if(UNDEFINED.equals(obj) == false) {
                return obj;
            }
        }
        return UNDEFINED;
    }

	public boolean setVariable(String name, Object value) {
        for(int i = 0; i < _resolvers.size(); i++) {
            ScriptResolver resolver = (ScriptResolver)_resolvers.get(i);
            if(resolver.setVariable(name, value)) {
                return true;
            }
        }
        return false;
	}

}
