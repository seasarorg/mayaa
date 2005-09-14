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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.seasar.maya.engine.specification.Namespaceable;
import org.seasar.maya.engine.specification.NodeNamespace;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.collection.NullIterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class NamespaceableImpl implements Namespaceable {

    private Namespaceable _parent;
    private Map _namespaces;
    
	protected void clear() {
	    synchronized(this) {
			if(_namespaces != null) {
			    _namespaces.clear();
			}
            _parent = null;
	    }
	}

    public boolean added() {
        if(_namespaces == null) {
            return false;
        }
        synchronized(_namespaces) {
            return _namespaces.size() > 0;
        }
    }
    
    public void setParentSpace(Namespaceable parent) {
        if(parent == null) {
            throw new IllegalArgumentException();
        }
        _parent = parent;
    }
    
    public Namespaceable getParentSpace() {
        return _parent;
    }

    public void addNamespace(String prefix, String namespaceURI) {
        if(StringUtil.isEmpty(namespaceURI)) {
            throw new IllegalArgumentException();
        }
        if(prefix == null) {
            prefix = "";
        }
	    synchronized(this) {
	        if(_namespaces == null) {
	            _namespaces = new HashMap();
	        }
	        if(_namespaces.containsKey(prefix) == false) {
	        	NodeNamespaceImpl ns = 
                    new NodeNamespaceImpl(prefix, namespaceURI); 
	            _namespaces.put(prefix, ns);
	            ns.setNamespaceable(this);
	        }
	    }
    }
    
    public NodeNamespace getNamespace(String prefix, boolean all) {
        if(prefix == null) {
            prefix = "";
        }
        NodeNamespace ret = null;
        if(_namespaces != null) {
            ret = (NodeNamespace)_namespaces.get(prefix);
            if(ret != null) {
                return ret;
            }
        }
        if(all && _parent != null) {
            return _parent.getNamespace(prefix, true);
        }
        return null;
    }

    public Iterator iterateNamespace(boolean all) {
        if(all && _parent != null) {
            return new AllNamespaceIterator(this);
        }
        if(_namespaces != null) {
            return _namespaces.values().iterator();
        }
        return NullIterator.getInstance();
    }
    
    private class AllNamespaceIterator implements Iterator {
        
        private Namespaceable _current;
        private Iterator _it;
        
        private AllNamespaceIterator(Namespaceable current) {
            if(current == null) {
                throw new IllegalArgumentException();
            }
            _current = current;
            _it = current.iterateNamespace(false);
        }

        public boolean hasNext() {
            while(_it != null) {
                boolean ret = _it.hasNext();
                if(ret) {
                    return true;
                }
                _current = _current.getParentSpace();
                if(_current != null) {
                    _it = _current.iterateNamespace(false);
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
