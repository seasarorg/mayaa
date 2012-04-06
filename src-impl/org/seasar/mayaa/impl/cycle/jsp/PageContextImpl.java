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
package org.seasar.mayaa.impl.cycle.jsp;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;
import javax.servlet.jsp.tagext.BodyContent;

import org.seasar.mayaa.cycle.Response;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.cycle.scope.ApplicationScope;
import org.seasar.mayaa.cycle.scope.AttributeScope;
import org.seasar.mayaa.cycle.scope.RequestScope;
import org.seasar.mayaa.cycle.scope.SessionScope;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.cycle.StandardScope;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.impl.util.collection.IteratorEnumeration;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PageContextImpl extends PageContext {

    private ServletConfig _config;

    public void initialize(Servlet servlet, ServletRequest request,
            ServletResponse response, String errorPageURL,
            boolean needsSession, int bufferSize, boolean autoFlush) {
        // Can't call.
        throw new IllegalStateException();
    }

    public void release() {
        // Can't call.
        throw new IllegalStateException();
    }

    public JspWriter getOut() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        Response response = cycle.getResponse();
        return new JspWriterImpl(response.getWriter());
    }

    public JspWriter popBody() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        Response response = cycle.getResponse();
        return new JspWriterImpl(response.getWriter());
    }

    public BodyContent pushBody() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        Response response = cycle.getResponse();
        return new BodyContentImpl(response.pushWriter());
    }

    public void forward(String relativeUrlPath) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.forward(relativeUrlPath);
    }

    public void include(String relativeUrlPath)
            throws ServletException, IOException {
        include(relativeUrlPath, false);
    }

    protected String getContextRelativePath(
            ServletRequest request, String relativePath) {
        if (relativePath.startsWith("/")
                || request instanceof HttpServletRequest == false) {
            return relativePath;
        }
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String uri = httpRequest.getServletPath();
        if (StringUtil.hasValue(uri)) {
            int pos = uri.lastIndexOf('/');
            if (pos >= 0) {
                uri = uri.substring(0, pos);
            }
        } else {
            uri = "";
        }
        return uri + '/' + relativePath;
    }

    public void include(String relativeUrlPath, boolean flush)
            throws ServletException, IOException {
        if (flush) {
            Response response = CycleUtil.getResponse();
            response.getWriter().flush();
        }
        ServletRequest request = getRequest();
        String contextRelativePath =
            getContextRelativePath(request, relativeUrlPath);
        RequestDispatcher dispatcher =
            request.getRequestDispatcher(contextRelativePath);
        dispatcher.include(request, getResponse());
    }

    public void handlePageException(Exception e) {
        handlePageException((Throwable) e);
    }

    public void handlePageException(Throwable t) {
        if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        }
        throw new RuntimeException(t);
    }

    public Exception getException() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        Throwable t = cycle.getHandledError();
        if (t instanceof Exception) {
            return (Exception) t;
        }
        return null;
    }

    // getting underlying object ---------------------------------------

    public ServletContext getServletContext() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        ApplicationScope application = cycle.getApplicationScope();
        Object obj = application.getUnderlyingContext();
        if (obj instanceof ServletContext) {
            return (ServletContext) obj;
        }
        throw new IllegalStateException();
    }

    public HttpSession getSession() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        SessionScope session = cycle.getSessionScope();

        Object obj = session.getUnderlyingContext();
        if (obj instanceof HttpSession) {
            return (HttpSession) obj;
        }
        return null;
    }

    public ServletRequest getRequest() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        RequestScope request = cycle.getRequestScope();
        Object obj = request.getUnderlyingContext();
        if (obj instanceof ServletRequest) {
            return (ServletRequest) obj;
        }
        throw new IllegalStateException();
    }

    public ServletResponse getResponse() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        Response response = cycle.getResponse();
        Object obj = response.getUnderlyingContext();
        if (obj instanceof ServletResponse) {
            return (ServletResponse) obj;
        }
        throw new IllegalStateException();
    }

    public ServletConfig getServletConfig() {
        if (_config == null) {
            _config = new CycleServletConfig();
        }
        return _config;
    }

    public Object getPage() {
        throw new UnsupportedOperationException();
    }

    // since 2.0 -------------------------------------------------

    public ExpressionEvaluator getExpressionEvaluator() {
        return ExpressionEvaluatorImpl.getInstance();
    }

    public VariableResolver getVariableResolver() {
        return VariableResolverImpl.getInstance();
    }

    // Attributes --------------------------------------------------

    private int toJspScope(String name) {
        if (ServiceCycle.SCOPE_PAGE.equals(name)) {
            return PageContext.PAGE_SCOPE;
        } else if (ServiceCycle.SCOPE_REQUEST.equals(name)) {
            return PageContext.REQUEST_SCOPE;
        } else if (ServiceCycle.SCOPE_SESSION.equals(name)) {
            return PageContext.SESSION_SCOPE;
        } else if (ServiceCycle.SCOPE_APPLICATION.equals(name)) {
            return PageContext.APPLICATION_SCOPE;
        }
        return 0;
    }

    private String toServiceScope(int scope) {
        if (scope == PageContext.APPLICATION_SCOPE) {
            return ServiceCycle.SCOPE_APPLICATION;
        } else if (scope == PageContext.SESSION_SCOPE) {
            return ServiceCycle.SCOPE_SESSION;
        } else if (scope == PageContext.REQUEST_SCOPE) {
            return ServiceCycle.SCOPE_REQUEST;
        } else if (scope == PageContext.PAGE_SCOPE) {
            return ServiceCycle.SCOPE_PAGE;
        }
        throw new IllegalArgumentException();
    }

    protected AttributeScope findAttributeScope(String name) {
        AttributeScope pageScope =
            CycleUtil.getServiceCycle().getPageScope();
        if (pageScope.hasAttribute(name)) {
            return pageScope;
        }
        AttributeScope scope = CycleUtil.findStandardAttributeScope(name);
        if (scope != null) {
            return scope;
        }
        return pageScope;
    }

    public Object findAttribute(String name) {
        return findAttributeScope(name).getAttribute(name);
    }

    public Object getAttribute(String name, int scope) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        if (scope != PageContext.PAGE_SCOPE) {
            String scopeName = toServiceScope(scope);
            return CycleUtil.getAttribute(name, scopeName);
        }
        return findAttributeScope(name).getAttribute(name);
    }

    public void removeAttribute(String name, int scope) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        if (scope != PageContext.PAGE_SCOPE) {
            String scopeName = toServiceScope(scope);
            CycleUtil.removeAttribute(name, scopeName);
        } else {
            findAttributeScope(name).removeAttribute(name);
        }
    }

    public void setAttribute(String name, Object value, int scope) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        if (scope != PageContext.PAGE_SCOPE) {
            String scopeName = toServiceScope(scope);
            CycleUtil.setAttribute(name, value, scopeName);
        } else {
            findAttributeScope(name).setAttribute(name, value);
        }
    }

    public Object getAttribute(String name) {
        return getAttribute(name, PAGE_SCOPE);
    }

    public void removeAttribute(String name) {
        /* removeAttribute(name, scope)があるのにこれをやると
         * 想定外の動作になってしまう。
        if (name == null) {
            throw new IllegalArgumentException();
        }
        StandardScope standardScope = CycleUtil.getStandardScope();
        for (int i = 0; i < standardScope.size(); i++) {
            CycleUtil.removeAttribute(name, standardScope.get(i));
        }
        */

        removeAttribute(name, PAGE_SCOPE);
    }

    public void setAttribute(String name, Object value) {
        setAttribute(name, value, PAGE_SCOPE);
    }

    public Enumeration getAttributeNamesInScope(int scope) {
        String scopeName = toServiceScope(scope);
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        AttributeScope attrScope = cycle.getAttributeScope(scopeName);
        return IteratorEnumeration.getInstance(
                attrScope.iterateAttributeNames());
    }

    public int getAttributesScope(String name) {
        StandardScope standardScope = CycleUtil.getStandardScope();
        int size = standardScope.size();
        for (int i = 0; i < size; i++) {
            String scopeName = standardScope.get(i);
            Object ret = CycleUtil.getAttribute(name, scopeName);
            if (ret != null) {
                int scope = toJspScope(scopeName);
                if (scope > 0) {
                    return scope;
                }
            }
        }
        return 0;
    }

    // support class -------------------------------------------------

    private class CycleServletConfig implements ServletConfig {
        protected CycleServletConfig() {
            // do nothing.
        }

        public String getInitParameter(String name) {
            return getServletContext().getInitParameter(name);
        }

        public Enumeration getInitParameterNames() {
            return getServletContext().getInitParameterNames();
        }

        public ServletContext getServletContext() {
            return PageContextImpl.this.getServletContext();
        }

        public String getServletName() {
            return "Mayaa Servlet";
        }

    }

}
