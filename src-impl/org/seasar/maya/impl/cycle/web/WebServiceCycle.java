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
    private Request _request;
    private Response _response;

    public WebServiceCycle(Application application) {
        if (application == null) {
            throw new IllegalArgumentException();
        }
        _application = application;
        putAttributeScope(SCOPE_APPLICATION, application);
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
        _request.setForwardPath(relativeUrlPath);
        _response.clearBuffer();
        Engine engine = SpecificationUtil.getEngine(this);
        Page page = engine.getPage(_request.getPageName(), _request.getExtension());
        page.doPageRender(this);
    }

    public void redirect(String url) {
        // FIXME implementing
        throw new UnsupportedOperationException();
    }
    
}
