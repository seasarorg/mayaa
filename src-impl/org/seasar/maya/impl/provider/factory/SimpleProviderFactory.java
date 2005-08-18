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
package org.seasar.maya.impl.provider.factory;

import javax.servlet.ServletContext;

import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.provider.SimpleServiceProvider;
import org.seasar.maya.impl.source.ClassLoaderSourceDescriptor;
import org.seasar.maya.impl.util.XmlUtil;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ProviderFactory;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SimpleProviderFactory extends ProviderFactory
		implements CONST_IMPL {

	private static final long serialVersionUID = 3581634661222113559L;

	protected static final String KEY_SERVICE = ServiceProvider.class.getName();
    
    private ServiceProvider createServiceProvider(ServletContext servletContext) {
    	SourceDescriptor source = new ClassLoaderSourceDescriptor(
                "/META-INF", "/maya.conf", null); 
    	if(source.exists()) {
	    	MayaConfHandler handler = new MayaConfHandler(servletContext);
	        XmlUtil.parse(handler, source.getInputStream(), 
	                PUBLIC_CONF10, source.getSystemID(), true, true, false);
            return handler.getResult();
    	}
    	return new SimpleServiceProvider(servletContext);
    }
    
    public ServiceProvider getServiceProvider(ServletContext servletContext) {
        if(servletContext == null) {
            throw new IllegalArgumentException();
        }
        ServiceProvider provider = (ServiceProvider)servletContext.getAttribute(KEY_SERVICE);
        if(provider == null) {
            provider = createServiceProvider(servletContext);
            servletContext.setAttribute(KEY_SERVICE, provider);
        }
        return provider;
    }

}
