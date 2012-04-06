/*
 * Copyright 2004-2012 the Seasar Foundation and the Others.
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.PositionAware;
import org.seasar.mayaa.builder.library.LibraryDefinition;
import org.seasar.mayaa.builder.library.LibraryManager;
import org.seasar.mayaa.builder.library.ProcessorDefinition;
import org.seasar.mayaa.builder.library.PropertySet;
import org.seasar.mayaa.builder.library.converter.PropertyConverter;
import org.seasar.mayaa.engine.specification.URI;
import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.impl.util.collection.NullIterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class LibraryDefinitionImpl extends ParameterAwareImpl
        implements LibraryDefinition {

    private static final long serialVersionUID = -2112164511711535994L;
    private static final Log LOG =
        LogFactory.getLog(LibraryDefinitionImpl.class);

    private URI _namespaceURI;
    private List _assignedURI = new ArrayList();
    private Map _converters;
    private Map _propertySets;
    private Map _processors;
    private String _systemID;

    public void setSystemID(String systemID) {
        if (StringUtil.isEmpty(systemID)) {
            throw new IllegalArgumentException();
        }
        _systemID = systemID;
    }

    public String getSystemID() {
        return _systemID;
    }

    public void setNamespaceURI(URI namespaceURI) {
        if (StringUtil.isEmpty(namespaceURI)) {
            throw new IllegalArgumentException();
        }
        _namespaceURI = namespaceURI;
    }

    public URI getNamespaceURI() {
        return _namespaceURI;
    }

    public void addAssignedURI(URI assignedURI) {
        if (StringUtil.isEmpty(assignedURI)) {
            throw new IllegalArgumentException();
        }
        if (assignedURI.equals(_namespaceURI) == false
                && _assignedURI.contains(assignedURI) == false) {
            _assignedURI.add(assignedURI);
        }
    }

    public Iterator iterateAssignedURI() {
        return _assignedURI.iterator();
    }

    protected void warnAlreadyRegistered(
            PositionAware obj, String name, int index) {
        if (LOG.isWarnEnabled()) {
            String systemID = obj.getSystemID();
            String lineNumber = Integer.toString(obj.getLineNumber());
            LOG.warn(StringUtil.getMessage(LibraryDefinitionImpl.class, index,
                    name, systemID, lineNumber));
        }
    }

    public void addPropertyConverter(
            String name, PropertyConverter converter) {
        if (converter == null) {
            throw new IllegalArgumentException();
        }
        if (StringUtil.isEmpty(name)) {
            name = converter.getPropetyClass().getName();
        }
        if (_converters == null) {
            _converters = new HashMap();
        }
        if (_converters.containsKey(name)) {
            warnAlreadyRegistered(converter, name, 1);
        } else {
            _converters.put(name, converter);
        }
    }

    public PropertyConverter getPropertyConverter(Class propertyClass) {
        if (propertyClass == null) {
            throw new IllegalArgumentException();
        }
        if (_converters != null) {
            for (Iterator it = _converters.values().iterator(); it.hasNext();) {
                PropertyConverter converter = (PropertyConverter) it.next();
                if (propertyClass.equals(converter.getPropetyClass())) {
                    return converter;
                }
            }
        }
        LibraryManager manager = ProviderUtil.getLibraryManager();
        return manager.getPropertyConverter(propertyClass);
    }

    public PropertyConverter getPropertyConverter(String converterName) {
        if (StringUtil.isEmpty(converterName)) {
            throw new IllegalArgumentException();
        }
        if (_converters != null && _converters.containsKey(converterName)) {
            return (PropertyConverter) _converters.get(converterName);
        }
        LibraryManager manager = ProviderUtil.getLibraryManager();
        return manager.getPropertyConverter(converterName);
    }

    public Iterator iteratePropertyConverters() {
        if (_converters == null) {
            return NullIterator.getInstance();
        }
        return _converters.values().iterator();
    }

    public void addPropertySet(PropertySet propertySet) {
        if (propertySet == null) {
            throw new IllegalArgumentException();
        }
        String name = propertySet.getName();
        if (_propertySets == null) {
            _propertySets = new HashMap();
        }
        if (_propertySets.containsKey(name)) {
            warnAlreadyRegistered(propertySet, name, 2);
        } else {
            _propertySets.put(name, propertySet);
        }
    }

    public Iterator iteratePropertySets() {
        if (_propertySets == null) {
            return NullIterator.getInstance();
        }
        return _propertySets.values().iterator();
    }

    public PropertySet getPropertySet(String name) {
        if (StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException();
        }
        if (_propertySets == null) {
            return null;
        }
        return (PropertySet) _propertySets.get(name);
    }

    public void addProcessorDefinition(ProcessorDefinition processor) {
        if (processor == null) {
            throw new IllegalArgumentException();
        }
        String name = processor.getName();
        if (_processors == null) {
            _processors = new HashMap();
        }
        if (_processors.containsKey(name)) {
            warnAlreadyRegistered(processor, name, 3);
        } else {
            _processors.put(name, processor);
        }
    }

    public Iterator iterateProcessorDefinitions() {
        if (_processors == null) {
            return NullIterator.getInstance();
        }
        return _processors.values().iterator();
    }

    public ProcessorDefinition getProcessorDefinition(String name) {
        if (StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException();
        }
        if (_processors == null) {
            return null;
        }
        return (ProcessorDefinition) _processors.get(name);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("LibraryDefinition: ");
        sb.append("systemID = ");
        sb.append(_systemID);
        sb.append(", namespaceUrl = ");
        sb.append(_namespaceURI);
        sb.append(", assignedURI = (");
        for (Iterator it = _assignedURI.iterator(); it.hasNext();) {
            sb.append(it.next());
            sb.append(", ");
        }
        sb.append("), ");
        if (_processors != null) {
            sb.append("processors = (");
            for (Iterator it = _processors.keySet().iterator(); it.hasNext();) {
                String key = (String) it.next();
                ProcessorDefinition def = getProcessorDefinition(key);
                sb.append(def.getName());
                sb.append(" - ");
                sb.append(def.getProcessorClass());
                sb.append(", ");
            }
            sb.append("), ");
        }
        if (_propertySets != null) {
            sb.append("propertySets = (");
            for (Iterator it = _propertySets.keySet().iterator(); it.hasNext();) {
                String key = (String) it.next();
                PropertySet prop = getPropertySet(key);
                sb.append(prop.getName());
                sb.append(" = ");
                sb.append(prop.getParameter(prop.getName()));
                sb.append(", ");
            }
            sb.append("), ");
        }
        return sb.toString();
    }

}
