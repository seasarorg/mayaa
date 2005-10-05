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
package org.seasar.maya.impl.cycle.jsp;

import java.util.Enumeration;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;
import javax.servlet.jsp.tagext.BodyContent;

import org.seasar.maya.cycle.Response;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.scope.ApplicationScope;
import org.seasar.maya.cycle.scope.AttributeScope;
import org.seasar.maya.cycle.scope.RequestScope;
import org.seasar.maya.cycle.scope.SessionScope;
import org.seasar.maya.impl.cycle.CycleUtil;
import org.seasar.maya.impl.util.collection.IteratorEnumeration;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PageContextImpl extends PageContext {

    private static final int[] JSP_SCOPES = {
            PageContext.PAGE_SCOPE,
            PageContext.REQUEST_SCOPE,
            PageContext.SESSION_SCOPE,
            PageContext.APPLICATION_SCOPE
    }; 

    private ServletConfig _config;

    private String getScopeFromInt(int scope) {
        if(scope == PageContext.APPLICATION_SCOPE) {
            return ServiceCycle.SCOPE_APPLICATION;
        } else if(scope == PageContext.SESSION_SCOPE) {
            return ServiceCycle.SCOPE_SESSION;
        } else if(scope == PageContext.REQUEST_SCOPE) {
            return ServiceCycle.SCOPE_REQUEST;
        } else if(scope == PageContext.PAGE_SCOPE) {
            return ServiceCycle.SCOPE_PAGE;
        }
        throw new IllegalArgumentException();
    }
    
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
    	return new JspWriterImpl(response.popWriter());
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

    public void include(String relativeUrlPath) {
        include(relativeUrlPath, false);
    }
    
    public void include(String relativeUrlPath, boolean flush) {
        // TODO impl JSP-include.
        throw new UnsupportedOperationException();
    }

    public void handlePageException(Exception e) {
        handlePageException((Throwable)e);
    }
    
    public void handlePageException(Throwable t) {
        if(t instanceof RuntimeException) {
            throw (RuntimeException)t;
        }
        throw new RuntimeException(t);
    }

    public Exception getException() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        Throwable t = cycle.getHandledError();
        if(t instanceof Exception) {
            return (Exception)t;
        }
        return null;
    }

    // getting underlying object ---------------------------------------
    
    public ServletContext getServletContext() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        ApplicationScope application = cycle.getApplicationScope();
        Object obj = application.getUnderlyingObject();
        if(obj instanceof ServletContext) {
            return (ServletContext)obj;
        }
        throw new IllegalStateException();
    }
    
    public HttpSession getSession() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        SessionScope session = cycle.getSessionScope();
        Object obj = session.getUnderlyingObject();
        if(obj instanceof HttpSession) {
            return (HttpSession)obj;
        }
        throw new IllegalStateException();
    }

    public ServletRequest getRequest() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        RequestScope request = cycle.getRequestScope();
    	Object obj = request.getUnderlyingObject();
    	if(obj instanceof ServletRequest) {
    		return (ServletRequest)obj;
    	}
        throw new IllegalStateException();
    }
    
    public ServletResponse getResponse() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        Response response = cycle.getResponse();
    	Object obj = response.getUnderlyingObject();
    	if(obj instanceof ServletResponse) {
    		return (ServletResponse)obj;
    	}
        throw new IllegalStateException();
    }
    
    public ServletConfig getServletConfig() {
        if(_config == null) {
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
    
    public Object findAttribute(String name) {
        for(int i = 0; i < CycleUtil.STANDARD_SCOPES.length; i++) {
            Object ret = CycleUtil.getAttribute(
                    name, CycleUtil.STANDARD_SCOPES[i]);
            if(ret != null) {
                return ret;
            }
        }
        return null;
    }

    public Object getAttribute(String name, int scope) {
        if(name == null) {
            throw new IllegalArgumentException();
        }
        String scopeName = getScopeFromInt(scope);
        return CycleUtil.getAttribute(name, scopeName);
    }

    public Object getAttribute(String name) {
        return getAttribute(name, PAGE_SCOPE);
    }

    public void removeAttribute(String name, int scope) {
        if(name == null) {
            throw new IllegalArgumentException();
        }
        String scopeName = getScopeFromInt(scope);
        CycleUtil.removeAttribute(name, scopeName);
    }

    public void removeAttribute(String name) {
        if(name == null) {
            throw new IllegalArgumentException();
        }
        for(int i = 0; i < CycleUtil.STANDARD_SCOPES.length; i++) {
            CycleUtil.removeAttribute(
                    name, CycleUtil.STANDARD_SCOPES[i]);
        }
    }

    public void setAttribute(String name, Object value, int scope) {
        if(name == null) {
            throw new IllegalArgumentException();
        }
        String scopeName = getScopeFromInt(scope);
        CycleUtil.setAttribute(name, value, scopeName);
    }

    public void setAttribute(String name, Object value) {
        setAttribute(name, value, PAGE_SCOPE);
    }

    public Enumeration getAttributeNamesInScope(int scope) {
        String scopeName = getScopeFromInt(scope);
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        AttributeScope attrScope = cycle.getAttributeScope(scopeName);
        return IteratorEnumeration.getInstance(
                attrScope.iterateAttributeNames());
    }

    public int getAttributesScope(String name) {
        for(int i = 0; i < CycleUtil.STANDARD_SCOPES.length; i++) {
            Object ret = CycleUtil.getAttribute(
                    name, CycleUtil.STANDARD_SCOPES[i]);
            if(ret != null) {
                return JSP_SCOPES[i];
            }
        }
        return 0;
    }
    
    // support class -------------------------------------------------
    
    private class CycleServletConfig implements ServletConfig {

        public String getInitParameter(String name) {
            return getServletContext().getInitParameter(name);
        }
        
        public Enumeration getInitParameterNames() {
            return getServletContext().getInitParameterNames();
        }
        
        public ServletContext getServletContext() {
            return getServletContext();
        }
        
        public String getServletName() {
            return "Maya Servlet";
        }
    
    }
    
}
