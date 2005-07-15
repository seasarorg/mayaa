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
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;

import org.seasar.maya.cycle.Request;
import org.seasar.maya.cycle.Response;
import org.seasar.maya.engine.Engine;
import org.seasar.maya.impl.cycle.web.WebRequest;
import org.seasar.maya.impl.cycle.web.WebResponse;
import org.seasar.maya.impl.provider.factory.SimpleServiceProviderFactory;
import org.seasar.maya.impl.util.EngineUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.provider.EngineSetting;
import org.seasar.maya.provider.PageContextSetting;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ServiceProviderFactory;
import org.seasar.maya.source.SourceDescriptor;
import org.seasar.maya.source.factory.SourceFactory;

/**
 * TODO ServiceCycle
 * 
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
    	WebRequest webRequest = new WebRequest();
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
    
    /**
     * @deprecated
     */
    private PageContext getPageContext(
            ServletRequest request, ServletResponse response) {
        if(request == null || response == null) {
            throw new IllegalArgumentException();
        }
        JspFactory factory = JspFactory.getDefaultFactory();
        PageContextSetting pc = _engine.getEngineSetting().getPageContextSetting();
        PageContext context = factory.getPageContext(this, request, response, 
                pc.getErrorPageURL(), pc.isNeedSession(), pc.getBufferSize(), pc.isAutoFlush());
        EngineUtil.setEngine(context, _engine);
        return context;
    }
    
    /**
     * @deprecated
     */
    private void releasePageContext(PageContext context) {
        if(context == null) {
            throw new IllegalArgumentException();
        }
        JspFactory factory = JspFactory.getDefaultFactory();
        factory.releasePageContext(context);
    }

    protected Throwable removeWrapperRuntimeException(Throwable t) {
        Throwable throwable = t ;
        while(throwable.getClass().equals(RuntimeException.class)) {
            if( throwable.getCause() == null ) {
                break;
            }
            throwable = throwable.getCause();
        }
        return throwable ;
    }
    
    protected void handleError(ServletRequest request, ServletResponse response, Throwable t) {
        PageContext context = getPageContext(request, response);
        try {
            t = removeWrapperRuntimeException(t);
            _engine.getErrorHandler().doErrorHandle(context, t);
        } catch(Throwable tx) {
            if(tx instanceof RuntimeException) {
                throw (RuntimeException)tx;
            }
            throw new RuntimeException(tx);
        } finally {
            releasePageContext(context);
        }
    }
    
    protected String getRequestedPath(ServletRequest request) {
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
    
    protected void prepareRequest(ServletResponse response) {
        if(response instanceof HttpServletResponse) {
            HttpServletResponse httpResponse = (HttpServletResponse)response;
            httpResponse.addHeader("Pragma", "no-cache");
            httpResponse.addHeader("Cache-Control", "no-cache");
            httpResponse.addHeader("Expires", "Thu, 01 Dec 1994 16:00:00 GMT");
        }
    }
    
    /*
     * return array include...
     * [0]=page name
     * [1]=suffix or none
     * [2]=extention
     */
    protected String[] getRequestedPageInfo(EngineSetting setting, String path) {
        String[] requestedPageInfo = new String[3];
        int paramOffset = path.indexOf('?');
        if(paramOffset >= 0) {
            path = path.substring(0, paramOffset);
        }
        int lastSlashOffset = path.lastIndexOf('/');
        String folder =  "";
        String file = path;
        if(lastSlashOffset >= 0) {
            folder = path.substring(0, lastSlashOffset + 1);
            file = path.substring(lastSlashOffset + 1);
        }
        int lastDotOffset = file.lastIndexOf('.');
        if(lastDotOffset > 0) {
            requestedPageInfo[2] = file.substring(lastDotOffset + 1);
            file = file.substring(0, lastDotOffset);
        } else {
            requestedPageInfo[2] = "";
        }
        String suffixSeparator = setting.getSuffixSeparator();
        int suffixSeparatorOffset = file.lastIndexOf(suffixSeparator);
        if(suffixSeparatorOffset > 0) {
            requestedPageInfo[0] = folder + file.substring(0, suffixSeparatorOffset);
            requestedPageInfo[1] = file.substring(suffixSeparatorOffset + suffixSeparator.length());
        } else {
            requestedPageInfo[0] = folder + file;
            requestedPageInfo[1] = "";
        }
        return requestedPageInfo;
    }

    protected void doMayaService(ServletRequest request, ServletResponse response) {
        if(request == null || response == null) {
            throw new IllegalArgumentException();
        }
        prepareRequest(response);
        EngineSetting setting = _engine.getEngineSetting();
        String path = getRequestedPath(request);
        String[] requestedPageInfo = getRequestedPageInfo(setting, path);
        try {
            PageContext context = getPageContext(request, response);
            try {
                _engine.doService(context, requestedPageInfo[0], 
                        requestedPageInfo[1], requestedPageInfo[2]);
            } catch(Throwable t) {
                context.getOut().clearBuffer();
                throw t;
            } finally {
                releasePageContext(context);
            }
        } catch(Throwable t) {
            handleError(request, response, t);
        }
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