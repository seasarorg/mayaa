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
package org.seasar.maya.impl.cycle.servlet;

import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.seasar.maya.impl.util.collection.IteratorEnumeration;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class MockHttpSession implements HttpSession {

    private ServletContext _servletContext;
    private Map _attributes = new HashMap();
    private int _interval = 60000;
    
    public MockHttpSession(ServletContext servletContext) {
        if(servletContext == null) {
            throw new IllegalArgumentException();
        }
        _servletContext = servletContext;
    }
    
    public ServletContext getServletContext() {
        return _servletContext;
    }

    public Enumeration getAttributeNames() {
        return IteratorEnumeration.getInstance(_attributes.keySet().iterator());
    }

    public Object getAttribute(String name) {
        return _attributes.get(name);
    }

    public void setAttribute(String name, Object attribute) {
        _attributes.put(name, attribute);
    }

    public void removeAttribute(String name) {
        _attributes.remove(name);
    }

    public void invalidate() {
        _attributes.clear();
    }

    public String getId() {
        return "id";
    }

    public long getCreationTime() {
        return new Date().getTime();
    }
    
    public long getLastAccessedTime() {
        return new Date().getTime();
    }

    public int getMaxInactiveInterval() {
        return _interval;
    }

    public void setMaxInactiveInterval(int interval) {
        _interval = interval;
    }

    public boolean isNew() {
        return false;
    }

    /**
     * @deprecated
     */
    public String[] getValueNames() {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated
     */
    public Object getValue(String name) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated
     */
    public void putValue(String name, Object value) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated
     */
    public void removeValue(String name) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated
     */
    public javax.servlet.http.HttpSessionContext getSessionContext() {
        throw new UnsupportedOperationException();
    }
    
}
