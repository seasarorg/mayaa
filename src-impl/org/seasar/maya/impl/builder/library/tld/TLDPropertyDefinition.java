/*
 * Copyright 2004-2005 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.maya.impl.builder.library.tld;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.maya.engine.specification.NodeAttribute;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.builder.library.NoRequiredPropertyException;
import org.seasar.maya.impl.builder.library.PropertyDefinitionImpl;
import org.seasar.maya.impl.engine.processor.ProcessorPropertyImpl;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TLDPropertyDefinition extends PropertyDefinitionImpl {

    private static final Log LOG =
        LogFactory.getLog(TLDPropertyDefinition.class);
    
    public Object createProcessorProperty(SpecificationNode injected) {
    	if(injected == null) {
    		throw new IllegalArgumentException();
    	}
        Class propertyType = getPropertyType();
        if(propertyType == null) {
            // real property not found on the tag.
            String processorName = getProcessorDefinition().getName();
            if(LOG.isWarnEnabled()) {
                String msg = StringUtil.getMessage(TLDPropertyDefinition.class, 
                        0, new String[] { processorName, getName() });
                LOG.warn(msg);
            }
            return null;
        }
        QName qName = getQName(injected);
        NodeAttribute attribute = injected.getAttribute(qName);
        if(attribute != null) {
            String value = attribute.getValue();
            return new ProcessorPropertyImpl(attribute, value, propertyType);
        } else if(isRequired()) {
            String processorName = getProcessorDefinition().getName();
            throw new NoRequiredPropertyException(processorName, qName);
        }
        return null;
    }
    
}
