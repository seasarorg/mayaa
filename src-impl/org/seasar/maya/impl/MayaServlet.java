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

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.seasar.maya.engine.Engine;
import org.seasar.maya.impl.provider.factory.XmlProviderFactory;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ProviderFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class MayaServlet extends HttpServlet {

    private static final long serialVersionUID = -5816552218525836552L;
    private static boolean _inithialized;
    
    public void init() {
    	if(_inithialized == false) {
            ProviderFactory.setDefaultFactory(new XmlProviderFactory());
            ProviderFactory.setContext(getServletContext());
            _inithialized = true;
    	}
    }

    public void doGet(
            HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

    public void doPost(
            HttpServletRequest request, HttpServletResponse response) {
        ServiceProvider provider = ProviderFactory.getServiceProvider();
        provider.initialize(request, response);
        Engine engine = provider.getEngine();
        engine.doService();
    }
    
}