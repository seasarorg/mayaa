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
package org.seasar.maya.standard.cycle;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.impl.cycle.ImplicitScope;
import org.seasar.maya.impl.cycle.implicit.ImplicitObjectResolver;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.standard.cycle.implicit.PageContextResolver;
import org.seasar.maya.standard.cycle.implicit.RequestResolver;
import org.seasar.maya.standard.cycle.implicit.ResponseResolver;
import org.seasar.maya.standard.cycle.implicit.SessionResolver;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class JspImplicitScope extends ImplicitScope {

    public static final String SCOPE_JSP_IMPLICIT = "jspImplicit";
    
    private static Map _jspResolverMap;
    static {
        _jspResolverMap = new HashMap();
        _jspResolverMap.put(PageContext.PAGECONTEXT, new PageContextResolver());
        _jspResolverMap.put(PageContext.REQUEST, new RequestResolver());
        _jspResolverMap.put(PageContext.RESPONSE, new ResponseResolver());
        _jspResolverMap.put(PageContext.SESSION, new SessionResolver());
    }
    
    public JspImplicitScope(ServiceCycle cycle) {
        super(cycle);
    }

    public Iterator iterateAttributeNames() {
        return _jspResolverMap.keySet().iterator();
    }
    
    protected ImplicitObjectResolver getResolver(String name) {
        if(StringUtil.isEmpty(name)) {
            return null;
        }
        return (ImplicitObjectResolver)_jspResolverMap.get(name);
    }
    
}
