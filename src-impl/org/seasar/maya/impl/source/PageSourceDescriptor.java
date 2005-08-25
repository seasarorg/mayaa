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

import org.seasar.maya.impl.provider.IllegalParameterValueException;
import org.seasar.maya.impl.provider.UnsupportedParameterException;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PageSourceDescriptor extends CompositeSourceDescriptor {

	private static final long serialVersionUID = -6124718310228340001L;

    private String _folder = "/WEB-INF/page";
    
    public void setParameter(String name, String value) {
        if("folder".equals(name)) {
            if(value == null) {
                throw new IllegalParameterValueException(name);
            }
            _folder = value;
        } else {
            throw new UnsupportedParameterException(name);
        }
    }
    
    public void setSystemID(String systemID) {
        super.setSystemID(systemID);
        ApplicationSourceDescriptor app1 = new ApplicationSourceDescriptor();
        app1.setSystemID(systemID);
        addSourceDescriptor(app1);
        ApplicationSourceDescriptor app2 = new ApplicationSourceDescriptor();
        app2.setRoot(_folder);
        app2.setSystemID(systemID);
        addSourceDescriptor(app2);
        ClassLoaderSourceDescriptor loader = new ClassLoaderSourceDescriptor();
        loader.setRoot(ClassLoaderSourceDescriptor.META_INF);
        loader.setSystemID(systemID);
        addSourceDescriptor(loader);
    }

}
