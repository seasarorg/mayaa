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

import org.seasar.maya.cycle.Application;
import org.seasar.maya.cycle.Request;
import org.seasar.maya.cycle.Response;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TestServiceCycle extends AbstractServiceCycle {
    
    private static final long serialVersionUID = 4626170364395345743L;

    private Application _application;
    private Request _request;
    private Response _response;
    
    public TestServiceCycle(Application application) {
        _application = application;
    }

    public Application getApplication() {
        return _application;
    }

    public Request getRequest() {
        return _request;
    }

    public Response getResponse() {
        return _response;
    }

    public void setRequest(Request request) {
        _request = request;
    }

    public void setResponse(Response response) {
        _response = response;
    }

    public void forward(String relativeUrlPath) {
        throw new IllegalStateException();
    }

    public void redirect(String url) {
        throw new IllegalStateException();
    }
    
}
