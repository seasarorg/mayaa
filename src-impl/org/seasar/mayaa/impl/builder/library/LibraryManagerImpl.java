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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.PositionAware;
import org.seasar.mayaa.builder.library.DefinitionBuilder;
import org.seasar.mayaa.builder.library.LibraryDefinition;
import org.seasar.mayaa.builder.library.LibraryManager;
import org.seasar.mayaa.builder.library.ProcessorDefinition;
import org.seasar.mayaa.builder.library.converter.PropertyConverter;
import org.seasar.mayaa.builder.library.scanner.SourceScanner;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.URI;
import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.builder.library.scanner.SourceAlias;
import org.seasar.mayaa.impl.builder.library.scanner.WebXMLTaglibSourceScanner;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.impl.util.collection.AbstractScanningIterator;
import org.seasar.mayaa.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class LibraryManagerImpl extends ParameterAwareImpl
        implements LibraryManager {

    private static final long serialVersionUID = -2346419518053103604L;
    private static final Log LOG = LogFactory.getLog(LibraryManagerImpl.class);

    private List _scanners;
    private List _builders;
    private List _libraries;
    private Map _converters;

    public LibraryManagerImpl() {
        _scanners = new ArrayList();
        _builders = new ArrayList();
        _converters = new HashMap();
    }

    protected void warnAlreadyRegistered(
            PositionAware obj, String name, int index) {
        if (LOG.isWarnEnabled()) {
            String systemID = obj.getSystemID();
            String lineNumber = Integer.toString(obj.getLineNumber());
            LOG.warn(StringUtil.getMessage(LibraryManagerImpl.class, index,
                    name, systemID, lineNumber));
        }
    }

    public void addPropertyConverter(
            String name, PropertyConverter propertyConverter) {
        if (propertyConverter == null) {
            throw new IllegalArgumentException();
        }
        if (StringUtil.isEmpty(name)) {
            name = propertyConverter.getPropetyClass().getName();
        }
        if (_converters.containsKey(name)) {
            warnAlreadyRegistered(propertyConverter, name, 1);
        } else {
            _converters.put(name, propertyConverter);
        }
    }

    public PropertyConverter getPropertyConverter(String converterName) {
        if (StringUtil.isEmpty(converterName)) {
            throw new IllegalArgumentException();
        }
        return (PropertyConverter) _converters.get(converterName);
    }

    public PropertyConverter getPropertyConverter(Class propertyClass) {
        if (propertyClass == null) {
            throw new IllegalArgumentException();
        }
        for (Iterator it = _converters.values().iterator(); it.hasNext();) {
            PropertyConverter propertyConverter = (PropertyConverter) it.next();
            Class converterClass = propertyConverter.getPropetyClass();
            if (propertyClass.isAssignableFrom(converterClass)) {
                return propertyConverter;
            }
        }
        return null;
    }

    public Iterator iteratePropertyConverters() {
        return _converters.values().iterator();
    }

    public void addSourceScanner(SourceScanner scanner) {
        if (scanner == null) {
            throw new IllegalArgumentException();
        }
        synchronized (_scanners) {
            if (_scanners.contains(scanner)) {
                warnAlreadyRegistered(scanner, scanner.getClass().getName(), 1);
            } else {
                _scanners.add(scanner);
            }
        }
    }

    public void addDefinitionBuilder(DefinitionBuilder builder) {
        if (builder == null) {
            throw new IllegalArgumentException();
        }
        synchronized (_builders) {
            if (_builders.contains(builder)) {
                warnAlreadyRegistered(builder, builder.getClass().getName(), 3);
            } else {
                _builders.add(builder);
            }
        }
    }

    public void prepareLibraries() {
        if (_libraries == null) {
            buildAll();
        }
    }

    protected void buildAll() {
        _libraries = new ArrayList();
        Set builtLibraries = new HashSet();
        for (int i = 0; i < _scanners.size(); i++) {
            SourceScanner scanner = (SourceScanner) _scanners.get(i);
            for (Iterator it = scanner.scan(); it.hasNext();) {
                SourceDescriptor source = (SourceDescriptor) it.next();
                boolean built = false;
                for (int k = 0; k < _builders.size(); k++) {
                    DefinitionBuilder builder =
                        (DefinitionBuilder) _builders.get(k);
                    LibraryDefinition library = builder.build(source);
                    if (library != null) {
                        if (builtLibraries.contains(
                                library.getNamespaceURI()) == false) {
                            _libraries.add(library);
                            builtLibraries.add(library.getNamespaceURI());
                            if (LOG.isInfoEnabled()) {
                                LOG.info(StringUtil.getMessage(
                                    LibraryManagerImpl.class, 4,
                                    source.getSystemID(),
                                    String.valueOf(library.getNamespaceURI())));
                            }
                        }
                        built = true;
                        break;
                    }
                }

                if (built == false) {
                    assignTaglibLocation(source);
                }
            }
        }
    }

    // condition: already loaded "META-INF/taglib.tld"
    private void assignTaglibLocation(SourceDescriptor source) {
        String realPath =
            source.getParameter(WebXMLTaglibSourceScanner.REAL_PATH);
        if (StringUtil.isEmpty(realPath)
                || realPath.endsWith(".jar") == false) {
            return;
        }

        for (int j = 0; j < _libraries.size(); j++) {
            LibraryDefinition library = (LibraryDefinition) _libraries.get(j);
            for (Iterator it = library.iterateAssignedURI(); it.hasNext();) {
                URI uri = (URI) it.next();
                if (realPath.equals(String.valueOf(uri))) {
                    URI assignedURI = SpecificationUtil.createURI(
                            source.getParameter(SourceAlias.ALIAS));
                    library.addAssignedURI(assignedURI);
                    if (LOG.isInfoEnabled()) {
                        LOG.info(StringUtil.getMessage(
                            LibraryManagerImpl.class, 4,
                            library.getNamespaceURI() + " (alias)",
                            String.valueOf(assignedURI)));
                    }
                    return;
                }
            }
        }
    }

    public Iterator iterateLibraryDefinition() {
        prepareLibraries();
        return _libraries.iterator();
    }

    public Iterator iterateLibraryDefinition(URI namespaceURI) {
        if (StringUtil.isEmpty(namespaceURI)) {
            throw new IllegalArgumentException();
        }
        return new LibraryDefinitionFilteredIterator(
                namespaceURI, iterateLibraryDefinition());
    }

    public ProcessorDefinition getProcessorDefinition(QName qName) {
        if (qName == null) {
            throw new IllegalArgumentException();
        }
        URI namespaceURI = qName.getNamespaceURI();
        String localName = qName.getLocalName();
        for (Iterator it = iterateLibraryDefinition(namespaceURI);
                it.hasNext();)  {
            LibraryDefinition library = (LibraryDefinition) it.next();
            ProcessorDefinition processor =
                library.getProcessorDefinition(localName);
            if (processor != null) {
                return processor;
            }
        }
        return null;
    }

    // support class ------------------------------------------------

    private static class LibraryDefinitionFilteredIterator
            extends AbstractScanningIterator {

        private URI _namespaceURI;

        public LibraryDefinitionFilteredIterator(
                URI namespaceURI, Iterator iterator) {
            super(iterator);
            if (StringUtil.isEmpty(namespaceURI)) {
                throw new IllegalArgumentException();
            }
            _namespaceURI = namespaceURI;
        }

        protected boolean filter(Object test) {
            if (test == null || (test instanceof LibraryDefinition == false)) {
                return false;
            }
            LibraryDefinition library = (LibraryDefinition) test;
            if (_namespaceURI.equals(library.getNamespaceURI())) {
                return true;
            }
            for (Iterator it = library.iterateAssignedURI(); it.hasNext();) {
                if (_namespaceURI.equals(it.next())) {
                    return true;
                }
            }
            return false;
        }

    }

}
