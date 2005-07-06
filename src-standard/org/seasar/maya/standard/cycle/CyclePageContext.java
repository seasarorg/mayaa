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

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CyclePageContext /*extends PageContext*/ {
/*
    private ServiceCycle _cycle;
    
    public CyclePageContext(ServiceCycle cycle) {
        if(cycle == null) {
            throw new IllegalArgumentException();
        }
        _cycle = cycle;
    }
    
    private static final String ERROR_EXCEPTION = "javax.servlet.error.exception";
    private static final String ERROR_STATUS_CODE = "javax.servlet.error.status_code";
    private static final String ERROR_REQUEST_URI = "javax.servlet.error.request_uri";
    private static final String ERROR_SERVLET_NAME = "javax.servlet.error.servlet_name";
    
    private Servlet _servlet;

    public void initialize(Servlet servlet, ServletRequest request,
            ServletResponse response, String errorPageURL,
            boolean needsSession, int bufferSize, boolean autoFlush) {
        throw new UnsupportedOperationException();
    }

    private Writer getBaseOut(ServletResponse response) throws IOException {
        String encoding = response.getCharacterEncoding();
        if (encoding != null) {
            return new OutputStreamWriter(response.getOutputStream(), encoding);
        }
        return response.getWriter();
    }
    
    public void release() {
        for(int i = PAGE_SCOPE; i < scopes.length; i++) {
            scopes[i].release();
        }
        _servlet = null;
        _request = null;
        _response = null;
        _errorPageURL = null;
        _out = null;
    }

    public JspWriter getOut() {
        if(_out == _rootOut && !_rootOut.isInitOut()) {
            try {
                _rootOut.setOut(getBaseOut(getResponse()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return _out;
    }

    public JspWriter popBody() {
        _out = ((BodyContent)_bodyContentStack.pop()).getEnclosingWriter();
        return _out;
    }

    public BodyContent pushBody() {
        return (BodyContent)pushBody(null);
    }

    private JspWriter pushBody(Writer writer) {
        TestBodyContent bodyContent = new TestBodyContent(_out);
        _bodyContentStack.push(bodyContent);
        bodyContent.setWriter(writer);
        _out = bodyContent;
        return bodyContent;
    }
    
    private String getAbsolutePathRelativeToContext(String relativeUrlPath) {
        if (!relativeUrlPath.startsWith("/")) {
            String servletPath = ((HttpServletRequest)_request).getServletPath();
            String baseURI = servletPath.substring(0, servletPath.lastIndexOf('/'));
            relativeUrlPath = baseURI + '/' + relativeUrlPath;
        }
        return relativeUrlPath;
    }

    public void forward(String relativeUrlPath) throws ServletException, IOException {
        try {
            _out.clear();
        } catch(IOException e) {
            throw new IllegalStateException();
        }
        String path = getAbsolutePathRelativeToContext(relativeUrlPath);
        getServletContext().getRequestDispatcher(path).forward(_request, _response);
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
        String uri = ((HttpServletRequest)_request).getRequestURI();
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
        if(_request == null) {
            throw new IllegalStateException();
        }
        Throwable throwable = (Throwable)_request.getAttribute(EXCEPTION);
        if(throwable != null && !(throwable instanceof Exception)) {
            throwable = new JspException(throwable);
        }
        return (Exception) throwable;
    }
    
    public ServletRequest getRequest() {
        if(_request == null) {
            throw new IllegalStateException();
        }
        return _request;
    }
    
    public ServletResponse getResponse() {
        if(_response == null) {
            throw new IllegalStateException();
        }
        return _response;
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
        if(_request == null) {
            throw new IllegalStateException();
        }
        if(_request instanceof HttpServletRequest) {
            return ((HttpServletRequest)_request).getSession(_needsSession);
        }
        return null;
    }

    // Attributes ------------------------------------------------------------
    public Object findAttribute(String name) {
        for(int i = PAGE_SCOPE; i < scopes.length; i++) {
            Object ret = scopes[i].getAttribute(name);
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
        checkScope(scope);
        return scopes[scope].getAttribute(name);
    }

    public Object getAttribute(String name) {
        return getAttribute(name, PAGE_SCOPE);
    }

    public void removeAttribute(String name, int scope) {
        if(name == null) {
            throw new IllegalArgumentException();
        }
        checkScope(scope);
        scopes[scope].removeAttribute(name);
    }

    public void removeAttribute(String name) {
        if(name == null) {
            throw new IllegalArgumentException();
        }
        for(int i = PAGE_SCOPE; i < scopes.length; i++) {
            scopes[i].removeAttribute(name);
        }
    }

    public void setAttribute(String name, Object value, int scope) {
        if(name == null) {
            throw new IllegalArgumentException();
        }
        checkScope(scope);
        scopes[scope].setAttribute(name, value);
    }

    public void setAttribute(String name, Object value) {
        setAttribute(name, value, PAGE_SCOPE);
    }

    public Enumeration getAttributeNamesInScope(int scope) {
        checkScope(scope);
        return scopes[scope].getAttributeNames();
    }

    public int getAttributesScope(String name) {
        for(int i = PAGE_SCOPE; i < scopes.length; i++) {
            Object ret = scopes[i].getAttribute(name);
            if(ret != null) {
                return i;
            }
        }
        return 0;
    }
*/    
}
