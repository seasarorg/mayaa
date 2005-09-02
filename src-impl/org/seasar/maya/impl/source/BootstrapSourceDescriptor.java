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
package org.seasar.maya.impl.source;

import javax.servlet.ServletContext;

import org.seasar.maya.impl.cycle.web.ApplicationImpl;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class BootstrapSourceDescriptor extends CompositeSourceDescriptor {

    private static final long serialVersionUID = -7436518426506691163L;
    private ServletContext _servletContext;

    public void setServletContext(ServletContext servletContext) {
        if(servletContext == null) {
            throw new IllegalArgumentException();
        }
        _servletContext = servletContext;
    }
    
    public void setSystemID(String systemID) {
        if(_servletContext == null) {
            throw new IllegalStateException();
        }
        super.setSystemID(systemID);
        ApplicationSourceDescriptor appSource = new ApplicationSourceDescriptor();
        appSource.setRoot(ApplicationSourceDescriptor.WEB_INF);
        appSource.setSystemID(systemID);
        ApplicationImpl application = new ApplicationImpl(_servletContext);
        appSource.setApplication(application);
        addSourceDescriptor(appSource);
        ClassLoaderSourceDescriptor loader = new ClassLoaderSourceDescriptor();
        loader.setRoot(ClassLoaderSourceDescriptor.META_INF);
        loader.setSystemID(systemID);
        addSourceDescriptor(loader);
    }

}
