/*
 * Copyright 2004-2012 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.mayaa.impl.cycle.web;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;

import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.impl.util.collection.IteratorEnumeration;

/**
 * AutoPageBuilderで利用するServletRequestのモック。
 *
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class MockServletRequest implements ServletRequest {

    private Map _parameters = new HashMap();
    private Map _attributes = new HashMap();
    private List _locale;

    public Map getParameterMap() {
        return _parameters;
    }

    public Enumeration getParameterNames() {
        return IteratorEnumeration.getInstance(_parameters.keySet().iterator());
    }

    public String[] getParameterValues(String name) {
        if(StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException();
        }
        return (String[])_parameters.get(name);
    }

    public String getParameter(String name) {
        if(StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException();
        }
        String values[] = getParameterValues(name);
        if(values != null && values.length > 0) {
            return values[0];
        }
        return null;
    }

    public void addParameter(String name, String value) {
        if(StringUtil.isEmpty(name) || value == null) {
            throw new IllegalArgumentException();
        }
        String[] values = (String[])_parameters.get(name);
        if(values == null) {
            _parameters.put(name, new String[] { value });
        } else {
            int len = values.length;
            String[] newValues = new String[len + 1];
            System.arraycopy(values, 0, newValues, 0, len);
            newValues[len] = value;
            _parameters.put(name, newValues);
        }
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

    public String getCharacterEncoding() {
        return null;
    }

    public int getContentLength() {
        return 0;
    }

    public String getContentType() {
        return null;
    }

    public void setCharacterEncoding(String encoding) {
        // do nothing.
    }

    public Locale getLocale() {
        if(_locale == null) {
            throw new IllegalStateException();
        }
        return (Locale)_locale.get(0);
    }

    public void addLocale(Locale locale) {
        if(locale == null) {
            throw new IllegalArgumentException();
        }
        if(_locale == null) {
            _locale = new ArrayList();
        }
        _locale.add(locale);
    }

    public Enumeration getLocales() {
        return IteratorEnumeration.getInstance(_locale.iterator());
    }

    public ServletInputStream getInputStream() {
        return null;
    }

    public BufferedReader getReader() {
        return null;
    }

    public String getRealPath(String arg0) {
        throw new UnsupportedOperationException();
    }

    public RequestDispatcher getRequestDispatcher(String uri) {
        throw new UnsupportedOperationException();
    }

    public String getRemoteAddr() {
        return "127.0.0.1";
    }

    public String getRemoteHost() {
        return "localhost";
    }

    public String getScheme() {
        return "http";
    }

    public String getProtocol() {
        return "GET";
    }

    public String getServerName() {
        return "localhost";
    }

    public int getServerPort() {
        return 8080;
    }

    public boolean isSecure() {
        return false;
    }

// since 2.4 --------------------------------------------------

    public String getLocalAddr() {
        return null;
    }

    public String getLocalName() {
        return null;
    }

    public int getLocalPort() {
        return 0;
    }

    public int getRemotePort() {
        return 0;
    }

}
