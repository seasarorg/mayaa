/*
 * Copyright (c) 2004-2005 the Seasar Founcation and the Others.
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
package org.seasar.maya.impl.util;

import org.seasar.maya.cycle.AttributeScope;
import org.seasar.maya.cycle.ServiceCycle;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CycleUtil {
    
    private CycleUtil() {
    }

    private static final String[] SCOPES = {
        ServiceCycle.SCOPE_PAGE,
        ServiceCycle.SCOPE_REQUEST,
        ServiceCycle.SCOPE_SESSION,
        ServiceCycle.SCOPE_APPLICATION
    };
    
    public static Object findAttribute(ServiceCycle cycle, String name) {
        for (int i = 0; i < SCOPES.length; i++) {
            AttributeScope scope = cycle.getAttributeScope(SCOPES[i]);
            Object obj = scope.getAttribute(name);
            if (obj != null) {
                return obj;
            }
        }
        return null;
    }

    public static void rewriteAttribute(ServiceCycle cycle, String name, Object value) {
        for (int i = 0; i < SCOPES.length; i++) {
            AttributeScope scope = cycle.getAttributeScope(SCOPES[i]);
            Object obj = scope.getAttribute(name);
            if (obj != null) {
                scope.setAttribute(name, value);
                return;
            }
        }
        cycle.setAttribute(name, value);
    }
    
}
