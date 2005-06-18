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

/**
 * HttpServletRequest headers as Map.
 * @author Anton Koinov
 */
public class HeaderMap extends AbstractAttributeMap {
    
    private final HttpServletRequest _httpServletRequest;

    public HeaderMap(HttpServletRequest httpServletRequest) {
        if(httpServletRequest == null) {
            throw new IllegalArgumentException();
        }
        _httpServletRequest = httpServletRequest;
    }

    protected Object getAttribute(String key) {
        return _httpServletRequest.getHeader(key);
    }

    protected void setAttribute(String key, Object value) {
        throw new UnsupportedOperationException(
            "Cannot set HttpServletRequest Header");
    }

    protected void removeAttribute(String key) {
        throw new UnsupportedOperationException(
            "Cannot remove HttpServletRequest Header");
    }

    protected Enumeration getAttributeNames() {
        return _httpServletRequest.getHeaderNames();
    }

}
