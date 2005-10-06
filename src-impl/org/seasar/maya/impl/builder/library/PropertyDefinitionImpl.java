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
import org.seasar.maya.impl.engine.specification.SpecificationUtil;
import org.seasar.maya.impl.provider.UnsupportedParameterException;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ProviderFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PropertyDefinitionImpl 
        implements PropertyDefinition, CONST_IMPL {

    private static final Log LOG =
        LogFactory.getLog(PropertyDefinitionImpl.class);
    
    private PropertySet _propertySet;
    private String _name;
    private boolean _required;
    private Class _expectedType;
    private String _defaultValue;
    private String _finalValue;
    private String _propertyConverterName;
    private PropertyConverter _propertyConverter;
    private int _lineNumber;

    public void setLineNumber(int lineNumber) {
    	if(lineNumber < 0) {
    		throw new IllegalArgumentException();
    	}
    	_lineNumber = lineNumber;
    }
    
    public int getLineNumber() {
		return _lineNumber;
	}

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

    public void setExpectedType(Class expectedType) {
        _expectedType = expectedType;
    }

    public Class getExpectedType() {
        if(_expectedType == null) {
            return Object.class;
        }
        return _expectedType;
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
    
    public PropertyConverter getPropertyConverter(
            ProcessorDefinition processorDef) {
        
    	// TODO converterName を用いての取得。
    	// TODO libraryDef内のコンバータ登録からの取得。
    	
    	if(_propertyConverter == null) {
            Class propertyType = getPropertyType(processorDef);
            if(propertyType == null) {
                return null;
            }
            LibraryManager manager = 
                ProviderFactory.getServiceProvider().getLibraryManager();
            return manager.getPropertyConverter(propertyType);
        }
        return _propertyConverter;
    }
    
    protected Class getPropertyType(ProcessorDefinition processorDef) {
        Class processorType = processorDef.getProcessorClass();
        return ObjectUtil.getPropertyType(processorType, getName());
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
	        Class propertyType = getPropertyType(processorDef);
            if(propertyType == null) {
                // real property not found on the processor.
                Class processotType = processorDef.getProcessorClass();
                if(VirtualPropertyAcceptable.class.isAssignableFrom(
                        processotType) == false) {
                    if(LOG.isWarnEnabled()) {
                        String[] params = new String[] {
                                processorDef.getName(), getName() };
                        LOG.warn(StringUtil.getMessage(getClass(), 0, params));
                    }
                    return null;
                }
            }
        	PropertyConverter converter = getPropertyConverter(processorDef);
        	if(converter == null && propertyType != null) {
        		ServiceProvider provider = ProviderFactory.getServiceProvider();
        		LibraryManager manager = provider.getLibraryManager();
        		converter = manager.getPropertyConverter(propertyType);
        	}
        	if(converter == null) {
        		return value;
        	}
       		return converter.convert(attribute, value, getExpectedType());
        } else if(_required) {
            String processorName = processorDef.getName();
            throw new NoRequiredPropertyException(processorName, qName);
        }
        return null;
    }

    // Parameterizable implements ------------------------------------
    
	public void setParameter(String name, String value) {
		throw new UnsupportedParameterException(getClass(), name);
	}
    
}
