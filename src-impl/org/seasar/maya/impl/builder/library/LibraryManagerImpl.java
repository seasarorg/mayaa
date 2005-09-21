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
import org.seasar.maya.builder.library.DefinitionBuilder;
import org.seasar.maya.builder.library.LibraryDefinition;
import org.seasar.maya.builder.library.LibraryManager;
import org.seasar.maya.builder.library.ProcessorDefinition;
import org.seasar.maya.builder.library.scanner.SourceScanner;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.impl.provider.UnsupportedParameterException;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.collection.AbstractScanningIterator;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class LibraryManagerImpl implements LibraryManager {

    private static Log LOG = LogFactory.getLog(LibraryManagerImpl.class);
    
	private List _scanners;
	private List _builders;
    private List _libraries;
    
    public LibraryManagerImpl() {
    	_scanners = new ArrayList();
    	_builders = new ArrayList();
    }
    
    public void setParameter(String name, String value) {
        throw new UnsupportedParameterException(getClass(), name);
    }

    protected void buildAll() {
        _libraries = new ArrayList();
    	for(int i = 0; i < _scanners.size(); i++) {
	    	SourceScanner scanner = (SourceScanner)_scanners.get(i);
	    	for(Iterator it = scanner.scan(); it.hasNext(); ) {
		    	SourceDescriptor source = (SourceDescriptor)it.next();
		    	for(int k = 0; k < _builders.size(); k++) {
			    	DefinitionBuilder builder = (DefinitionBuilder)_builders.get(k);
			    	LibraryDefinition library = builder.build(source);
			    	if(library != null) {
			            _libraries.add(library);
                        if(LOG.isTraceEnabled()) {
                            LOG.trace("loaded library - " + source.getSystemID() + 
                                    " - " + library.getNamespaceURI());
                        }
			            break;
			    	}
		    	}
	    	}
    	}
    }
    
    public void addSourceScanner(SourceScanner scanner) {
		if(scanner == null) {
			throw new IllegalArgumentException();
		}
		synchronized (_scanners) {
            if(LOG.isTraceEnabled()) {
                LOG.trace("adding SourceScanner[" + _scanners.size() + "] - " +
                        scanner.getClass());
            }
			_scanners.add(scanner);
		}
	}

    public void addDefinitionBuilder(DefinitionBuilder builder) {
    	if(builder == null) {
    		throw new IllegalArgumentException();
    	}
    	synchronized(_builders) {
            if(LOG.isTraceEnabled()) {
                LOG.trace("adding DefinitionBuilder[" + _builders.size() + "] - " +
                        builder.getClass());
            }
    		_builders.add(builder);
    	}
    }
    
    public Iterator iterateLibraryDefinition() {
        if(_libraries == null) {
            buildAll();
        }
        return _libraries.iterator();
    }
    
    public Iterator iterateLibraryDefinition(String namespaceURI) {
        if(StringUtil.isEmpty(namespaceURI)) {
            throw new IllegalArgumentException();
        }
		return new LibraryDefinitionFilteredIterator(
                namespaceURI, iterateLibraryDefinition());
    }
    
    public ProcessorDefinition getProcessorDefinition(QName qName) {
        if(qName == null) {
            throw new IllegalArgumentException();
        }
        String namespaceURI = qName.getNamespaceURI();
        String localName = qName.getLocalName();
        for(Iterator it = iterateLibraryDefinition(namespaceURI); it.hasNext(); ) {
            LibraryDefinition library = (LibraryDefinition)it.next();
            ProcessorDefinition processor = library.getProcessorDefinition(localName);
            if(processor != null) {
                return processor;
            }
        }
        return null;
    }
    
    private class LibraryDefinitionFilteredIterator extends AbstractScanningIterator {
        
        private String _namespaceURI;
        
        private LibraryDefinitionFilteredIterator(String namespaceURI, Iterator iterator) {
            super(iterator);
            if(StringUtil.isEmpty(namespaceURI)) {
                throw new IllegalArgumentException();
            }
            _namespaceURI = namespaceURI;
        }
        
        protected boolean filter(Object test) {
            if(test == null || (test instanceof LibraryDefinition == false)) {
                return false;
            }
            LibraryDefinition library = (LibraryDefinition)test;
            if(_namespaceURI.equals(library.getNamespaceURI())) {
                return true;
            }
            for(Iterator it = library.iterateAssignedURI(); it.hasNext(); ) {
                if(_namespaceURI.equals(it.next())) {
                    return true;
                }
            }
            return false;
        }
    }
    
}
