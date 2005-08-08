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
package org.seasar.maya.standard.cycle.implicit;

import javax.servlet.http.HttpSession;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.Session;
import org.seasar.maya.impl.cycle.implicit.ImplicitObjectResolver;
import org.seasar.maya.impl.util.CycleUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SessionResolver implements ImplicitObjectResolver {

	public Object resolve() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        Session session = cycle.getRequest().getSession();
        Object obj = session.getUnderlyingObject();
        if(obj instanceof HttpSession) {
            return obj;
        }
        throw new IllegalStateException();
	}

}
