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

import org.seasar.maya.cycle.Request;
import org.seasar.maya.cycle.Response;
import org.seasar.maya.cycle.Session;
import org.seasar.maya.impl.cycle.AbstractServiceCycle;
import org.seasar.maya.impl.engine.PageForwarded;
import org.seasar.maya.impl.provider.UnsupportedParameterException;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ServiceCycleImpl extends AbstractServiceCycle {

    private static final long serialVersionUID = 5971443264903384152L;

    private RequestImpl _request = new RequestImpl();
    private ResponseImpl _response = new ResponseImpl();
    private SessionImpl _session;

    public Request getRequest() {
    	return _request;
    }

    public Session getSession() {
        if(_session == null) {
            _session = new SessionImpl();
            _session.setUnderlyingObject(_request.getHttpSession());
        }
        return _session;
	}

	public Response getResponse() {
        return _response;
    }

    public void forward(String relativeUrlPath) {
        _request.setForwardPath(relativeUrlPath);
        _response.clearBuffer();
        throw new PageForwarded();
    }

    public void redirect(String url) {
		_response.redirect(url);
    }

    public void setParameter(String name, String value) {
        throw new UnsupportedParameterException(getClass(), name);
    }
    
}
