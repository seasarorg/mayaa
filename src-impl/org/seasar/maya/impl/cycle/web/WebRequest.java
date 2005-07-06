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

import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.seasar.maya.cycle.Request;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class WebRequest implements Request {

    private static final long serialVersionUID = 8377365781441987529L;

    private HttpServletRequest _httpServletRequest;
    
    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        if(httpServletRequest == null) {
            throw new IllegalArgumentException();
        }
        _httpServletRequest = httpServletRequest;
    }
    
    private void checkRequest() {
        if(_httpServletRequest == null) {
            throw new IllegalStateException();
        }
    }
    
    public Object getUnderlyingObject() {
        checkRequest();
        return _httpServletRequest;
    }
    
    public Locale getLocale() {
        checkRequest();
        return _httpServletRequest.getLocale();
    }

    public String getParameter(String name) {
        checkRequest();
        return _httpServletRequest.getParameter(name);
    }

    public Map getParameterMap() {
        checkRequest();
        return _httpServletRequest.getParameterMap();
    }

    public String getPath() {
        checkRequest();
        StringBuffer buffer = new StringBuffer();
        buffer.append(StringUtil.preparePath(_httpServletRequest.getServletPath()));
        buffer.append(StringUtil.preparePath(_httpServletRequest.getPathInfo()));
        return buffer.toString();
    }
    
    public Object getAttribute(String name) {
        checkRequest();
        if(StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException();
        }
        return _httpServletRequest.getAttribute(name);
    }

    public void setAttribute(String name, Object attribute) {
        checkRequest();
        if(StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException();
        }
        if(attribute != null) {
            _httpServletRequest.setAttribute(name, attribute);
        } else {
            _httpServletRequest.removeAttribute(name);
        }
    }
    
}
