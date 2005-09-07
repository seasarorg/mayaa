/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License"); you may
 * not use this file except in compliance with the License which accompanies
 * this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.seasar.maya.impl.cycle.jsp;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;
import javax.servlet.jsp.tagext.BodyContent;

import org.seasar.maya.cycle.Application;
import org.seasar.maya.cycle.AttributeScope;
import org.seasar.maya.cycle.Request;
import org.seasar.maya.cycle.Response;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.Session;
import org.seasar.maya.impl.util.CycleUtil;
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
    private static final String[] CYCLE_SCOPES = {
            ServiceCycle.SCOPE_PAGE,
            ServiceCycle.SCOPE_REQUEST,
            ServiceCycle.SCOPE_SESSION,
            ServiceCycle.SCOPE_APPLICATION
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

    public void forward(String relativeUrlPath) throws ServletException, IOException {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
    	cycle.forward(relativeUrlPath);
    }

    public void include(String relativeUrlPath) throws ServletException, IOException {
        throw new UnsupportedOperationException();
    }

    public void handlePageException(Exception e) throws ServletException, IOException {
    	throw new UnsupportedOperationException();
    }
    
    public void handlePageException(Throwable t) throws ServletException, IOException {
    	throw new UnsupportedOperationException();
    }

    public Exception getException() {
        throw new UnsupportedOperationException();
    }

    // Underlying object -----------------------------------------------
    
    public ServletContext getServletContext() {
        Application application = CycleUtil.getApplication();
        Object obj = application.getUnderlyingObject();
        if(obj instanceof ServletContext) {
            return (ServletContext)obj;
        }
        throw new IllegalStateException();
    }
    
    public HttpSession getSession() {
        Session session = CycleUtil.getSession();
        Object obj = session.getUnderlyingObject();
        if(obj instanceof HttpSession) {
            return (HttpSession)obj;
        }
        throw new IllegalStateException();
    }

    public ServletRequest getRequest() {
    	Request request = CycleUtil.getRequest();
    	Object obj = request.getUnderlyingObject();
    	if(obj instanceof ServletRequest) {
    		return (ServletRequest)obj;
    	}
        throw new IllegalStateException();
    }
    
    public ServletResponse getResponse() {
    	Response response = CycleUtil.getResponse();
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
    
	public void include(String relativeUrlPath, boolean flush) {
        throw new UnsupportedOperationException();
	}

	public ExpressionEvaluator getExpressionEvaluator() {
        return ExpressionEvaluatorImpl.getInstance();
	}

	public VariableResolver getVariableResolver() {
        return VariableResolverImpl.getInstance();
	}

	// Attributes ------------------------------------------------------------
    public Object findAttribute(String name) {
        for(int i = 0; i < CYCLE_SCOPES.length; i++) {
            Object ret = CycleUtil.getAttribute(name, CYCLE_SCOPES[i]);
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
        for(int i = 0; i < CYCLE_SCOPES.length; i++) {
            CycleUtil.removeAttribute(name, CYCLE_SCOPES[i]);
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
        AttributeScope attrScope = CycleUtil.getAttributeScope(scopeName);
        return IteratorEnumeration.getInstance(attrScope.iterateAttributeNames());
    }

    public int getAttributesScope(String name) {
        for(int i = 0; i < CYCLE_SCOPES.length; i++) {
            Object ret = CycleUtil.getAttribute(name, CYCLE_SCOPES[i]);
            if(ret != null) {
                return JSP_SCOPES[i];
            }
        }
        return 0;
    }
    
    // support class -----------------------------------------------------
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
