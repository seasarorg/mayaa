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

import org.seasar.maya.engine.specification.Namespaceable;
import org.seasar.maya.engine.specification.NodeNamespace;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.collection.AbstractScanningIterator;
import org.seasar.maya.impl.util.collection.NullIterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class NamespaceableImpl implements Namespaceable {

    private Map _namespaces;

	protected void clear() {
	    synchronized(this) {
			if(_namespaces != null) {
			    _namespaces.clear();
			}
	    }
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
	        	NodeNamespaceImpl ns = new NodeNamespaceImpl(prefix, namespaceURI); 
	            _namespaces.put(prefix, ns);
	            ns.setNamespaceable(this);
	        }
	    }
    }
    
    public void removeNamespace(String prefix) {
        if(prefix == null) {
            throw new IllegalArgumentException();
        }
	    synchronized(this) {
	        if(_namespaces == null) {
	            return;
	        }
            _namespaces.remove(prefix);
	    }
    }
    
    public NodeNamespace getNamespace(String prefix) {
        if(_namespaces == null) {
            return null;
        }
        if(prefix == null) {
            prefix = "";
        }
        return (NodeNamespace)_namespaces.get(prefix);
    }

    public Iterator iterateNamespace() {
		if(_namespaces == null) {
		    return NullIterator.getInstance();
		}
		return _namespaces.values().iterator();
    }
    
    public Iterator iterateNamespace(String namespaceURI) {
        if(StringUtil.isEmpty(namespaceURI)) {
            throw new IllegalArgumentException();
        }
		return new NodeNamespaceFilteredIterator(namespaceURI, iterateNamespace());
    }
    
    private class NodeNamespaceFilteredIterator extends AbstractScanningIterator {
        
        private String _namespaceURI;
        
        private NodeNamespaceFilteredIterator(String namespaceURI, Iterator iterator) {
            super(iterator);
            if(StringUtil.isEmpty(namespaceURI)) {
                throw new IllegalArgumentException();
            }
            _namespaceURI = namespaceURI;
        }
        
        protected boolean filter(Object test) {
            if(test == null || (test instanceof NodeNamespace == false)) {
                return false;
            }
            NodeNamespace namespace = (NodeNamespace)test;
            return _namespaceURI.equals(namespace.getNamespaceURI());
        }

    }
    
}
