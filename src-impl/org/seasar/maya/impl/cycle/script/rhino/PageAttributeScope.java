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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.seasar.maya.cycle.AttributeScope;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PageAttributeScope extends ScriptableObject
        implements AttributeScope {

    private static final long serialVersionUID = 7746385735022710670L;

    public String getClassName() {
        return "PageScope";
    }

    public String getScopeName() {
        return "page";
    }

    public Iterator iterateAttributeNames() {
        List list = new ArrayList();
        for(Scriptable scope = this;
                scope instanceof PageAttributeScope; 
                scope = scope.getParentScope()) {
            Object[] ids = scope.getIds();
            for(int i = 0; i < ids.length; i++) {
                if(ids[i] instanceof String && list.contains(ids[i]) == false) {
                    list.add(ids[i]);
                }
            }
        }
        return list.iterator();
    }

    protected Scriptable findScope(String name) {
        for(Scriptable scope = this;
		        scope instanceof PageAttributeScope; 
		        scope = scope.getParentScope()) {
        	if(scope.has(name, this)) {
        		return scope;
        	}
        }
		return null;
	}

    public boolean hasAttribute(String name) {
        Scriptable scope = findScope(name);
        if(scope != null) {
            return true;
        }
        return false;
    }
    
	public Object getAttribute(String name) {
        Scriptable scope = findScope(name);
        if(scope != null) {
            return scope.get(name, this);
        }
        return null;
    }

    public boolean isAttributeWritable() {
		return true;
	}

	public void setAttribute(String name, Object attribute) {
        put(name, this, attribute);
    }

    public void removeAttribute(String name) {
        for(Scriptable scope = this;
                scope instanceof PageAttributeScope; 
                scope = scope.getParentScope()) {
            scope.delete(name);
        }
    }
    
}
