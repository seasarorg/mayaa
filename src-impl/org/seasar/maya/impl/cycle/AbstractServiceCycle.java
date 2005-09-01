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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.seasar.maya.cycle.Application;
import org.seasar.maya.cycle.AttributeScope;
import org.seasar.maya.cycle.Request;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.Session;
import org.seasar.maya.engine.specification.SpecificationNode;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class AbstractServiceCycle implements ServiceCycle {

    private AttributeScope _page;
    private List _scopes;
    private SpecificationNode _originalNode;
    private SpecificationNode _injectedNode;

    public void addAttributeScope(AttributeScope attrs) {
        if(attrs == null) {
            throw new IllegalArgumentException();
        }
        if(_scopes == null) {
            _scopes = new ArrayList();
        }
        synchronized(_scopes) {
            _scopes.add(attrs);
        }
    }

    public Iterator iterateAttributeScope() {
        List list = new ArrayList();
        if(_page != null) {
            list.add(_page);
        }
        if(_scopes != null) {
            list.addAll(_scopes);
        }
        Request request = getRequest();
        if(request != null) {
            list.add(request);
        }
        Session session = getSession();
        if(session != null) {
            list.add(session);
        }
        Application application = getApplication();
        if(application != null) {
        	list.add(application);
        }
        return list.iterator();
    }

    public void setPageScope(AttributeScope page) {
        if(page == null) {
            throw new IllegalArgumentException();
        }
        _page = page;
    }
    
    public AttributeScope getPageScope() {
        if(_page == null) {
            throw new IllegalStateException();
        }
        return _page;
    }

    public void setOriginalNode(SpecificationNode originalNode) {
        if(originalNode == null) {
            throw new IllegalArgumentException();
        }
        _originalNode = originalNode;
    }    

    public SpecificationNode getOriginalNode() {
        return _originalNode;
    }
    
	public void setInjectedNode(SpecificationNode injectedNode) {
		_injectedNode = injectedNode;
	}
    
    public SpecificationNode getInjectedNode() {
		return _injectedNode;
	}
    
}
