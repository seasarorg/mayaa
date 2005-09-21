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
package org.seasar.maya.impl.engine.specification;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.maya.engine.specification.Namespace;
import org.seasar.maya.engine.specification.PrefixMapping;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.collection.NullIterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class NamespaceImpl implements Namespace {

    private static Log LOG = LogFactory.getLog(NamespaceImpl.class);
    
    private Namespace _parentSpace;
    private List _mappings;
    
    public void setParentSpace(Namespace parent) {
        if(parent == null) {
            throw new IllegalArgumentException();
        }
        _parentSpace = parent;
    }
    
    public Namespace getParentSpace() {
        return _parentSpace;
    }

    public void addPrefixMapping(PrefixMapping mapping) {
        if(mapping == null) {
            throw new IllegalArgumentException();
        }
	    synchronized(this) {
	        if(_mappings == null) {
	            _mappings = new ArrayList();
	        }
	        if(_mappings.contains(mapping) == false) {
	            _mappings.add(mapping);
	            mapping.setNamespace(this);
	        } else {
                if(LOG.isWarnEnabled()) {
                    LOG.warn(StringUtil.getMessage(NamespaceImpl.class, 0, 
                            new String[] { mapping.toString() }));
                }
            }
	    }
    }
    
    private PrefixMapping getMapping(
            boolean fromPrefix, String test, boolean all) {
        if(test == null) {
            throw new IllegalArgumentException();
        }
        for(Iterator it = iteratePrefixMapping(all); it.hasNext(); ) {
            PrefixMapping mapping = (PrefixMapping)it.next();
            String value = fromPrefix ? 
                    mapping.getPrefix() : mapping.getNamespaceURI();
            if(test.equals(value)) {
                return mapping;
            }
        }
        return null;
    }
    
    public PrefixMapping getMappingFromPrefix(String prefix, boolean all) {
        if(prefix == null) {
            prefix = "";
        }
        return getMapping(true, prefix, all);
    }

    public PrefixMapping getMappingFromURI(
            String namespaceURI, boolean all) {
        if(StringUtil.isEmpty(namespaceURI)) {
            throw new IllegalArgumentException();
        }
        return getMapping(false, namespaceURI, all);
    }

    public Iterator iteratePrefixMapping(boolean all) {
        if(all && _parentSpace != null) {
            return new AllNamespaceIterator(this);
        }
        if(_mappings != null) {
            return _mappings.iterator();
        }
        return NullIterator.getInstance();
    }

    public boolean addedMapping() {
        if(_mappings == null) {
            return false;
        }
        synchronized(_mappings) {
            return _mappings.size() > 0;
        }
    }
    
    // support class -------------------------------------------------
    
    private class AllNamespaceIterator implements Iterator {
        
        private Namespace _current;
        private Iterator _it;
        
        private AllNamespaceIterator(Namespace current) {
            if(current == null) {
                throw new IllegalArgumentException();
            }
            _current = current;
            _it = current.iteratePrefixMapping(false);
        }

        public boolean hasNext() {
            while(_it != null) {
                boolean ret = _it.hasNext();
                if(ret) {
                    return true;
                }
                _current = _current.getParentSpace();
                if(_current != null) {
                    _it = _current.iteratePrefixMapping(false);
                } else {
                    _it = null;
                }
            }
            return false;
        }

        public Object next() {
            if(hasNext()) {
                return _it.next();
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }
    
}
