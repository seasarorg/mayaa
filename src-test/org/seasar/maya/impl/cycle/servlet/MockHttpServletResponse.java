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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class MockHttpServletResponse extends MockServletResponse 
        implements HttpServletResponse {

    public void sendRedirect(String url) {
    }

    public void sendError(int code, String msg) {
    }

    public void sendError(int code) {
    }

    public void setStatus(int status, String msg) {
    }

    public void setStatus(int status) {
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
    }

    public boolean containsHeader(String name) {
        return false;
    }

    public void addHeader(String name, String value) {
    }

    public void setHeader(String name, String value) {
    }

    public void addDateHeader(String name, long value) {
    }

    public void setDateHeader(String name, long value) {
    }

    public void addIntHeader(String name, int value) {
    }

    public void setIntHeader(String name, int value) {
    }

}
