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

import org.seasar.maya.cycle.Application;
import org.seasar.maya.cycle.AttributeScope;
import org.seasar.maya.cycle.Request;
import org.seasar.maya.cycle.Response;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.Session;
import org.seasar.maya.engine.Engine;
import org.seasar.maya.engine.Page;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.cycle.web.WebResponse;
import org.seasar.maya.impl.util.SpecificationUtil;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ServiceCycleImpl implements ServiceCycle {

    private static final long serialVersionUID = 5971443264903384152L;

    private Application _application;
    private Request _request;
    private Response _response;
    private Map _scopes;
    private Map _attributes;
    private SpecificationNode _node;

    public ServiceCycleImpl(Application application) {
        if (application == null) {
            throw new IllegalArgumentException();
        }
        _application = application;
        _scopes = new HashMap();
        putAttributeScope(SCOPE_APPLICATION, application);
        putAttributeScope(SCOPE_PAGE, this);
        putAttributeScope(SCOPE_IMPLICIT, new ImplicitScope());
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
    
    public void resetPageScope() {
        Object engine = _attributes.get("engine");
        _attributes.clear();
        _attributes.put("engine", engine);
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

    public void setCurrentNode(SpecificationNode node) {
        if(node == null) {
            throw new IllegalArgumentException();
        }
        _node = node;
    }    

    public SpecificationNode getCurrentNode() {
        return _node;
    }
    
    public Application getApplication() {
        return _application;
    }

    public Request getRequest() {
        if(_request == null) {
            throw new IllegalStateException();
        }
        return _request;
    }

    public void setRequest(Request request) {
        if (request == null) {
            throw new IllegalArgumentException();
        }
        _request = request;
        putAttributeScope(SCOPE_REQUEST, request);
        Session session = request.getSession();
        putAttributeScope(SCOPE_SESSION, session);
    }

    public Response getResponse() {
        if(_response == null) {
            throw new IllegalStateException();
        }
        return _response;
    }

    public void setResponse(Response response) {
        if (response == null) {
            throw new IllegalArgumentException();
        }
        _response = response;
    }

    public void forward(String relativeUrlPath) {
    	resetPageScope();
        _request.setForwardPath(relativeUrlPath);
        _response.clearBuffer();
        Engine engine = SpecificationUtil.getEngine();
        Page page = engine.getPage(_request.getPageName(), _request.getExtension());
        page.doPageRender();
    }

    public void redirect(String url) {
    	if( _response instanceof WebResponse) {
        	WebResponse webResponse = (WebResponse)_response;
   			webResponse.redirect(url);
    	}
    }
    
}
