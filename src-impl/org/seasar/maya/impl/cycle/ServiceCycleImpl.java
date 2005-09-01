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
import org.seasar.maya.cycle.Response;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.Engine;
import org.seasar.maya.engine.Page;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.cycle.web.WebResponse;
import org.seasar.maya.impl.util.SpecificationUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ServiceCycleImpl implements ServiceCycle {

    private static final long serialVersionUID = 5971443264903384152L;

    private Application _application;
    private Request _request;
    private Response _response;
    private AttributeScope _page;
    private List _scopes;
    private SpecificationNode _originalNode;
    private SpecificationNode _injectedNode;

    public ServiceCycleImpl(Application application) {
        if (application == null) {
            throw new IllegalArgumentException();
        }
        _application = application;
    }

    public Application getApplication() {
        return _application;
    }

    public void setRequest(Request request) {
        if (request == null) {
            throw new IllegalArgumentException();
        }
        _request = request;
    }

    public Request getRequest() {
        if(_request == null) {
            throw new IllegalStateException();
        }
        return _request;
    }

    public void setResponse(Response response) {
        if (response == null) {
            throw new IllegalArgumentException();
        }
        _response = response;
    }

    public Response getResponse() {
        if(_response == null) {
            throw new IllegalStateException();
        }
        return _response;
    }

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
        if(_request != null) {
            list.add(_request);
            list.add(_request.getSession());
        }
        list.add(_application);
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

    public void forward(String relativeUrlPath) {
        _request.setForwardPath(relativeUrlPath);
        _response.clearBuffer();
        Engine engine = SpecificationUtil.getEngine();
        Page page = engine.getPage(
                engine, _request.getPageName(), _request.getExtension());
        page.doPageRender();
    }

    public void redirect(String url) {
    	if( _response instanceof WebResponse) {
        	WebResponse webResponse = (WebResponse)_response;
   			webResponse.redirect(url);
    	}
    }
    
}
