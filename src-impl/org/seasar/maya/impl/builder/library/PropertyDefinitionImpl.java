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

import org.seasar.maya.builder.library.PropertyDefinition;
import org.seasar.maya.cycle.script.CompiledScript;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.specification.Namespaceable;
import org.seasar.maya.engine.specification.NodeAttribute;
import org.seasar.maya.engine.specification.NodeNamespace;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.engine.processor.ProcessorPropertyImpl;
import org.seasar.maya.impl.util.ScriptUtil;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PropertyDefinitionImpl implements PropertyDefinition {

    private String _name;
    private boolean _required;
    private Class _expectedType;
    private String _defaultValue;
    private Class _processorClass;    
    
    protected String getPrefix(Namespaceable namespaceable, QName qName) {
        Iterator it = namespaceable.iterateNamespace(qName.getNamespaceURI());
        String prefix = null;
        if(it.hasNext()) {
            prefix = ((NodeNamespace)it.next()).getPrefix();
        }
        return prefix;
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
    
    public void setProcessorClass(Class processorClass) {
        if(processorClass == null) {
            throw new IllegalArgumentException();
        }
        _processorClass = processorClass;
    }
    
    protected Class getPropertyType() {
        if(_processorClass == null) {
            throw new IllegalStateException();
        }
        return ObjectUtil.getPropertyType(_processorClass, getName());
    }

    protected QName getQName(SpecificationNode injected) {
        return new QName(injected.getQName().getNamespaceURI(), _name);
    }
    
    protected String getProcessValue(SpecificationNode injected, QName qName) {
        String value = _defaultValue;
        if(injected != null) {
            NodeAttribute attribute = injected.getAttribute(qName);
            if(attribute != null) {
                value = attribute.getValue();
            }
        }
        if(value == null && _required) {
            throw new NoRequiredPropertyException(injected, qName);
        }
        return value;
    }
    
    public Object createProcessorProperty(SpecificationNode injected) {
        QName qName = getQName(injected);
        String stringValue = getProcessValue(injected, qName);
        if(stringValue != null) {
	        Class propertyType = getPropertyType();
	        if(propertyType == null || 
                    propertyType.equals(ProcessorProperty.class)) {
	            CompiledScript script  = 
                    ScriptUtil.compile(stringValue, getExpectedType());
	            String prefix = getPrefix(injected, qName);
	            return new ProcessorPropertyImpl(qName, prefix, script);
	        }
	        return stringValue;
        }
        return null;
    }
    
}
