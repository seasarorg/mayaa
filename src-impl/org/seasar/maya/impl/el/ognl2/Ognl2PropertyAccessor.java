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
package org.seasar.maya.impl.el.ognl2;

import java.util.Map;

import ognl.ObjectPropertyAccessor;
import ognl.OgnlException;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.el.resolver.ExpressionChain;
import org.seasar.maya.el.resolver.ExpressionResolver;
import org.seasar.maya.engine.specification.Specification;
import org.seasar.maya.impl.el.PropertyNotFoundException;
import org.seasar.maya.impl.util.SpecificationUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class Ognl2PropertyAccessor extends ObjectPropertyAccessor {

	private ExpressionResolver _resolver;
	
	public Ognl2PropertyAccessor(ExpressionResolver resolver) {
		if(resolver == null) {
			throw new IllegalArgumentException();
		}
		_resolver = resolver;
	}
	
	private ServiceCycle getServiceCycle(Map map) {
        return (ServiceCycle)map.get(Ognl2CompiledExpression.SERVICE_CYCLE);
	}	    
	
	private Object getRootModel(Map map) {
        ServiceCycle cycle = getServiceCycle(map);
        Specification specification = SpecificationUtil.findSpecification(cycle);
       	return SpecificationUtil.findSpecificationModel(cycle, specification);
	}
	
	public Object getProperty(Map map, Object target, Object property) {
	    Object base = target;
        if(base == Ognl2CompiledExpression.ROOT) {
           	Object model = getRootModel(map);
            if(model != null) {
                try {
                    return super.getProperty(map, model, property);
                } catch (Exception ignore) {
                }
            }
            base = null;
        }
        ServiceCycle cycle = getServiceCycle(map);
        if(cycle == null) {
            try {
                return super.getProperty(map, target, property);
            } catch (OgnlException e) {
                throw new RuntimeException(e);
            }
        }
        OgnlExpressionChain chain = new OgnlExpressionChain(map);
        return _resolver.getValue(cycle, base, property, chain);
    }

    public void setProperty(Map map, Object target, Object property, Object value) {
	    Object base = target;
        if(base == Ognl2CompiledExpression.ROOT) {
           	Object model = getRootModel(map);
            if(model != null) {
                try {
                    super.setProperty(map, model, property, value);
                    return;
                } catch (Exception ignore) {
                }
            }
            base = null;
        }
        ServiceCycle cycle = getServiceCycle(map);
        if(cycle == null) {
            try {
                super.setProperty(map, target, property, value);
                return;
            } catch (OgnlException e) {
                throw new RuntimeException(e);
            }
        }
        OgnlExpressionChain chain = new OgnlExpressionChain(map);
        _resolver.setValue(cycle, base, property, value, chain);
    }
    
	public Object superGetPropertyValue(Map map, Object target, Object property) {
        try {
            return super.getProperty(map, target, property);
        } catch (OgnlException e) {
            throw new PropertyNotFoundException(target, property);
        }
	}

    public void superSetPropertyValue(Map map, Object target, Object property, Object value) {
        try {
            super.setProperty(map, target, property, value);
        } catch (OgnlException e) {
            throw new PropertyNotFoundException(target, property);
        }
    }
	
    private class OgnlExpressionChain implements ExpressionChain {
    
        private Map _map;
        
        public OgnlExpressionChain(Map map) {
            if(map == null) {
                throw new IllegalArgumentException();
            }
            _map = map;
        }
        
	    public Object getValue(
                ServiceCycle cycle, Object base, Object property) {
	        return superGetPropertyValue(_map, base, property);
	    }
	
	    public void setValue(ServiceCycle cycle, 
	            Object base, Object property, Object value) {
	        superSetPropertyValue(_map, base, property, value);
	    }

    }

}
