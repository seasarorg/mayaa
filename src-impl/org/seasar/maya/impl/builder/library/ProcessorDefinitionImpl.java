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
import org.seasar.maya.builder.library.PropertySet;
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
public class ProcessorDefinitionImpl extends PropertySetImpl
        implements ProcessorDefinition {

    private static final Log LOG =
        LogFactory.getLog(ProcessorDefinitionImpl.class);
    
    private Class _processorClass;
    private List _propertySetNames;
    
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

    public void addPropertySetName(String name) {
        if(StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException();
        }
        if(_propertySetNames == null) {
            _propertySetNames = new ArrayList();
        }
        if(_propertySetNames.contains(name)) {
            // TODO åxçê
        } else {
            _propertySetNames.add(name);
        }
    }
    
    public Iterator iteratePropertySets() {
        if(_propertySetNames == null) {
            return NullIterator.getInstance();
        }
        Iterator it = _propertySetNames.iterator();
        return new PropertySetIterator(it, getLibraryDefinition());
    }

    protected TemplateProcessor newInstance() {
        return (TemplateProcessor)ObjectUtil.newInstance(_processorClass);
    }
    
    protected void settingProperty(SpecificationNode injected,
            TemplateProcessor processor, PropertyDefinition property) {
        Object value = property.createProcessorProperty(this, injected);
        if(value != null) {
            String propertyName = property.getName();
            Class processorClass = getProcessorClass();
            if(ObjectUtil.hasProperty(processorClass, propertyName)) {
                ObjectUtil.setProperty(processor, propertyName, value);
            } else if(processor instanceof VirtualPropertyAcceptable) {
                VirtualPropertyAcceptable acceptable =
                    (VirtualPropertyAcceptable)processor;
                acceptable.addProperty(propertyName, value);
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

    protected void settingPropertySet(SpecificationNode injected, 
            TemplateProcessor processor, PropertySet propertySet) {
        for(Iterator it = propertySet.iteratePropertyDefinition(); it.hasNext(); ) {
            PropertyDefinition property = (PropertyDefinition)it.next();
            settingProperty(injected, processor, property);
        }
    }

    protected void settingInformalProperties(SpecificationNode injected, 
            InformalPropertyAcceptable acceptable) {
        String injectedNS = injected.getQName().getNamespaceURI();
        for(Iterator it = injected.iterateAttribute(); it.hasNext(); ) {
            NodeAttribute attr = (NodeAttribute)it.next();
            if(contain(injectedNS, attr)) {
                continue;
            }
            acceptable.addInformalProperty(new ProcessorPropertyImpl(
            		attr, attr.getValue(), acceptable.getExpectedType()));
        }
    }

    public TemplateProcessor createTemplateProcessor(
            SpecificationNode injected) {
        if(injected == null) {
            throw new IllegalArgumentException();
        }
        TemplateProcessor processor = newInstance();
        settingPropertySet(injected, processor, this);
        for(Iterator it = iteratePropertySets(); it.hasNext(); ) {
            PropertySet propertySet = (PropertySet)it.next();
            settingPropertySet(injected, processor, propertySet);
        }
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
    
    // support class ------------------------------------------------

    protected class PropertySetIterator implements Iterator {

        private Iterator _it;
        private LibraryDefinition _library;
        
        public PropertySetIterator(Iterator it, LibraryDefinition library) {
            if(it == null || library == null) {
                throw new IllegalArgumentException();
            }
            _it = it;
            _library = library;
        }
        
        public boolean hasNext() {
            return _it.hasNext();
        }

        public Object next() {
            String name = (String)_it.next();
            PropertySet propertySet = _library.getPropertySet(name);
            if(propertySet == null) {
                throw new IllegalStateException();
            }
            return propertySet;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
}
