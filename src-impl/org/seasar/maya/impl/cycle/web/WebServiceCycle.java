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
package org.seasar.maya.impl.cycle.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.seasar.maya.cycle.Application;
import org.seasar.maya.cycle.Request;
import org.seasar.maya.cycle.Response;
import org.seasar.maya.cycle.Session;
import org.seasar.maya.engine.Engine;
import org.seasar.maya.engine.Page;
import org.seasar.maya.impl.cycle.AbstractServiceCycle;
import org.seasar.maya.impl.util.SpecificationUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class WebServiceCycle extends AbstractServiceCycle {

    private static final long serialVersionUID = 5971443264903384152L;

    private Application _application;
    private WebRequest _request;
    private WebResponse _response;

    public WebServiceCycle(Application application) {
        if (application == null) {
            throw new IllegalArgumentException();
        }
        _application = application;
    }

    public void inithialize(
    		HttpServletRequest request, HttpServletResponse response) {
    	if(request == null || response == null) {
    		throw new IllegalArgumentException();
    	}
    	_request = new WebRequest();
    	_request.setHttpServletRequest(request);
        _response = new WebResponse();
    	_response.setHttpServletResponse(response);
    }
    
    private void check() {
    	if(_request == null || _response == null) {
    		throw new IllegalStateException();
    	}
    }
    
    public Application getApplication() {
        return _application;
    }
    
    public Request getRequest() {
    	check();
    	return _request;
    }

    public Session getSession() {
    	check();
		return _request.getSession();
	}

	public Response getResponse() {
    	check();
        return _response;
    }

    public void forward(String relativeUrlPath) {
    	check();
        _request.setForwardPath(relativeUrlPath);
        _response.clearBuffer();
        Engine engine = SpecificationUtil.getEngine();
        Page page = engine.getPage(
                engine, _request.getPageName(), _request.getExtension());
        page.doPageRender();
    }

    public void redirect(String url) {
    	check();
		_response.redirect(url);
    }
    
}
