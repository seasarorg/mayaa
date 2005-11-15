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
package org.seasar.maya.impl.builder.library;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.maya.builder.library.LibraryDefinition;
import org.seasar.maya.builder.library.LibraryManager;
import org.seasar.maya.builder.library.ProcessorDefinition;
import org.seasar.maya.builder.library.PropertyDefinition;
import org.seasar.maya.builder.library.PropertySet;
import org.seasar.maya.builder.library.converter.PropertyConverter;
import org.seasar.maya.engine.processor.VirtualPropertyAcceptable;
import org.seasar.maya.engine.specification.NodeAttribute;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.ParameterAwareImpl;
import org.seasar.maya.impl.engine.specification.SpecificationUtil;
import org.seasar.maya.impl.provider.ProviderUtil;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PropertyDefinitionImpl extends ParameterAwareImpl 
        implements PropertyDefinition, CONST_IMPL {

    private static final Log LOG =
        LogFactory.getLog(PropertyDefinitionImpl.class);
    
    private PropertySet _propertySet;
    private String _name;
    private boolean _required;
    private Class _expectedClass;
    private String _defaultValue;
    private String _finalValue;
    private String _propertyConverterName;
    private PropertyConverter _propertyConverter;

    public void setPropertySet(PropertySet propertySet) {
        if(propertySet == null) {
            throw new IllegalArgumentException();
        }
        _propertySet = propertySet;
    }
    
    public PropertySet getPropertySet() {
        if(_propertySet == null) {
            throw new IllegalStateException();
        }
        return _propertySet;
    }
    
    public void setName(String name) {
        if(StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException();
        }
        _name = name;
    }
    
    public String getName() {
        if(StringUtil.isEmpty(_name)) {
            throw new IllegalStateException();
        }
        return _name;
    }

    public void setRequired(boolean required) {
        _required = required;
    }
    
    public boolean isRequired() {
        return _required;
    }

    public void setExpectedClass(Class expectedClass) {
        _expectedClass = expectedClass;
    }

    public Class getExpectedClass() {
        if(_expectedClass == null) {
            return Object.class;
        }
        return _expectedClass;
    }
    
    public void setDefaultValue(String defaultValue) {
        _defaultValue = defaultValue;
    }
    
    public String getDefaultValue() {
        return _defaultValue;
    }
    
    public void setFinalValue(String finalValue) {
        _finalValue = finalValue;
    }
    
    public String getFinalValue() {
        return _finalValue;
    }

    public void setPropertyConverterName(String propertyConverterName) {
    	_propertyConverterName = propertyConverterName;
    }
    
    public String getPropertyConverterName() {
		return _propertyConverterName;
	}

	public void setPropertyConverter(PropertyConverter propertyConverter) {
        if(propertyConverter == null) {
            throw new IllegalArgumentException();
        }
        _propertyConverter = propertyConverter;
    }
    
    protected PropertyConverter getPropertyConverter(
            ProcessorDefinition processorDef) {
    	if(_propertyConverter != null) {
            return _propertyConverter;
        }
        LibraryDefinition library = getPropertySet().getLibraryDefinition();
        String converterName = getPropertyConverterName();
        if(StringUtil.hasValue(converterName)) {
            PropertyConverter converter =
                library.getPropertyConverter(converterName);
            if(converter == null) {
                throw new ConverterNotFoundException(
                        converterName, getSystemID(), getLineNumber());
            }
            return converter;
        }
        Class propertyClass = getPropertyClass(processorDef);
        if(propertyClass != null) {
            return library.getPropertyConverter(propertyClass);
        }
        return null;
    }
    
    protected Class getPropertyClass(ProcessorDefinition processorDef) {
        Class processorClass = processorDef.getProcessorClass();
        return ObjectUtil.getPropertyClass(processorClass, getName());
    }

    protected QName getQName(SpecificationNode injected) {
        return SpecificationUtil.createQName(
                injected.getQName().getNamespaceURI(), _name);
    }
    
    public Object createProcessorProperty(
            ProcessorDefinition processorDef, SpecificationNode injected) {
    	if(injected == null) {
    		throw new IllegalArgumentException();
    	}
    	QName qName = getQName(injected);
        String value = getFinalValue();
        NodeAttribute attribute = injected.getAttribute(qName);
        if(value == null) {
            value = getDefaultValue();
            if(attribute != null) {
                value = attribute.getValue();
            }
        } else if(attribute != null) {
            String processorName = processorDef.getName();
            throw new FinalProcessorPropertyException(
                    processorName, qName);
        }
        if(value != null) {
	        Class propertyClass = getPropertyClass(processorDef);
            if(propertyClass == null) {
                // real property not found on the processor.
                Class processotClass = processorDef.getProcessorClass();
                if(VirtualPropertyAcceptable.class.isAssignableFrom(
                        processotClass) == false) {
                    if(LOG.isWarnEnabled()) {
                        LOG.warn(StringUtil.getMessage(
                                PropertyDefinitionImpl.class, 0,
                                processorDef.getName(), getName()));
                    }
                    return null;
                }
            }
        	PropertyConverter converter = getPropertyConverter(processorDef);
        	if(converter == null && propertyClass != null) {
        		LibraryManager manager = ProviderUtil.getLibraryManager();
        		converter = manager.getPropertyConverter(propertyClass);
        	}
        	if(converter == null) {
        		return value;
        	}
       		return converter.convert(attribute, value, getExpectedClass());
        } else if(_required) {
            String processorName = processorDef.getName();
            throw new NoRequiredPropertyException(processorName, qName);
        }
        return null;
    }
    
}
