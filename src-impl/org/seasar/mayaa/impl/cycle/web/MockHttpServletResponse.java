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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * AutoPageBuilderで利用するHttpServletResponseのモック。
 *
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class MockHttpServletResponse extends MockServletResponse
        implements HttpServletResponse {

    private int _status;

    public void sendRedirect(String url) {
        // do nothing.
    }

    public void sendError(int code, String msg) {
        // do nothing.
    }

    public void sendError(int code) {
        // do nothing.
    }

    public void setStatus(int status, String msg) {
        // do nothing.
    }

    public void setStatus(int status) {
        _status = status;
    }

    public int getStatus() {
        return _status;
    }

    /**
     * @deprecated
     */
    public String encodeUrl(String url) {
        return encodeURL(url);
    }

    public String encodeURL(String url) {
        return url;
    }

    /**
     * @deprecated
     */
    public String encodeRedirectUrl(String url) {
        return encodeRedirectURL(url);
    }

    public String encodeRedirectURL(String url) {
        throw new UnsupportedOperationException();
    }

    public void addCookie(Cookie cookie) {
        // do nothing.
    }

    public boolean containsHeader(String name) {
        return false;
    }

    public void addHeader(String name, String value) {
        // do nothing.
    }

    public void setHeader(String name, String value) {
        // do nothing.
    }

    public void addDateHeader(String name, long value) {
        // do nothing.
    }

    public void setDateHeader(String name, long value) {
        // do nothing.
    }

    public void addIntHeader(String name, int value) {
        // do nothing.
    }

    public void setIntHeader(String name, int value) {
        // do nothing.
    }

}
