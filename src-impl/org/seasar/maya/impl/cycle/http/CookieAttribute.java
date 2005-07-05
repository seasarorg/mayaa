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
package org.seasar.maya.impl.cycle.http;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.seasar.maya.impl.cycle.AttributeScope;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CookieAttribute implements AttributeScope {

    private HttpServletRequest _httpServletRequest;
    private HttpServletResponse _httpServletResponse;
    
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

    public void setHttpServletResponse(HttpServletResponse httpServletResponse) {
        if(httpServletResponse == null) {
            throw new IllegalArgumentException();
        }
        _httpServletResponse = httpServletResponse;
    }

    private void checkResponse() {
        if(_httpServletResponse == null) {
            throw new IllegalStateException();
        }
    }
    
    public Object getAttribute(String name) {
        checkRequest();
        if(StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException();
        }
        Cookie[] cookies = _httpServletRequest.getCookies();
        for(int i = 0; i < cookies.length; i++) {
            if(name.equals(cookies[i].getName())) {
                return cookies[i].getValue();
            }
        }
        return null;
    }

    public void setAttribute(String name, Object attribute) {
        checkResponse();
        if(StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException();
        }
        if(attribute != null) {
            if(attribute instanceof String) {
                _httpServletResponse.addCookie(new Cookie(name, attribute.toString()));
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

}
