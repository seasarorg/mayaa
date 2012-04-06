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

import java.security.Principal;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.seasar.mayaa.impl.util.StringUtil;

/**
 * AutoPageBuilderで利用するHttpServletRequestのモック。
 *
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class MockHttpServletRequest extends MockServletRequest
        implements HttpServletRequest {

    private ServletContext _servletContext;
    private String _path;
    private String _contextPath;
    private HttpSession _session;

    public MockHttpServletRequest(ServletContext servletContext, String contextPath) {
        if(servletContext == null) {
            throw new IllegalArgumentException();
        }
        _servletContext = servletContext;
        _contextPath = contextPath;
    }

    public HttpSession getSession(boolean create) {
        if(_session == null && create) {
            _session = new MockHttpSession(_servletContext);
        }
        return _session;
    }

    public HttpSession getSession() {
        return getSession(true);
    }

    public String getContextPath() {
        return _contextPath;
    }

    public String getServletPath() {
        // TODO servletPathを正しく返す
        return "";
    }

    public void setPathInfo(String path) {
        _path = StringUtil.preparePath(path);
    }

    public String getPathInfo() {
        return _path;
    }

    public String getPathTranslated() {
        return getPathInfo();
    }

    public String getRequestURI() {
        return getPathInfo();
    }

    public StringBuffer getRequestURL() {
        return new StringBuffer(getPathInfo());
    }

    public String getQueryString() {
        return null;
    }

    public Cookie[] getCookies() {
        return new Cookie[0];
    }

    public String getMethod() {
        return "GET";
    }

    public String getRequestedSessionId() {
        return null;
    }

    public String getRemoteUser() {
        return null;
    }

    public Enumeration getHeaderNames() {
        return null;
    }

    public String getHeader(String name) {
        return null;
    }

    public Enumeration getHeaders(String name) {
        return null;
    }

    public long getDateHeader(String name) {
        return 0;
    }

    public int getIntHeader(String name) {
        return 0;
    }

    public String getAuthType() {
        return null;
    }

    public Principal getUserPrincipal() {
        return null;
    }

    public boolean isUserInRole(String user) {
        return false;
    }

    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    public boolean isRequestedSessionIdValid() {
        return false;
    }

}
