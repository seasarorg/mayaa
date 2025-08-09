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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

import jakarta.servlet.http.HttpServletResponse;

import org.seasar.mayaa.impl.cycle.AbstractResponse;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ResponseImpl extends AbstractResponse {

    private static final long serialVersionUID = -4653384671998376182L;

    private HttpServletResponse _httpServletResponse;
    private transient ResponseHeaderCache _headers = new ResponseHeaderCache();
    private int _status;

    protected void check() {
        if (_httpServletResponse == null) {
            throw new IllegalStateException();
        }
    }

    public void redirect(String url) {
        if (isFlushed() == false) {
            check();
            try {
                _httpServletResponse.sendRedirect(url);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void error(int errorCode, String message) {
        if (isFlushed() == false) {
            check();
            try {
                _httpServletResponse.sendError(errorCode, message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected void setContentTypeToUnderlyingObject(
            String contentType) {
        check();
        if (StringUtil.isEmpty(contentType)) {
            throw new IllegalArgumentException();
        }
        _httpServletResponse.setContentType(contentType);
    }

    // Response implements -----------------------------------------

    public void addHeader(String name, String value) {
        check();
        if (StringUtil.isEmpty(name)) {
            return;
        }
        _httpServletResponse.addHeader(name, value);
        _headers.addHeader(name, value);
    }

    public void setHeader(String name, String value) {
        check();
        if (StringUtil.isEmpty(name)) {
            return;
        }
        _httpServletResponse.setHeader(name, value);
        _headers.setHeader(name, value);
    }

    public void setStatus(int code) {
        check();
        _httpServletResponse.setStatus(code);
        _status = code;
    }

    public OutputStream getOutputStream() {
        check();
        try {
            return _httpServletResponse.getOutputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String encodeURL(String url) {
        check();
        if (StringUtil.isEmpty(url)) {
            throw new IllegalArgumentException();
        }
        return _httpServletResponse.encodeURL(url);
    }

    // ContextAware implemetns --------------------------------------

    public void setUnderlyingContext(Object context) {
        if (context == null
                || context instanceof HttpServletResponse == false) {
            throw new IllegalArgumentException();
        }
        _httpServletResponse = (HttpServletResponse) context;
        clearBuffer();
    }

    public Object getUnderlyingContext() {
        check();
        return _httpServletResponse;
    }

    public List<Object> getHeaders(String name) {
        return _headers.getHeaders(name);
    }

    public boolean containsHeader(String name) {
        return _headers.containsHeader(name);
    }

    public Set<String> getHeaderNames() {
        return _headers.getHeaderNames();
    }

    public int getStatus() {
        return _status;
    }

    public String getCharacterEncoding() {
        return _httpServletResponse.getCharacterEncoding();
    }


    // for deserialize
    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        _headers = new ResponseHeaderCache();
    }

}
