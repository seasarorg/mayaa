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
package org.seasar.maya.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.seasar.maya.cycle.Request;
import org.seasar.maya.cycle.Response;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.Engine;
import org.seasar.maya.impl.cycle.web.WebRequest;
import org.seasar.maya.impl.cycle.web.WebResponse;
import org.seasar.maya.impl.provider.factory.SimpleServiceProviderFactory;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.ThrowableUtil;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ServiceProviderFactory;
import org.seasar.maya.source.SourceDescriptor;
import org.seasar.maya.source.factory.SourceFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class MayaServlet extends HttpServlet implements CONST_IMPL {

	private static final long serialVersionUID = 5182757427194256698L;
	private static boolean _inithialized;
    private Engine _engine;

    public void init() throws ServletException {
    	if(_inithialized == false) {
            ServiceProviderFactory.setDefaultFactory(new SimpleServiceProviderFactory());
            ServiceProviderFactory.setServletContext(getServletContext());
            _inithialized = true;
    	}
    	ServiceProvider provider = ServiceProviderFactory.getServiceProvider();
        _engine = provider.getEngine();
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String uri = request.getRequestURI();
    	String mimeType = getServletContext().getMimeType(uri);
    	if(mimeType == null || "text/html".equals(mimeType)) {
    		doMayaService(request, response);
    	} else {
    		doResourceService(request, response);
    	}
    }
    
    protected Request createRequest(HttpServletRequest request) {
    	if(request == null) {
    		throw new IllegalArgumentException();
    	}
        String suffixSeparator = _engine.getEngineSetting().getSuffixSeparator();
    	WebRequest webRequest = new WebRequest(suffixSeparator);
    	webRequest.setHttpServletRequest(request);
    	return webRequest;
    }
    
    protected Response createResponse(HttpServletResponse response) {
    	if(response == null) {
    		throw new IllegalArgumentException();
    	}
    	WebResponse webResponse = new WebResponse();
    	webResponse.setHttpServletResponse(response);
    	return webResponse;
    }
    
    protected ServiceCycle getServiceCycle(Request request, Response response) {
        if(request == null || response == null) {
            throw new IllegalArgumentException();
        }
        ServiceProvider provider = ServiceProviderFactory.getServiceProvider();
        return provider.getServiceCycle(request, response);
    }
    
    protected void releaseServiceCycle(ServiceCycle cycle) {
        if(cycle == null) {
            throw new IllegalArgumentException();
        }
        ServiceProvider provider = ServiceProviderFactory.getServiceProvider();
        provider.releaseServiceCycle(cycle);
    }
    
    protected void handleError(Request request, Response response, Throwable t) {
        ServiceCycle cycle = getServiceCycle(request, response);
        try {
            t = ThrowableUtil.removeWrapperRuntimeException(t);
            _engine.getErrorHandler().doErrorHandle(cycle, t);
        } catch(Throwable tx) {
            if(tx instanceof RuntimeException) {
                throw (RuntimeException)tx;
            }
            throw new RuntimeException(tx);
        } finally {
            releaseServiceCycle(cycle);
        }
    }
    
    protected void prepareRequest(ServletResponse response) {
        if(response instanceof HttpServletResponse) {
            HttpServletResponse httpResponse = (HttpServletResponse)response;
            httpResponse.addHeader("Pragma", "no-cache");
            httpResponse.addHeader("Cache-Control", "no-cache");
            httpResponse.addHeader("Expires", "Thu, 01 Dec 1994 16:00:00 GMT");
        }
    }

    protected void doMayaService(
            HttpServletRequest request, HttpServletResponse response) {
        if(request == null || response == null) {
            throw new IllegalArgumentException();
        }
        prepareRequest(response);
        Request req = createRequest(request);
        Response res = createResponse(response);
        try {
            ServiceCycle cycle = getServiceCycle(req, res);
            try {
                _engine.doService(cycle);
            } catch(Throwable t) {
                res.clearBuffer();
                throw t;
            } finally {
                releaseServiceCycle(cycle);
            }
        } catch(Throwable t) {
            handleError(req, res, t);
        }
        res.flush();
    }
    
    private String getRequestedPath(ServletRequest request) {
        if(request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest)request;
            StringBuffer buffer = new StringBuffer();
            buffer.append(StringUtil.preparePath(httpRequest.getServletPath()));
            String pathInfo = httpRequest.getPathInfo();
            buffer.append(StringUtil.preparePath(pathInfo));
            if(buffer.toString().endsWith("/")) {
                buffer.append(StringUtil.preparePath(_engine.getWelcomeFileName()));
            }
            return buffer.toString();
        }
        throw new IllegalStateException();
    }
    
    protected void doResourceService(ServletRequest request, ServletResponse response) {
        if(request == null || response == null) {
            throw new IllegalArgumentException();
        }
        String path = PREFIX_PAGE + getRequestedPath(request);
        ServiceProvider provider = ServiceProviderFactory.getServiceProvider();
        SourceFactory factory = provider.getSourceFactory();
        SourceDescriptor source = factory.createSourceDescriptor(path);
        InputStream stream = source.getInputStream();
        if(stream != null) {
            try {
                Writer out = response.getWriter();
                for(int i = stream.read(); i != -1; i = stream.read()) {
                    out.write(i);
                }
                out.flush();
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            if(response instanceof HttpServletResponse) {
                HttpServletResponse httpResponse = (HttpServletResponse)response;
                httpResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }
    
}