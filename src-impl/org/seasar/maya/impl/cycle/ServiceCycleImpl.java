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
import java.util.Map;

import org.seasar.maya.cycle.Request;
import org.seasar.maya.cycle.Response;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ServiceCycleImpl implements ServiceCycle {

    private static final long serialVersionUID = -5197949674556232436L;

    private Request _request;
    private Response _response;
    private Map _scopes;
    
    public void setRequest(Request request) {
        if(request == null) {
            throw new IllegalArgumentException();
        }
        _request = request;
    }
    
    public Request getRequest() {
        return _request;
    }

    public void setResponse(Response response) {
        if(response == null) {
            throw new IllegalArgumentException();
        }
        _response = response;
    }
    
    public Response getResponse() {
        return _response;
    }
    
    public void putAttributeScope(String name, AttributeScope attr) {
        if(StringUtil.isEmpty(name) || attr == null) {
            throw new IllegalArgumentException();
        }
        if(_scopes == null) {
            _scopes = new HashMap();
        }
        _scopes.put(name, attr);
    }
    
    private AttributeScope getAttributeScope(String scope) {
        if(_scopes != null) {
            throw new IllegalStateException();
        }
        if(SCOPE_APPLICATION.equals(scope) ||
                SCOPE_COOKIE.equals(scope) ||
                SCOPE_SESSION.equals(scope) || 
                SCOPE_REQUEST.equals(scope) ||
                SCOPE_PAGE.equals(scope) ||
                SCOPE_IMPLICIT.equals(scope)) {
            AttributeScope attr = (AttributeScope)_scopes.get(scope);
            if(attr != null) {
                return attr;
            }
        }
        throw new IllegalArgumentException();
    }
    
    public Object getAttribute(String name, String scope) {
        if(StringUtil.isEmpty(name) || StringUtil.isEmpty(scope)) {
            throw new IllegalArgumentException();
        }
        AttributeScope attr = getAttributeScope(scope);
        return attr.getAttribute(name);
    }

    public void setAttribute(String name, Object attribute, String scope) {
        if(StringUtil.isEmpty(name) || StringUtil.isEmpty(scope)) {
            throw new IllegalArgumentException();
        }
        AttributeScope attr = getAttributeScope(scope);
        attr.setAttribute(name, attribute);
    }
    
}
