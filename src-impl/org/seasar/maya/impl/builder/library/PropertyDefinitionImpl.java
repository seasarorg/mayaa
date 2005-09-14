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
package org.seasar.maya.impl.builder.library;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.maya.builder.library.ProcessorDefinition;
import org.seasar.maya.builder.library.PropertyDefinition;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.specification.Namespaceable;
import org.seasar.maya.engine.specification.NodeAttribute;
import org.seasar.maya.engine.specification.NodeNamespace;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.QNameable;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.builder.BuilderUtil;
import org.seasar.maya.impl.engine.processor.ProcessorPropertyImpl;
import org.seasar.maya.impl.engine.specification.QNameImpl;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PropertyDefinitionImpl 
        implements PropertyDefinition, CONST_IMPL {

    private static final Log LOG =LogFactory.getLog(PropertyDefinitionImpl.class);
    
    private ProcessorDefinition _processor;
    private String _name;
    private boolean _required;
    private Class _expectedType;
    private String _defaultValue;
    private String _finalValue;
    
    protected String getPrefix(Namespaceable namespaceable, QName qName) {
        for(Iterator it = namespaceable.iterateNamespace(true); it.hasNext(); ) {
            NodeNamespace ns = (NodeNamespace)it.next();
            if(ns.getNamespaceURI().equals(qName.getNamespaceURI())) {
                return ns.getPrefix(); 
            }
        }
        return null;
    }

    public void setProcessorDefinition(ProcessorDefinition processor) {
        if(processor == null) {
            throw new IllegalArgumentException();
        }
        _processor = processor;
    }
    
    public ProcessorDefinition getProcessorDefinition() {
        if(_processor == null) {
            throw new IllegalStateException();
        }
        return _processor;
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
    
    protected String getMessage(int index) {
        String[] params = new String[] {
                getProcessorDefinition().getName(), getName() };
        return StringUtil.getMessage(getClass(), index, params);
    }
    
    protected Class getPropertyType() {
        Class processorClass = getProcessorDefinition().getProcessorClass();
        Class ret = ObjectUtil.getPropertyType(processorClass, getName());
        if(ret == null) {
            if(LOG.isWarnEnabled()) {
                LOG.warn(getMessage(0));
            }
        }
        return ret;
    }

    protected QName getQName(SpecificationNode injected) {
        return new QNameImpl(injected.getQName().getNamespaceURI(), _name);
    }
    
    public Object createProcessorProperty(SpecificationNode injected) {
    	if(injected == null) {
    		throw new IllegalArgumentException();
    	}
    	QName qName = getQName(injected);
        String value = getFinalValue();
        NodeAttribute attribute = injected.getAttribute(qName);
        if(value == null) {
            value = getDefaultValue();
            attribute = injected.getAttribute(qName);
            if(attribute != null) {
                value = attribute.getValue();
            }
        } else if(attribute != null) {
            String processorName = getProcessorDefinition().getName();
            throw new FinalProcessorPropertyException(
                    processorName, qName);
        }
        if(value != null) {
	        Class propertyType = getPropertyType();
            if(propertyType == null) {
                // real property not found on the processor.
                return null;
            }
	        if(propertyType.equals(ProcessorProperty.class)) {
                NodeAttribute attr = injected.getAttribute(qName);
	            return new ProcessorPropertyImpl(attr, value, getExpectedType());
	        } else if(propertyType.equals(QNameable.class)) {
                return BuilderUtil.parseName(injected.getParentScope(), value); 
            }
	        return value;
        } else if(_required) {
            String processorName = getProcessorDefinition().getName();
            throw new NoRequiredPropertyException(processorName, qName);
        }
        return null;
    }
    
}
