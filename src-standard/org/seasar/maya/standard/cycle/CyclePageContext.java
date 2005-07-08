/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
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
import java.io.Writer;
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

import org.seasar.maya.cycle.Request;
import org.seasar.maya.cycle.Response;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.Session;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.test.util.servlet.jsp.tagext.TestBodyContent;

/**
 * TODO Cycle対応実装（今のところ、既存からコピペしただけ）
 * 
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CyclePageContext extends PageContext {
    
    private static final String ERROR_EXCEPTION = "javax.servlet.error.exception";
    private static final String ERROR_STATUS_CODE = "javax.servlet.error.status_code";
    private static final String ERROR_REQUEST_URI = "javax.servlet.error.request_uri";
    private static final String ERROR_SERVLET_NAME = "javax.servlet.error.servlet_name";
    
	private ServiceCycle _cycle;
    private Servlet _servlet;
    private String _errorPageURL;

    public CyclePageContext(ServiceCycle cycle) {
        if(cycle == null) {
            throw new IllegalArgumentException();
        }
        _cycle = cycle;
    }

    public void initialize(Servlet servlet, ServletRequest request,
            ServletResponse response, String errorPageURL,
            boolean needsSession, int bufferSize, boolean autoFlush) {
    }
    
    public void release() {
    }

    public JspWriter getOut() {
        return null;
    }

    public JspWriter popBody() {
    	return null;
    }

    public BodyContent pushBody() {
        return (BodyContent)pushBody(null);
    }

    private JspWriter pushBody(Writer writer) {
        TestBodyContent bodyContent = new TestBodyContent(null);
//        _bodyContentStack.push(bodyContent);
//        bodyContent.setWriter(writer);
//        _out = bodyContent;
        return bodyContent;
    }

    public void forward(String relativeUrlPath) throws ServletException, IOException {
//        try {
//            _out.clear();
//        } catch(IOException e) {
//            throw new IllegalStateException();
//        }
//        String path = getAbsolutePathRelativeToContext(relativeUrlPath);
//        getServletContext().getRequestDispatcher(path).forward(_request, _response);
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
//        if(_request == null) {
//            throw new IllegalStateException();
//        }
//        Throwable throwable = (Throwable)_request.getAttribute(EXCEPTION);
//        if(throwable != null && !(throwable instanceof Exception)) {
//            throwable = new JspException(throwable);
//        }
//        return (Exception) throwable;
    	return null;
    }
    
    public ServletRequest getRequest() {
    	Request request = _cycle.getRequest();
    	Object obj = request.getUnderlyingObject();
    	if(obj instanceof ServletRequest) {
    		return (ServletRequest)obj;
    	}
    	// TODO MockRequest
    	return null;
    }
    
    public ServletResponse getResponse() {
    	Response response = _cycle.getResponse();
    	Object obj = response.getUnderlyingObject();
    	if(obj instanceof ServletResponse) {
    		return (ServletResponse)obj;
    	}
    	// TODO MockResponse
    	return null;
    }
    
    public ServletConfig getServletConfig() {
        if(_servlet == null) {
            throw new IllegalStateException();
        }
        return _servlet.getServletConfig();
    }
    
    public ServletContext getServletContext() {
        if(_servlet == null) {
            throw new IllegalStateException();
        }
        return _servlet.getServletConfig().getServletContext();
    }
    
    public HttpSession getSession() {
    	Session session = _cycle.getSession();
    	Object obj = session.getUnderlyingObject();
    	if(obj instanceof HttpSession) {
            return (HttpSession)obj;
        }
    	// TODO MockSession
        return null;
    }

    // Attributes ------------------------------------------------------------
    public Object findAttribute(String name) {
//        for(int i = PAGE_SCOPE; i < scopes.length; i++) {
//            Object ret = scopes[i].getAttribute(name);
//            if(ret != null) {
//                return ret;
//            }
//        }
        return null;
    }

    public Object getAttribute(String name, int scope) {
        if(name == null) {
            throw new IllegalArgumentException();
        }
        String scopeName = "";
        return _cycle.getAttribute(name, scopeName);
    }

    public Object getAttribute(String name) {
        return getAttribute(name, PAGE_SCOPE);
    }

    public void removeAttribute(String name, int scope) {
        if(name == null) {
            throw new IllegalArgumentException();
        }
        String scopeName = "";
        _cycle.setAttribute(name, null, scopeName);
    }

    public void removeAttribute(String name) {
        if(name == null) {
            throw new IllegalArgumentException();
        }
//        for(int i = PAGE_SCOPE; i < APPLICATION_SCOPE; i++) {
//            scopes[i].removeAttribute(name);
//        }
    }

    public void setAttribute(String name, Object value, int scope) {
        if(name == null) {
            throw new IllegalArgumentException();
        }
        String scopeName = "";
        _cycle.setAttribute(name, value, scopeName);
    }

    public void setAttribute(String name, Object value) {
        setAttribute(name, value, PAGE_SCOPE);
    }

    public Enumeration getAttributeNamesInScope(int scope) {
//        checkScope(scope);
//        return scopes[scope].getAttributeNames();
    	return null;
    }

    public int getAttributesScope(String name) {
//        for(int i = PAGE_SCOPE; i < scopes.length; i++) {
//            Object ret = scopes[i].getAttribute(name);
//            if(ret != null) {
//                return i;
//            }
//        }
        return 0;
    }

	/* (non-Javadoc)
	 * @see javax.servlet.jsp.PageContext#getPage()
	 */
	public Object getPage() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.jsp.PageContext#include(java.lang.String)
	 */
	public void include(String arg0) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	}
    
}
