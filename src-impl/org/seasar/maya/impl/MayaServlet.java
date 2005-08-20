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
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.seasar.maya.cycle.Request;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.Engine;
import org.seasar.maya.impl.provider.factory.SimpleProviderFactory;
import org.seasar.maya.impl.source.PageSourceDescriptor;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.ThrowableUtil;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ProviderFactory;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class MayaServlet extends HttpServlet implements CONST_IMPL {

	private static final long serialVersionUID = 5182757427194256698L;
	private static boolean _inithialized;
    private Engine _engine;

    public void init() throws ServletException {
    	if(_inithialized == false) {
            ProviderFactory.setDefaultFactory(new SimpleProviderFactory());
            ProviderFactory.setServletContext(getServletContext());
            _inithialized = true;
    	}
    	ServiceProvider provider = ProviderFactory.getServiceProvider();
        _engine = provider.getEngine();
    }

    private String getRequestedPath(ServiceCycle cycle) {
        Request request = cycle.getRequest();
        String path = request.getPageName();
        String extention = request.getExtension();
        if(StringUtil.hasValue(extention)) {
            path = path + "." + extention;
        }
        return path;
    }
    
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        ServiceProvider provider = ProviderFactory.getServiceProvider();
        provider.initialize(request, response);
        ServiceCycle cycle = provider.getServiceCycle();
        String path = getRequestedPath(cycle);
        String mimeType = cycle.getApplication().getMimeType(path);
    	if(mimeType != null && (
                mimeType.startsWith("text/html") || 
                mimeType.startsWith("text/xhtml") ||
                mimeType.startsWith("text/xml"))) {
            prepareResponse(response);
    		doMayaService(cycle);
    	} else {
    		doResourceService(cycle);
    	}
    }
    
    protected void handleError(Throwable t) {
        try {
            t = ThrowableUtil.removeWrapperRuntimeException(t);
            t.printStackTrace();
            _engine.getErrorHandler().doErrorHandle(t);
        } catch(Throwable tx) {
            if(tx instanceof RuntimeException) {
                throw (RuntimeException)tx;
            }
            throw new RuntimeException(tx);
        }
    }
    
    protected void prepareResponse(ServletResponse response) {
        if(response instanceof HttpServletResponse) {
            HttpServletResponse httpResponse = (HttpServletResponse)response;
            httpResponse.addHeader("Pragma", "no-cache");
            httpResponse.addHeader("Cache-Control", "no-cache");
            httpResponse.addHeader("Expires", "Thu, 01 Dec 1994 16:00:00 GMT");
        }
    }

    protected void doMayaService(ServiceCycle cycle) {
        if(cycle == null) {
            throw new IllegalArgumentException();
        }
        try {
            _engine.doService();
        } catch(Throwable t) {
            if(t instanceof MayaException) {
                ((MayaException)t).setCurrentNode(cycle.getCurrentNode());
            }
            cycle.getResponse().clearBuffer();
            cycle.resetPageScope();
            handleError(t);
        }
        cycle.getResponse().flush();
    }
    
    protected void doResourceService(ServiceCycle cycle) {
        if(cycle == null) {
            throw new IllegalArgumentException();
        }
        String path = getRequestedPath(cycle);
        SourceDescriptor source = new PageSourceDescriptor(path);
        InputStream stream = source.getInputStream();
        if(stream != null) {
            OutputStream out = cycle.getResponse().getUnderlyingOutputStream();
            try {
                for(int i = stream.read(); i != -1; i = stream.read()) {
                    out.write(i);
                }
                out.flush();
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            cycle.getResponse().setStatus(404);
        }
    }
    
}