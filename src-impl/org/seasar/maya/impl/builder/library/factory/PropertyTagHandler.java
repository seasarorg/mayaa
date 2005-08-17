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
package org.seasar.maya.impl.builder.library.handler;

import org.seasar.maya.impl.builder.library.ProcessorDefinitionImpl;
import org.seasar.maya.impl.builder.library.PropertyDefinitionImpl;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.XmlUtil;
import org.seasar.maya.impl.util.xml.TagHandler;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PropertyTagHandler extends TagHandler {

    private ProcessorTagHandler _parent;
    
    public PropertyTagHandler(ProcessorTagHandler parent) {
        _parent = parent;
    }

    protected void start(Attributes attributes) {
        String name = attributes.getValue("name");
        if(StringUtil.isEmpty(name)) {
            // ÉçÉOèoóÕ
            _parent.invalidate();
        } else {
	        boolean required = XmlUtil.getBooleanValue(attributes, "required", false);
	        String expectedType = attributes.getValue("expectedType");
	        String defaultValue = attributes.getValue("defaultValue");
	        PropertyDefinitionImpl property = new PropertyDefinitionImpl();
	        property.setName(name);
	        property.setRequired(required);
	        property.setExpectedType(expectedType);
	        property.setDefaultValue(defaultValue);
	        ProcessorDefinitionImpl processor = _parent.getProcessorDefinition();
	        property.setProcessorDefinition(processor);
	        processor.addPropertyDefinitiion(property);
        }
    }
    
}
