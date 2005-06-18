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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.seasar.maya.impl.util.collection.NullEnumeration;

/**
 * HttpSession attibutes as Map.
 * @author Anton Koinov
 */
public class SessionScopeMap extends AbstractAttributeMap {
    
    private final HttpServletRequest _httpRequest;

    public SessionScopeMap(HttpServletRequest httpRequest) {
        if (httpRequest == null) {
            throw new IllegalArgumentException();
        }
        _httpRequest = httpRequest;
    }

    protected Object getAttribute(String key) {
        HttpSession httpSession = getSession();
        return (httpSession == null) ? null : httpSession.getAttribute(key
                .toString());
    }

    protected void setAttribute(String key, Object value) {
        _httpRequest.getSession(true).setAttribute(key, value);
    }

    protected void removeAttribute(String key) {
        HttpSession httpSession = getSession();
        if (httpSession != null) {
            httpSession.removeAttribute(key);
        }
    }

    protected Enumeration getAttributeNames() {
        HttpSession httpSession = getSession();
        return (httpSession == null) ? NullEnumeration.getInstance()
                : httpSession.getAttributeNames();
    }

    private HttpSession getSession() {
        return _httpRequest.getSession(false);
    }
}