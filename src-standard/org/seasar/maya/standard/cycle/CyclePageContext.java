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
package org.seasar.maya.standard.cycle;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;

import org.seasar.maya.cycle.Application;
import org.seasar.maya.cycle.AttributeScope;
import org.seasar.maya.cycle.Request;
import org.seasar.maya.cycle.Response;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.Session;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.collection.IteratorEnumeration;
import org.seasar.maya.impl.util.collection.NullEnumeration;
import org.seasar.maya.standard.util.JspUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CyclePageContext extends PageContext {

    private static final String ERROR_EXCEPTION = "javax.servlet.error.exception";
    private static final String ERROR_STATUS_CODE = "javax.servlet.error.status_code";
    private static final String ERROR_REQUEST_URI = "javax.servlet.error.request_uri";
    private static final String ERROR_SERVLET_NAME = "javax.servlet.error.servlet_name";
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
	private ServiceCycle _cycle;
    private String _errorPageURL;

    public CyclePageContext(ServiceCycle cycle, String errorPageURL) {
        if(cycle == null) {
            throw new IllegalArgumentException();
        }
        _cycle = cycle;
        _errorPageURL = errorPageURL;
        _cycle.putAttributeScope(
                JspImplicitScope.SCOPE_JSP_IMPLICIT, new JspImplicitScope(_cycle));
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
        Response response = _cycle.getResponse();
        return new CycleJspWriter(response.getWriter());
    }

    public JspWriter popBody() {
        Response response = _cycle.getResponse();
    	return new CycleJspWriter(response.popWriter());
    }

    public BodyContent pushBody() {
        Response response = _cycle.getResponse();
        return new CycleBodyContent(response.pushWriter());
    }

    public void forward(String relativeUrlPath) throws ServletException, IOException {
    	_cycle.forward(relativeUrlPath);
    }

    public void include(String relativeUrlPath) throws ServletException, IOException {
    	_cycle.include(relativeUrlPath);
    }

    public void handlePageException(Exception e) throws ServletException, IOException {
        handlePageException((Throwable)e);
    }
    
    public void handlePageException(Throwable t) throws ServletException, IOException {
        if(t == null) {
            return ;
        }
        if(StringUtil.isEmpty(_errorPageURL)) {
            t.printStackTrace();
            return;
        }
        Integer status = new Integer(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        String uri = _cycle.getRequest().getPath();
        String servletName = getServletConfig().getServletName();
        setAttribute(EXCEPTION, t, REQUEST_SCOPE);
        setAttribute(ERROR_EXCEPTION, t, REQUEST_SCOPE);
        setAttribute(ERROR_STATUS_CODE, status, REQUEST_SCOPE);
        setAttribute(ERROR_REQUEST_URI, uri, REQUEST_SCOPE);
        setAttribute(ERROR_SERVLET_NAME, servletName, REQUEST_SCOPE);
        forward(_errorPageURL);
        removeAttribute(EXCEPTION, REQUEST_SCOPE);
        removeAttribute(ERROR_EXCEPTION, REQUEST_SCOPE);
        removeAttribute(ERROR_STATUS_CODE, REQUEST_SCOPE);
        removeAttribute(ERROR_REQUEST_URI, REQUEST_SCOPE);
        removeAttribute(ERROR_SERVLET_NAME, REQUEST_SCOPE);
    }

    public Exception getException() {
        Request request = _cycle.getRequest();
        Throwable throwable = (Throwable)request.getAttribute(EXCEPTION);
        if(throwable != null && !(throwable instanceof Exception)) {
            throwable = new Exception(throwable);
        }
        return (Exception) throwable;
    }

    // Underlying object -----------------------------------------------
    public ServletRequest getRequest() {
    	Request request = _cycle.getRequest();
    	Object obj = request.getUnderlyingObject();
    	if(obj instanceof ServletRequest) {
    		return (ServletRequest)obj;
    	}
        throw new IllegalStateException();
    }
    
    public ServletResponse getResponse() {
    	Response response = _cycle.getResponse();
    	Object obj = response.getUnderlyingObject();
    	if(obj instanceof ServletResponse) {
    		return (ServletResponse)obj;
    	}
        throw new IllegalStateException();
    }
    
    public HttpSession getSession() {
        Session session = _cycle.getRequest().getSession();
        Object obj = session.getUnderlyingObject();
        if(obj instanceof HttpSession) {
            return (HttpSession)obj;
        }
        throw new IllegalStateException();
    }
    
    public ServletConfig getServletConfig() {
        if(_config == null) {
            _config = new CycleServletConfig();
        }
        return _config;
    }
    
    public ServletContext getServletContext() {
        Application application = _cycle.getApplication();
        Object obj = application.getUnderlyingObject();
        if(obj instanceof ServletContext) {
            return (ServletContext)obj;
        }
        throw new IllegalStateException();
    }

    public Object getPage() {
        return null;
    }

    // Attributes ------------------------------------------------------------
    public Object findAttribute(String name) {
        for(int i = 0; i < CYCLE_SCOPES.length; i++) {
            AttributeScope scope = _cycle.getAttributeScope(CYCLE_SCOPES[i]);
            Object ret = scope.getAttribute(name);
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
        String scopeName = JspUtil.getScopeFromInt(scope);
        AttributeScope attrScope = _cycle.getAttributeScope(scopeName);
        return attrScope.getAttribute(name);
    }

    public Object getAttribute(String name) {
        return getAttribute(name, PAGE_SCOPE);
    }

    public void removeAttribute(String name, int scope) {
        if(name == null) {
            throw new IllegalArgumentException();
        }
        String scopeName = JspUtil.getScopeFromInt(scope);
        AttributeScope attrScope = _cycle.getAttributeScope(scopeName);
        attrScope.setAttribute(name, null);
    }

    public void removeAttribute(String name) {
        if(name == null) {
            throw new IllegalArgumentException();
        }
        for(int i = 0; i < CYCLE_SCOPES.length; i++) {
            AttributeScope attrScope = _cycle.getAttributeScope(CYCLE_SCOPES[i]);
            attrScope.setAttribute(name, null);
        }
    }

    public void setAttribute(String name, Object value, int scope) {
        if(name == null) {
            throw new IllegalArgumentException();
        }
        String scopeName = JspUtil.getScopeFromInt(scope);
        AttributeScope attrScope = _cycle.getAttributeScope(scopeName);
        attrScope.setAttribute(name, value);
    }

    public void setAttribute(String name, Object value) {
        setAttribute(name, value, PAGE_SCOPE);
    }

    public Enumeration getAttributeNamesInScope(int scope) {
        String scopeName = JspUtil.getScopeFromInt(scope);
        AttributeScope attrScope = _cycle.getAttributeScope(scopeName);
        return IteratorEnumeration.getInstance(attrScope.iterateAttributeNames());
    }

    public int getAttributesScope(String name) {
        for(int i = 0; i < CYCLE_SCOPES.length; i++) {
            AttributeScope attrScope = _cycle.getAttributeScope(CYCLE_SCOPES[i]);
            Object ret = attrScope.getAttribute(name);
            if(ret != null) {
                return JSP_SCOPES[i];
            }
        }
        return 0;
    }
    
    // support class -----------------------------------------------------
    private class CycleServletConfig implements ServletConfig {

        public String getInitParameter(String name) {
            return null;
        }
        
        public Enumeration getInitParameterNames() {
            return NullEnumeration.getInstance();
        }
        
        public ServletContext getServletContext() {
            return getServletContext();
        }
        
        public String getServletName() {
            return "Internal dummy servlet";
        }
    
    }
    
}
