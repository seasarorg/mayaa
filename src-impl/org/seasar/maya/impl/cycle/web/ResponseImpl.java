/*
 * Copyright 2004-2005 the Seasar Foundation and the Others.
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
package org.seasar.maya.impl.cycle.web;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.seasar.maya.impl.cycle.AbstractResponse;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ResponseImpl extends AbstractResponse {

    private static final long serialVersionUID = -4653384671998376182L;

    private HttpServletResponse _httpServletResponse;

    protected void check() {
        if(_httpServletResponse == null) {
            throw new IllegalStateException();
        }
    }

    public void redirect(String url) {
        check();
        try {
            _httpServletResponse.sendRedirect(url);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void setContentTypeToUnderlyingObject(
            String contentType) {
        check();
        if(StringUtil.isEmpty(contentType)) {
            throw new IllegalArgumentException();
        }
        _httpServletResponse.setContentType(contentType);
    }

    // Response implements -----------------------------------------

    public void addHeader(String name, String value) {
        check();
        if(StringUtil.isEmpty(name)) {
            return;
        }
        _httpServletResponse.addHeader(name, value);
    }

    public void setHeader(String name, String value) {
        check();
        if(StringUtil.isEmpty(name)) {
            return;
        }
        _httpServletResponse.setHeader(name, value);
    }

    public void setStatus(int code) {
        check();
        _httpServletResponse.setStatus(code);
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
        if(StringUtil.isEmpty(url)) {
            throw new IllegalArgumentException();
        }
        return _httpServletResponse.encodeURL(url);
    }

    // Underlyable implemetns ----------------------------------------

    public void setUnderlyingContext(Object context) {
        if(context == null ||
                context instanceof HttpServletResponse == false) {
            throw new IllegalArgumentException();
        }
        _httpServletResponse = (HttpServletResponse)context;
        clearBuffer();
    }

    public Object getUnderlyingContext() {
        check();
        return _httpServletResponse;
    }

}
