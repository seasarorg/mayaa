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
package org.seasar.maya.impl.jsp.cycle;

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
import javax.servlet.jsp.tagext.BodyContent;

import org.seasar.maya.cycle.Application;
import org.seasar.maya.cycle.AttributeScope;
import org.seasar.maya.cycle.Request;
import org.seasar.maya.cycle.Response;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.Session;
import org.seasar.maya.impl.util.CycleUtil;
import org.seasar.maya.impl.util.JspUtil;
import org.seasar.maya.impl.util.collection.IteratorEnumeration;
import org.seasar.maya.impl.util.collection.NullEnumeration;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CyclePageContext extends PageContext {

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

    public CyclePageContext() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.putAttributeScope(
                JspImplicitScope.SCOPE_JSP_IMPLICIT, new JspImplicitScope());
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
        return new CycleJspWriter(response.getWriter());
    }

    public JspWriter popBody() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        Response response = cycle.getResponse();
    	return new CycleJspWriter(response.popWriter());
    }

    public BodyContent pushBody() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        Response response = cycle.getResponse();
        return new CycleBodyContent(response.pushWriter());
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
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        Request request = cycle.getRequest();
        Throwable throwable = (Throwable)request.getAttribute(EXCEPTION);
        if(throwable != null && !(throwable instanceof Exception)) {
            throwable = new Exception(throwable);
        }
        return (Exception) throwable;
    }

    // Underlying object -----------------------------------------------
    public ServletRequest getRequest() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
    	Request request = cycle.getRequest();
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
    
    public HttpSession getSession() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        Session session = cycle.getRequest().getSession();
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
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        Application application = cycle.getApplication();
        Object obj = application.getUnderlyingObject();
        if(obj instanceof ServletContext) {
            return (ServletContext)obj;
        }
        throw new IllegalStateException();
    }

    public Object getPage() {
        throw new UnsupportedOperationException();
    }

    // Attributes ------------------------------------------------------------
    public Object findAttribute(String name) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        for(int i = 0; i < CYCLE_SCOPES.length; i++) {
            AttributeScope scope = cycle.getAttributeScope(CYCLE_SCOPES[i]);
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
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        AttributeScope attrScope = cycle.getAttributeScope(scopeName);
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
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        AttributeScope attrScope = cycle.getAttributeScope(scopeName);
        attrScope.removeAttribute(name);
    }

    public void removeAttribute(String name) {
        if(name == null) {
            throw new IllegalArgumentException();
        }
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        for(int i = 0; i < CYCLE_SCOPES.length; i++) {
            AttributeScope attrScope = cycle.getAttributeScope(CYCLE_SCOPES[i]);
            attrScope.removeAttribute(name);
        }
    }

    public void setAttribute(String name, Object value, int scope) {
        if(name == null) {
            throw new IllegalArgumentException();
        }
        String scopeName = JspUtil.getScopeFromInt(scope);
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        AttributeScope attrScope = cycle.getAttributeScope(scopeName);
        attrScope.setAttribute(name, value);
    }

    public void setAttribute(String name, Object value) {
        setAttribute(name, value, PAGE_SCOPE);
    }

    public Enumeration getAttributeNamesInScope(int scope) {
        String scopeName = JspUtil.getScopeFromInt(scope);
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        AttributeScope attrScope = cycle.getAttributeScope(scopeName);
        return IteratorEnumeration.getInstance(attrScope.iterateAttributeNames());
    }

    public int getAttributesScope(String name) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        for(int i = 0; i < CYCLE_SCOPES.length; i++) {
            AttributeScope attrScope = cycle.getAttributeScope(CYCLE_SCOPES[i]);
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
