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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.seasar.maya.builder.library.LibraryDefinition;
import org.seasar.maya.builder.library.ProcessorDefinition;
import org.seasar.maya.builder.library.PropertyDefinition;
import org.seasar.maya.builder.processor.ProcessorFactory;
import org.seasar.maya.cycle.script.CompiledScript;
import org.seasar.maya.engine.Template;
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

    private static final Map _factoryInstances = new HashMap();
    
    private LibraryDefinition _libraryDefinition;
    private String _name;
    private String _className;
    private List _properties;

    public void setName(String name) {
        if(StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException();
        }
        _name = name;
    }

    public LibraryDefinition getLibraryDefinition() {
        if(_libraryDefinition == null) {
            throw new IllegalStateException();
        }
        return _libraryDefinition;
    }
    
    public void setLibraryDefinition(LibraryDefinition libraryDefinition) {
        if(libraryDefinition == null) {
            throw new IllegalArgumentException();
        }
        _libraryDefinition = libraryDefinition;
    }
    
    public String getName() {
        return _name;
    }
    
    public void setClassName(String className) {
        if(StringUtil.isEmpty(className)) {
            throw new IllegalArgumentException();
        }
        _className = className;
    }

    public String getClassName() {
        return _className;
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

    protected TemplateProcessor newInstance(Template template, SpecificationNode injected) {
        Object obj = _factoryInstances.get(_className);
        if(obj == null) {
            Class defineClass = ObjectUtil.loadClass(_className);
            if(TemplateProcessor.class.isAssignableFrom(defineClass) == false && 
                    ProcessorFactory.class.isAssignableFrom(defineClass) == false) {
                throw new IllegalStateException();
            }
            obj = ObjectUtil.newInstance(defineClass);
        }
        if(obj instanceof TemplateProcessor) {
            return (TemplateProcessor)obj;
        }
        _factoryInstances.put(_className, obj);
        ProcessorFactory factory = (ProcessorFactory)obj;
        return factory.createProcessor(template, injected);
    }

    protected void settingProperties(
            SpecificationNode injected, TemplateProcessor processor) {
        for(Iterator it = iteratePropertyDefinition(); it.hasNext(); ) {
            PropertyDefinition property = (PropertyDefinition)it.next();
            Object prop = property.getProcessorProperty(injected, processor);
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
	                obj = script.getText();
	            }
	            acceptable.addInformalProperty(new ProcessorPropertyImpl(qName, prefix, obj));
            }
        }
    }
    
    public TemplateProcessor createTemplateProcessor(
            Template template, SpecificationNode injected) {
        TemplateProcessor processor = newInstance(template, injected);
        settingProperties(injected, processor);
        if(processor instanceof InformalPropertyAcceptable) {
            settingInformalProperties(injected, (InformalPropertyAcceptable)processor);
        }
        return processor;
    }

}
