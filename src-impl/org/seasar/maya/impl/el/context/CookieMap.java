/*
 * Copyright 2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.seasar.maya.impl.el.context;

import java.util.Enumeration;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * HttpServletRequest Cookies as Map.
 * @author Dimitry D'hondt
 * @author Anton Koinov
 */
public class CookieMap extends AbstractAttributeMap {

    private final HttpServletRequest _httpServletRequest;

    public CookieMap(HttpServletRequest httpServletRequest) {
        if(httpServletRequest == null) {
            throw new IllegalArgumentException();
        }
        _httpServletRequest = httpServletRequest;
    }

    public void clear() {
        throw new UnsupportedOperationException(
            "Cannot clear HttpRequest Cookies");
    }

    public boolean containsKey(Object key) {
        Cookie[] cookies = _httpServletRequest.getCookies();
        for (int i = 0, len = cookies.length; i < len; i++) {
            if (cookies[i].getName().equals(key)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsValue(Object findValue) {
        if (findValue == null) {
            return false;
        }
        Cookie[] cookies = _httpServletRequest.getCookies();
        for (int i = 0, len = cookies.length; i < len; i++) {
            if (findValue.equals(cookies[i].getValue())) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return _httpServletRequest.getCookies().length == 0;
    }

    public int size() {
        return _httpServletRequest.getCookies().length;
    }

    protected Object getAttribute(String key) {
        Cookie[] cookies = _httpServletRequest.getCookies();
        for (int i = 0, len = cookies.length; i < len; i++) {
            if (cookies[i].getName().equals(key)) {
                return cookies[i].getValue();
            }
        }
        return null;
    }

    protected void setAttribute(String key, Object value) {
        throw new UnsupportedOperationException(
            "Cannot set HttpRequest Cookies");
    }

    protected void removeAttribute(String key) {
        throw new UnsupportedOperationException(
            "Cannot remove HttpRequest Cookies");
    }

    protected Enumeration getAttributeNames() {
        return new CookieNameEnumeration(_httpServletRequest.getCookies());
    }
    
    private static class CookieNameEnumeration implements Enumeration {
        private final Cookie[] _cookies;
        private final int _length;
        private int _index;
        
        public CookieNameEnumeration(Cookie[] cookies) {
            _cookies = cookies;
            _length = cookies.length;
        }

        public boolean hasMoreElements() {
            return _index < _length;
        }

        public Object nextElement() {
            return _cookies[_index++].getName();
        }
    }
    
}
