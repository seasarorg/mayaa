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

import javax.servlet.ServletContext;

/**
 * ServletContext init parameters as Map.
 * @author Anton Koinov
 */
public class InitParamMap extends AbstractAttributeMap {
    
    private ServletContext _servletContext;

    public InitParamMap(ServletContext servletContext) {
        _servletContext = servletContext;
    }

    protected Object getAttribute(String key) {
        return _servletContext.getInitParameter(key);
    }

    protected void setAttribute(String key, Object value) {
        throw new UnsupportedOperationException(
            "Cannot set ServletContext InitParameter");
    }

    protected void removeAttribute(String key) {
        throw new UnsupportedOperationException(
            "Cannot remove ServletContext InitParameter");
    }

    protected Enumeration getAttributeNames() {
        return _servletContext.getInitParameterNames();
    }

}
