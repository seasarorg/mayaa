/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which 
 * accompanies this distribution, and is available at
 * 
 *     http://homepage3.nifty.com/seasar/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package org.seasar.maya.impl.cycle.el.ognl2;

import java.util.Map;

import ognl.ObjectMethodAccessor;
import ognl.OgnlException;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.specification.Specification;
import org.seasar.maya.impl.cycle.el.MethodNotFoundException;
import org.seasar.maya.impl.util.SpecificationUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class Ognl2MethodAccessor extends ObjectMethodAccessor {

    public Object callMethod(Map map, Object target, String methodName, Object[] args) {
        try {
	        if (target == Ognl2CompiledExpression.ROOT) {
	            ServiceCycle cycle = (ServiceCycle)map.get(
	                    Ognl2CompiledExpression.SERVICE_CYCLE);
	            Specification specification = SpecificationUtil.findSpecification(cycle);
                Object model = SpecificationUtil.findSpecificationModel(specification);
                if (model != null) {
                    return super.callMethod(map, model, methodName, args);
                }
	        }
	        return super.callMethod(map, target, methodName, args);
        } catch(OgnlException e) {
            throw new MethodNotFoundException(target, methodName, args, false);
        }
    }
    
    public Object callStaticMethod(
            Map map, Class targetClass, String methodName, Object[] args) {
        try {
            return super.callStaticMethod(map, targetClass, methodName, args);
        } catch(OgnlException e) {
            throw new MethodNotFoundException(targetClass, methodName, args, true);
        }
    }
}
