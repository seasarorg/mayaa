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

import org.seasar.maya.builder.library.converter.PropertyConverter;
import org.seasar.maya.impl.builder.library.PropertyDefinitionImpl;
import org.seasar.maya.impl.provider.factory.AbstractParameterizableTagHandler;
import org.seasar.maya.impl.util.XMLUtil;
import org.seasar.maya.provider.Parameterizable;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ConverterTagHandler 
		extends AbstractParameterizableTagHandler {

    private PropertyTagHandler _parent;  
    private PropertyConverter _propertyConverter;
    
    public ConverterTagHandler(PropertyTagHandler parent) {
        super("converter");
        _parent = parent;
    }
    
    protected void start(Attributes attributes) {
    	PropertyConverter converter =
    		(PropertyConverter)XMLUtil.getObjectValue(
    				attributes, "class", null, PropertyConverter.class);
        if(converter == null) {
        	throw new IllegalStateException();
        }
    	PropertyDefinitionImpl propertyDefinition =
    		_parent.getPropertyDefinition();
    	propertyDefinition.setPropertyConverter(converter);
    }
    
    protected void end(String body) {
        _propertyConverter = null;
    }

	public Parameterizable getParameterizable() {
        if(_propertyConverter == null) {
            throw new IllegalStateException();
        }
        return _propertyConverter;
	}
    
}
