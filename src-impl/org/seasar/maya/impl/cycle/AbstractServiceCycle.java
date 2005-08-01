/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 *
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which
 * accompanies this distribution, and is available at
 *
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.seasar.maya.impl.cycle;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.seasar.maya.cycle.AttributeScope;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class AbstractServiceCycle implements ServiceCycle {

    private Map _scopes;
    private Map _attributes;
    
    public AbstractServiceCycle() {
        _scopes = new HashMap();
    	putAttributeScope(SCOPE_IMPLICIT, new ImplicitScope(this));
        putAttributeScope(SCOPE_PAGE, this);
    }
    
    public String getScopeName() {
        return SCOPE_PAGE;
    }

    public boolean hasAttributeScope(String scope) {
        if(StringUtil.isEmpty(scope)) {
            scope = SCOPE_PAGE;
        }
        return _scopes.containsKey(scope);
    }

    public AttributeScope getAttributeScope(String scope) {
        if(StringUtil.isEmpty(scope)) {
            scope = SCOPE_PAGE;
        }
        AttributeScope attr = (AttributeScope)_scopes.get(scope);
        if(attr != null) {
            return attr;
        }
        throw new IllegalArgumentException();
    }

    public void putAttributeScope(String scope, AttributeScope attrScope) {
        if(StringUtil.isEmpty(scope) || attrScope == null) {
            throw new IllegalArgumentException();
        }
        _scopes.put(scope, attrScope);
    }

    public Iterator iterateAttributeNames() {
        return _attributes.keySet().iterator();
    }

    public Object getAttribute(String name) {
        if(StringUtil.isEmpty(name)) {
            return null;
        }
        if(_attributes != null) {
            return _attributes.get(name);
        }
        return null;
    }

    public void setAttribute(String name, Object attribute) {
        if(StringUtil.isEmpty(name)) {
            return;
        }
        if(_attributes == null) {
            _attributes = new HashMap();
        }
        _attributes.put(name, attribute);
    }
    
    public void removeAttribute(String name) {
        if(StringUtil.isEmpty(name)) {
            return;
        }
        _attributes.remove(name);
    }    
    
}
