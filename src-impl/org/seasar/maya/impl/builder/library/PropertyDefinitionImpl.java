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

import org.seasar.maya.builder.library.ProcessorDefinition;
import org.seasar.maya.builder.library.PropertyDefinition;
import org.seasar.maya.cycle.el.CompiledExpression;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.engine.specification.Namespaceable;
import org.seasar.maya.engine.specification.NodeAttribute;
import org.seasar.maya.engine.specification.NodeNamespace;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.engine.processor.ProcessorPropertyImpl;
import org.seasar.maya.impl.util.ExpressionUtil;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PropertyDefinitionImpl implements PropertyDefinition {

    private ProcessorDefinition _processorDefinition;
    private String _name;
    private boolean _required;
    private String _expectedType;
    private String _defaultValue;
    
    public void setProcessorDefinition(ProcessorDefinition processorDefinition) {
        if(processorDefinition == null) {
            throw new IllegalArgumentException();
        }
        _processorDefinition = processorDefinition;
    }

    public ProcessorDefinition getProcessorDefinition() {
        if(_processorDefinition == null) {
            throw new IllegalStateException();
        }
        return _processorDefinition;
    }
    
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
        return _name;
    }

    public void setRequired(boolean required) {
        _required = required;
    }
    
    public boolean isRequired() {
        return _required;
    }

    public void setExpectedType(String expectedType) {
    	if(StringUtil.isEmpty(expectedType)) {
    		expectedType = "java.lang.Object";
    	}
        _expectedType = expectedType;
    }

    public String getExpectedType() {
        return _expectedType;
    }
    
    public void setDefaultValue(String defaultValue) {
        _defaultValue = defaultValue;
    }
    
    public String getDefaultValue() {
        return _defaultValue;
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
    
    public Object getProcessorProperty(SpecificationNode injected, 
            TemplateProcessor processor) {
        QName qName = getQName(injected);
        String stringValue = getProcessValue(injected, qName);
        if(stringValue != null) {
	        Class propertyType = ObjectUtil.getPropertyType(processor, _name);
            if( propertyType == null ) {
                throw new IllegalStateException(processor.getClass().getName()+":"+_name+"‚ÌŒ^‚ª‚æ‚­•ª‚©‚ñ‚È‚¢(--;");
            }
	        if(propertyType.equals(ProcessorProperty.class)) {
	        	Class clazz = ObjectUtil.loadClass(_expectedType);
	            CompiledExpression expression  = 
	                ExpressionUtil.parseExpression(stringValue, clazz);
	            String prefix = getPrefix(injected, qName);
	            return new ProcessorPropertyImpl(qName, prefix, expression);
	        }
	        return stringValue;
        }
        return null;
    }
    
}
