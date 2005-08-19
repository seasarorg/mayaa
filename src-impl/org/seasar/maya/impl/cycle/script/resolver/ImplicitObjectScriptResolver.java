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
package org.seasar.maya.impl.cycle.script.resolver;

import org.seasar.maya.cycle.AttributeScope;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.script.resolver.ScriptResolver;
import org.seasar.maya.impl.cycle.script.PropertyNotWritableException;
import org.seasar.maya.impl.util.CycleUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ImplicitObjectScriptResolver implements ScriptResolver {
    
    public void setParameter(String name, String value) {
        throw new UnsupportedOperationException();
    }

    public Object getVariable(String name) {
    	ServiceCycle cycle = CycleUtil.getServiceCycle();
        AttributeScope implicitScope = cycle.getAttributeScope(ServiceCycle.SCOPE_IMPLICIT);
        Object obj = implicitScope.getAttribute(name);
        if(obj != null) {
            return obj;
        }
        return UNDEFINED;
    }

	public boolean setVariable(String name, Object value) {
		Object obj = getVariable(name);
		if(UNDEFINED.equals(obj)) {
			return false;
		}
		throw new PropertyNotWritableException(name);
	}

}