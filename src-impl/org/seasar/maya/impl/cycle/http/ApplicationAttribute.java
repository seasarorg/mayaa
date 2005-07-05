/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 *
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which
 * accompanies this distribution, and is available at
 *
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.seasar.maya.impl.cycle.http;

import javax.servlet.ServletContext;

import org.seasar.maya.impl.cycle.AttributeScope;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.provider.factory.ServiceProviderFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ApplicationAttribute implements AttributeScope {

    public Object getAttribute(String name) {
        if(StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException();
        }
        ServletContext context = ServiceProviderFactory.getServiceProvider().getServletContext();
        return context.getAttribute(name);
    }

    public void setAttribute(String name, Object attribute) {
        if(StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException();
        }
        ServletContext context = ServiceProviderFactory.getServiceProvider().getServletContext();
        if(attribute != null) {
            context.setAttribute(name, attribute);
        } else {
            context.removeAttribute(name);
        }
    }
    
}
