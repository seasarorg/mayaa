/*
 * Copyright 2004-2005 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.mayaa.impl.cycle.web;

import org.seasar.mayaa.cycle.Response;
import org.seasar.mayaa.cycle.scope.ApplicationScope;
import org.seasar.mayaa.cycle.scope.RequestScope;
import org.seasar.mayaa.cycle.scope.SessionScope;
import org.seasar.mayaa.impl.cycle.AbstractServiceCycle;
import org.seasar.mayaa.impl.engine.PageForwarded;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ServiceCycleImpl extends AbstractServiceCycle {

    private static final long serialVersionUID = 5971443264903384152L;

    private ApplicationScope _application;
    private RequestScopeImpl _request;
    private ResponseImpl _response;
    private SessionScopeImpl _session;

    public ServiceCycleImpl() {
        _request = new RequestScopeImpl();
    }
    
    public void setApplicationScope(ApplicationScope application) {
    	if(application == null) {
    		throw new IllegalArgumentException();
    	}
    	_application = application;
    }
    
    public ApplicationScope getApplicationScope() {
    	if(_application == null) {
    		_application = new ApplicationScopeImpl();
    		_application.setUnderlyingContext(getUnderlyingContext());
    	}
        return _application;
    }
    
    public RequestScope getRequestScope() {
    	return _request;
    }

    public SessionScope getSessionScope() {
        if(_session == null) {
            _session = new SessionScopeImpl();
            Object underlying = _request.getUnderlyingContext();
            _session.setUnderlyingContext(underlying);
        }
        return _session;
	}

	public Response getResponse() {
        if(_response == null) {
            _response = new ResponseImpl();
        }
        return _response;
    }

    public void forward(String forwardPath) {
        _request.setForwardPath(forwardPath);
        _response.clearBuffer();
        throw new PageForwarded();
    }

    public void redirect(String url) {
		_response.redirect(url);
    }
    
}
