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
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.collection.NullIterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class LibraryDefinitionImpl implements LibraryDefinition {

    private List _namespaceURI = new ArrayList();
    private Map _processors;
    
    public void addNamespaceURI(String namespaceURI) {
        if(StringUtil.isEmpty(namespaceURI)) {
            throw new IllegalArgumentException();
        }
        if(_namespaceURI.contains(namespaceURI) == false) {
            _namespaceURI.add(namespaceURI);
        }
    }
    
    public Iterator iterateNamespaceURI() {
        return _namespaceURI.iterator();
    }

    public void addProcessorDefinition(ProcessorDefinition processor) {
        if(processor == null) {
            throw new IllegalArgumentException();
        }
        if(_processors == null) {
            _processors = new HashMap();
        }
        _processors.put(processor.getName(), processor);
    }
    
    public Iterator iterateProcessorDefinition() {
        if(_processors == null) {
            return NullIterator.getInstance();
        }
        return _processors.values().iterator();
    }
    
    public ProcessorDefinition getProcessorDefinition(String localName) {
        if(StringUtil.isEmpty(localName)) {
            throw new IllegalArgumentException();
        }
        if(_processors == null) {
            return null;
        }
        return (ProcessorDefinition)_processors.get(localName);
    }

}
