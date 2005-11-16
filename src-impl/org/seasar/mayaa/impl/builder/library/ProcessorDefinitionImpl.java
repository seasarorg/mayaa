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
package org.seasar.mayaa.impl.builder.library;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.builder.library.LibraryDefinition;
import org.seasar.mayaa.builder.library.ProcessorDefinition;
import org.seasar.mayaa.builder.library.PropertyDefinition;
import org.seasar.mayaa.builder.library.PropertySet;
import org.seasar.mayaa.builder.library.converter.PropertyConverter;
import org.seasar.mayaa.engine.processor.InformalPropertyAcceptable;
import org.seasar.mayaa.engine.processor.TemplateProcessor;
import org.seasar.mayaa.engine.processor.VirtualPropertyAcceptable;
import org.seasar.mayaa.engine.specification.NodeAttribute;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.PrefixAwareName;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.util.ObjectUtil;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.impl.util.collection.NullIterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ProcessorDefinitionImpl extends PropertySetImpl
        implements ProcessorDefinition {

    private static final Log LOG =
        LogFactory.getLog(ProcessorDefinitionImpl.class);
    
    private Class _processorClass;
    private List _propertySetRefs;
    
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

    public void addPropertySetRef(
            String name, String systemID, int lineNumber) {
        if(StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException();
        }
        if(_propertySetRefs == null) {
            _propertySetRefs = new ArrayList();
        }
        if(_propertySetRefs.contains(name)) {
            if(LOG.isWarnEnabled()) {
                String line = Integer.toString(lineNumber);
                LOG.warn(StringUtil.getMessage(
                        ProcessorDefinitionImpl.class, 1, name, systemID, line));
            }
        } else {
            _propertySetRefs.add(
                    new PropertySetRef(name, systemID, lineNumber));
        }
    }
    
    public Iterator iteratePropertySets() {
        if(_propertySetRefs == null) {
            return NullIterator.getInstance();
        }
        Iterator it = _propertySetRefs.iterator();
        return new PropertySetIterator(it, getLibraryDefinition());
    }

    protected TemplateProcessor newInstance() {
        return (TemplateProcessor)ObjectUtil.newInstance(_processorClass);
    }

    protected PrefixAwareName getPrefixAwareName(
            SpecificationNode injected, String propertyName) {
        QName qName = SpecificationUtil.createQName(
                injected.getQName().getNamespaceURI(), propertyName);
        return injected.getAttribute(qName);
    }
    
    protected void settingProperty(
            SpecificationNode original, SpecificationNode injected,
            TemplateProcessor processor, PropertyDefinition property) {
        Object value = 
            property.createProcessorProperty(this, original, injected);
        if(value != null) {
            String propertyName = property.getName();
            Class processorClass = getProcessorClass();
            if(ObjectUtil.hasProperty(processorClass, propertyName)) {
                ObjectUtil.setProperty(processor, propertyName, value);
            } else if(processor instanceof VirtualPropertyAcceptable) {
                VirtualPropertyAcceptable acceptable =
                    (VirtualPropertyAcceptable)processor;
                PrefixAwareName name =
                    getPrefixAwareName(injected, propertyName); 
                acceptable.addVirtualProperty(name, value);
            } else {
                if(LOG.isWarnEnabled()) {
                    LOG.warn(StringUtil.getMessage(
                            ProcessorDefinitionImpl.class, 2,
                            processorClass.getName(), propertyName));
                }
            }
        }
    }

    protected void settingPropertySet(
            SpecificationNode original, SpecificationNode injected, 
            TemplateProcessor processor, PropertySet propertySet) {
        for(Iterator it = propertySet.iteratePropertyDefinition(); it.hasNext(); ) {
            PropertyDefinition property = (PropertyDefinition)it.next();
            settingProperty(original, injected, processor, property);
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
            LibraryDefinition library = getLibraryDefinition();
            Class propertyClass = acceptable.getPropertyClass();
            PropertyConverter converter = 
                library.getPropertyConverter(propertyClass);
            if(converter == null) {
                throw new ConverterNotFoundException(
                        propertyClass.getName(), getSystemID(), getLineNumber());
            }
            Class expectedClass = acceptable.getExpectedClass();
            String value = attr.getValue();
            Object property = converter.convert(attr, value, expectedClass);
            if(property == null) {
                throw new ConverterOperationException(converter, value);
            }
            acceptable.addInformalProperty(attr, property);
        }
    }

    public TemplateProcessor createTemplateProcessor(
            SpecificationNode original, SpecificationNode injected) {
        if(injected == null) {
            throw new IllegalArgumentException();
        }
        TemplateProcessor processor = newInstance();
        processor.setProcessorDefinition(this);
        settingPropertySet(original, injected, processor, this);
        for(Iterator it = iteratePropertySets(); it.hasNext(); ) {
            PropertySet propertySet = (PropertySet)it.next();
            settingPropertySet(original, injected, processor, propertySet);
        }
        if(processor instanceof InformalPropertyAcceptable) {
            settingInformalProperties(
                    injected, (InformalPropertyAcceptable)processor);
        }
        return processor;
    }
    
    // support class ------------------------------------------------

    protected class PropertySetRef {
        
        private String _name;
        private String _systemID;
        private int _lineNumber;
        
        public PropertySetRef(String name, String systemID, int lineNumber) {
            if(StringUtil.isEmpty(name) || 
                    StringUtil.isEmpty(systemID) || lineNumber < 0) {
                throw new IllegalArgumentException();
            }
            _name = name;
            _systemID = systemID;
            _lineNumber = lineNumber;
        }
        
        public String getName() {
            return _name;
        }
        
        public String getSystemID() {
            return _systemID;
        }
        
        public int getLineNumber() {
            return _lineNumber;
        }
        
    }
    
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
            PropertySetRef ref = (PropertySetRef)_it.next();
            PropertySet propertySet = _library.getPropertySet(ref.getName());
            if(propertySet == null) {
                throw new PropertySetNotFoundException(ref.getName(), 
                        ref.getSystemID(), ref.getLineNumber());
            }
            return propertySet;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
}
