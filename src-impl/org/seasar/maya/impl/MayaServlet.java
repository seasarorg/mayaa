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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.seasar.maya.engine.Engine;
import org.seasar.maya.impl.provider.factory.SimpleServiceProviderFactory;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ServiceProviderFactory;

/**
 * Mayaのサービスエントリ。web.xmlに登録する。GET＆POSTリクエストを受け取ると、
 * Engine#doService(ServletRequest, ServletResponse)を呼び出す
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class MayaServlet extends HttpServlet implements CONST_IMPL {

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
    		_engine.doService(request, response);
    	} else {
    		_engine.doResourceService(request, response);
    	}
    }

}