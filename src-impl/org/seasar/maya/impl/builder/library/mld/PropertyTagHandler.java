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
package org.seasar.maya.impl.builder.library.mld;

import org.seasar.maya.impl.builder.library.ProcessorDefinitionImpl;
import org.seasar.maya.impl.builder.library.PropertyDefinitionImpl;
import org.seasar.maya.impl.provider.factory.AbstractParameterizableTagHandler;
import org.seasar.maya.impl.util.XMLUtil;
import org.seasar.maya.provider.Parameterizable;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PropertyTagHandler 
		extends AbstractParameterizableTagHandler {

    private ProcessorTagHandler _parent;
    private PropertyDefinitionImpl _propertyDefinition;
    
    public PropertyTagHandler(ProcessorTagHandler parent) {
        super("property");
        _parent = parent;
        putHandler(new ConverterTagHandler(this));
    }

    protected void start(
    		Attributes attributes, String systemID, int lineNumber) {
        String name = attributes.getValue("name");
        boolean required = XMLUtil.getBooleanValue(
                attributes, "required", false);
        Class expectedType = XMLUtil.getClassValue(
                attributes, "expectedType", Object.class);
        String finalValue = attributes.getValue("final");
        String defaultValue = attributes.getValue("default");
        _propertyDefinition = new PropertyDefinitionImpl();
        _propertyDefinition.setName(name);
        _propertyDefinition.setRequired(required);
        _propertyDefinition.setExpectedType(expectedType);
        _propertyDefinition.setFinalValue(finalValue);
        _propertyDefinition.setDefaultValue(defaultValue);
        _propertyDefinition.setLineNumber(lineNumber);
        ProcessorDefinitionImpl processor = _parent.getProcessorDefinition();
        processor.addPropertyDefinitiion(_propertyDefinition);
        _propertyDefinition.setProcessorDefinition(processor);
    }
    
    protected void end(String body) {
    	_propertyDefinition = null;
    }
 
    public PropertyDefinitionImpl getPropertyDefinition() {
    	if(_propertyDefinition == null) {
    		throw new IllegalStateException();
    	}
    	return _propertyDefinition;
    }

	public Parameterizable getParameterizable() {
		return getPropertyDefinition();
	}
    
}
