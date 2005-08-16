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

import org.seasar.maya.cycle.script.resolver.ScriptResolver;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class AttributeScriptResolver implements ScriptResolver {
    
    public void putParameter(String name, String value) {
        throw new UnsupportedOperationException();
    }

//    private ProcessorProperty findProperty(Iterator it, Object property) {
//        if(it == null || property == null) {
//            throw new IllegalArgumentException();
//        }
//        while(it.hasNext()) {
//            ProcessorProperty prop = (ProcessorProperty)it.next();
//            if(prop.getQName().getLocalName().equals(property)) {
//                return prop;
//            }
//        }
//        return null;
//    }
    
    public Object getVariable(String name) {
//        if (property == null || chain == null) {
//            throw new NullPointerException();
//        }
//        ServiceCycle cycle = CycleUtil.getServiceCycle();
//        if (base == null && "attribute".equals(property)) {
//            Template template = SpecificationUtil.getTemplate();
//            TemplateProcessor parent = template.getParentProcessor();
//            if (parent == null) {
//                return new ParamMap(cycle.getRequest());
//            } else if (parent instanceof ComponentPageProcessor) {
//                return parent;
//            } else {
//                throw new IllegalStateException();
//            }
//        } else if (base instanceof ComponentPageProcessor) {
//            ComponentPageProcessor component = (ComponentPageProcessor) base;
//            Iterator it = component.getInformalProperties().iterator();
//            ProcessorProperty prop = findProperty(it, property);
//            if(prop == null) {
//                it = component.getProcesstimeProperties().iterator();
//                prop = findProperty(it , property);
//            }
//            if(prop != null) {
//                return prop.getValue();
//            }
//            throw new PropertyNotFoundException(base, property);
//        }
//        return chain.getValue(base, property);
        return UNDEFINED;
    }

    public boolean setVariable(String name, Object value) {
//        if (property == null || chain == null) {
//            throw new NullPointerException();
//        }
//        if ((base == null && "attribute".equals(property))
//                || (base instanceof ComponentPageProcessor)) {
//            throw new PropertyNotWritableException(base, property);
//        }
//        chain.setValue(base, property, value);
        return false;
    }

}