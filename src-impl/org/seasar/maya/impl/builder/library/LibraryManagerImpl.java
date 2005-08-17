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

import org.seasar.maya.builder.library.LibraryDefinition;
import org.seasar.maya.builder.library.DefinitionBuilder;
import org.seasar.maya.builder.library.SourceScanner;
import org.seasar.maya.builder.library.LibraryManager;
import org.seasar.maya.builder.library.ProcessorDefinition;
import org.seasar.maya.builder.library.scanner.LibraryScanner;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.impl.builder.library.scanner.CompositeLibraryScanner;
import org.seasar.maya.impl.builder.library.scanner.MLDLibraryScanner;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.collection.AbstractScanningIterator;
import org.seasar.maya.impl.util.collection.NullIterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class LibraryManagerImpl implements LibraryManager {
    
    private CompositeLibraryScanner _libraryScanner;
    private List _libraries;
    private boolean _scaned;
    
    public LibraryManagerImpl() {
        prepareLibraryScanner();
    }
    
    public void putParameter(String name, String value) {
        throw new UnsupportedOperationException();
    }

//++    
    protected void prepareLibraryScanner() {
        _libraryScanner = new CompositeLibraryScanner();
        _libraryScanner.add(new MLDLibraryScanner());
    }
 	
    public void addLibraryScanner(LibraryScanner scanner) {
        if(scanner == null) {
            throw new IllegalArgumentException();
        }
        _libraryScanner.add(scanner);
    }
    
    public LibraryScanner getLibraryScanner() {
        return _libraryScanner;
    }

    public void addLibraryDefinition(LibraryDefinition library) {
        if(library == null) {
            throw new IllegalArgumentException();
        }
        if(_libraries == null) {
            _libraries = new ArrayList();
        }
        _libraries.add(library);
    }
    
    private void scanLibrary() {
        if(_scaned == false) {
            _scaned = true;
            _libraryScanner.scanLibrary(this);
        }
    }
//--

    public void addSourceScanner(SourceScanner scanner) {
	}

    public void addDefinitionBuilder(DefinitionBuilder builder) {
    }
    
    public Iterator iterateLibraryDefinition() {
        scanLibrary();
        if(_libraries == null) {
            return NullIterator.getInstance();
        }
        return _libraries.iterator();
    }
    
    public Iterator iterateLibraryDefinition(String namespaceURI) {
        if(StringUtil.isEmpty(namespaceURI)) {
            throw new IllegalArgumentException();
        }
		return new LibraryDefinitionFilteredIterator(namespaceURI, iterateLibraryDefinition());
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
            return  _namespaceURI.equals(library.getNamespaceURI()) ||
            	_namespaceURI.equals(library.getAssignedURI());
        }
    }
    
}
