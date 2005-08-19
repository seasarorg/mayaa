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

import org.seasar.maya.impl.cycle.web.WebApplication;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class BootstrapSourceDescriptor extends CompositeSourceDescriptor {

    private static final long serialVersionUID = -7436518426506691163L;

    public BootstrapSourceDescriptor(String systemID, ServletContext context) {
        super(systemID);
        ApplicationSourceDescriptor appSource = 
            new ApplicationSourceDescriptor("/WEB-INF", systemID);
        WebApplication application = new WebApplication();
        application.setServletContext(context);
        appSource.setApplication(application);
        addSourceDescriptor(appSource);
        addSourceDescriptor(new ClassLoaderSourceDescriptor("/META-INF", systemID, null));
    }

}
