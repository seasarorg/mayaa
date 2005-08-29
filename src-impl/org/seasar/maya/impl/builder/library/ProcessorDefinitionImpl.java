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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.seasar.maya.builder.library.ProcessorDefinition;
import org.seasar.maya.builder.library.PropertyDefinition;
import org.seasar.maya.cycle.script.CompiledScript;
import org.seasar.maya.engine.processor.InformalPropertyAcceptable;
import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.engine.specification.NodeAttribute;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.engine.processor.ProcessorPropertyImpl;
import org.seasar.maya.impl.util.ScriptUtil;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.collection.NullIterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ProcessorDefinitionImpl implements ProcessorDefinition {
    
    private String _name;
    private Class _processorClass;
    private List _properties;

    public void setName(String name) {
        if(StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException();
        }
        _name = name;
    }
    
    public String getName() {
        return _name;
    }
    
    public void setProcessorClass(Class processorClass) {
        if(processorClass == null || 
                TemplateProcessor.class.isAssignableFrom(processorClass) == false) {
            throw new IllegalArgumentException();
        }
        _processorClass = processorClass;
    }

    public Class getProcessorClass() {
        return _processorClass;
    }
    
    public void addPropertyDefinitiion(PropertyDefinition property) {
        if(property == null) {
            throw new IllegalArgumentException();
        }
        if(_properties == null) {
            _properties = new ArrayList();
        }
        _properties.add(property);
    }
    
    public Iterator iteratePropertyDefinition() {
        if(_properties == null) {
            return NullIterator.getInstance();
        }
        return _properties.iterator();
    }

    protected TemplateProcessor newInstance(SpecificationNode injected) {
        return (TemplateProcessor)ObjectUtil.newInstance(_processorClass);
    }

    protected Class getTargetType(TemplateProcessor processor) {
        return processor.getClass();
    }
    
    protected void settingProperties(SpecificationNode injected, 
            TemplateProcessor processor) {
        for(Iterator it = iteratePropertyDefinition(); it.hasNext(); ) {
            PropertyDefinition property = (PropertyDefinition)it.next();
            Object prop = property.createProcessorProperty(injected);
            if(prop != null) {
                ObjectUtil.setProperty(processor, property.getName(), prop);
            }
        }
    }
    
    protected void settingInformalProperties(
            SpecificationNode injected, InformalPropertyAcceptable acceptable) {
        for(Iterator it = injected.iterateAttribute(); it.hasNext(); ) {
            NodeAttribute attr = (NodeAttribute)it.next();
            QName qName = attr.getQName();
            if(acceptable.getInformalAttrituteURI().equals(qName.getNamespaceURI())) {
	            String prefix = injected.getPrefix();
	            CompiledScript script = ScriptUtil.compile(attr.getValue(), Object.class);
	            Object obj = script;
	            if(script == null) {
	            	obj = "";
	            } else if(script.isLiteral()) {
	                obj = script.execute(null);
	            }
	            acceptable.addInformalProperty(new ProcessorPropertyImpl(qName, prefix, obj));
            }
        }
    }
    
    public TemplateProcessor createTemplateProcessor(SpecificationNode injected) {
        if(injected == null) {
            throw new IllegalArgumentException();
        }
        TemplateProcessor processor = newInstance(injected);
        settingProperties(injected, processor);
        if(processor instanceof InformalPropertyAcceptable) {
            settingInformalProperties(injected, (InformalPropertyAcceptable)processor);
        }
        return processor;
    }

}
