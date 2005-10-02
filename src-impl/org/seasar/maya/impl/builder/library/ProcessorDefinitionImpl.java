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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.maya.builder.library.LibraryDefinition;
import org.seasar.maya.builder.library.ProcessorDefinition;
import org.seasar.maya.builder.library.PropertyDefinition;
import org.seasar.maya.engine.processor.InformalPropertyAcceptable;
import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.engine.processor.VirtualPropertyAcceptable;
import org.seasar.maya.engine.specification.NodeAttribute;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.engine.processor.ProcessorPropertyImpl;
import org.seasar.maya.impl.provider.UnsupportedParameterException;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.collection.NullIterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ProcessorDefinitionImpl implements ProcessorDefinition {

    private static final Log LOG =
        LogFactory.getLog(ProcessorDefinitionImpl.class);
    
    private LibraryDefinition _library;
    private String _name;
    private Class _processorClass;
    private List _properties;
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

	public void setLibraryDefinition(LibraryDefinition library) {
        if(library == null) {
            throw new IllegalArgumentException();
        }
        _library = library;
    }
    
    public LibraryDefinition getLibraryDefinition() {
        if(_library == null) {
            throw new IllegalStateException();
        }
        return _library;
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
    
    public void setProcessorClass(Class processorClass) {
        if(processorClass == null || 
                TemplateProcessor.class.isAssignableFrom(
                        processorClass) == false) {
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
    
    protected void settingProperties(SpecificationNode injected, 
            TemplateProcessor processor) {
        for(Iterator it = iteratePropertyDefinition(); it.hasNext(); ) {
            PropertyDefinition property = (PropertyDefinition)it.next();
            Object value = property.createProcessorProperty(injected);
            if(value != null) {
                String propertyName = property.getName();
                Class processorClass = getProcessorClass();
                if(ObjectUtil.hasProperty(processorClass, propertyName)) {
                    ObjectUtil.setProperty(processor, propertyName, value);
                } else if(processor instanceof VirtualPropertyAcceptable) {
                    VirtualPropertyAcceptable acceptable =
                        (VirtualPropertyAcceptable)processor;
                    acceptable.addProperty(propertyName, value.toString());
                } else {
                    if(LOG.isWarnEnabled()) {
                        String[] params = new String[] {
                                processorClass.getName(), propertyName };
                        LOG.warn(StringUtil.getMessage(
                                ProcessorDefinitionImpl.class, 0, params));
                    }
                }
            }
        }
    }
    
    protected void settingInformalProperties(SpecificationNode injected, 
            InformalPropertyAcceptable acceptable) {
        // TODO MLDプロパティは除外する。        
        for(Iterator it = injected.iterateAttribute(); it.hasNext(); ) {
            NodeAttribute attr = (NodeAttribute)it.next();
            acceptable.addInformalProperty(
                    new ProcessorPropertyImpl(
                            attr, attr.getValue(), Object.class));
        }
    }
    
    public TemplateProcessor createTemplateProcessor(
            SpecificationNode injected) {
        if(injected == null) {
            throw new IllegalArgumentException();
        }
        TemplateProcessor processor = newInstance(injected);
        settingProperties(injected, processor);
        if(processor instanceof InformalPropertyAcceptable) {
            settingInformalProperties(
                    injected, (InformalPropertyAcceptable)processor);
        }
        return processor;
    }

    // Parameterizable implements ------------------------------------
    
	public void setParameter(String name, String value) {
		throw new UnsupportedParameterException(getClass(), name);
	}

}
