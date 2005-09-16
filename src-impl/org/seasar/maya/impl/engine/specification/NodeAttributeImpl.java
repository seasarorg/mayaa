/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License"); you may
 * not use this file except in compliance with the License which accompanies
 * this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.seasar.maya.impl.engine.specification;

import java.util.Iterator;

import org.seasar.maya.engine.specification.Namespace;
import org.seasar.maya.engine.specification.NodeAttribute;
import org.seasar.maya.engine.specification.PrefixMapping;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.util.collection.NullIterator;

/**
 * NodeAttributeÇÃé¿ëïÉNÉâÉXÅB
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class NodeAttributeImpl implements NodeAttribute {
    
    private SpecificationNode _node;
    private QName _qName;
    private String _value;

	public NodeAttributeImpl(QName qName, String value) {
        if(qName == null || value == null) {
            throw new IllegalArgumentException();
        }
        _qName = qName;
        _value = value;
    }
	
	public void setNode(SpecificationNode node) {
	    if(node == null) {
	        throw new IllegalArgumentException();
	    }
	    _node = node;
	}
	
    public SpecificationNode getNode() {
        return _node;
    }

    public String getValue() {
        return _value;
    }

    public String toString() {
        return getQName().toString();
    }

    // QNameable implements ----------------------------------------
    
    public QName getQName() {
        return _qName;
    }
    
    public String getPrefix() {
        String namespaceURI = _qName.getNamespaceURI();
        PrefixMapping mapping = getMappingFromURI(namespaceURI, true);
        if(mapping != null) {
            return mapping.getPrefix();
        }
        return "";
    }
    
    // Namespace implemetns ----------------------------------------
    
    public void setParentSpace(Namespace parent) {
        throw new UnsupportedOperationException();
    }

    public Namespace getParentSpace() {
        if(_node != null) {
            _node.getParentSpace();
        }
        return null;
    }

    public void addPrefixMapping(PrefixMapping mapping) {
        throw new UnsupportedOperationException();
    }

    public PrefixMapping getMappingFromPrefix(String prefix, boolean all) {
        if(_node != null) {
            return _node.getMappingFromPrefix(prefix, all);
        }
        return null;
    }
    
    public PrefixMapping getMappingFromURI(
            String namespaceURI, boolean all) {
        if(_node != null) {
            return _node.getMappingFromURI(namespaceURI, all);
        }
        return null;
    }

    public Iterator iteratePrefixMapping(boolean all) {
        if(_node != null) {
            _node.iteratePrefixMapping(all);
        }
        return NullIterator.getInstance();
    }

    public boolean addedMapping() {
        return false;
    }
    
}
