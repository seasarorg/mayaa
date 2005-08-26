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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.seasar.maya.engine.specification.CopyToFilter;
import org.seasar.maya.engine.specification.NodeAttribute;
import org.seasar.maya.engine.specification.NodeNamespace;
import org.seasar.maya.engine.specification.NodeObject;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.Specification;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.collection.NullIterator;
import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

/**
 * SpecificationNodeの実装クラス。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SpecificationNodeImpl extends QNameableImpl
		implements SpecificationNode, CONST_IMPL {
    
    private Map _attributes;
    private SpecificationNode _parent;
    private List _childNodes;
	private Locator _locator;

	public SpecificationNodeImpl(QName qName, Locator locator) {
	    super(qName);
	    if(locator != null) {
	        _locator = new LocatorImpl(locator);
	    } else {
            _locator = LOCATOR_NULL;
	    }
    }
    
	public void addAttribute(QName qName, String value) {
	    if(qName == null || value == null) {
	        throw new IllegalArgumentException();
	    }
	    synchronized(this) {
	        if(_attributes == null) {
	            _attributes = new HashMap();
	        }
	    }
	    synchronized(_attributes) {
	        if(_attributes.containsKey(qName) == false) {
	        	NodeAttributeImpl attr = new NodeAttributeImpl(qName, value);
		        _attributes.put(qName, attr);
		        attr.setNode(this);
	        }
	    }
	}
	
	public NodeAttribute getAttribute(QName qName) {
	    if(qName == null) {
	        throw new IllegalArgumentException();
	    }
	    if(_attributes == null) {
	        return null;
	    }
	    return (NodeAttribute)_attributes.get(qName);
	}
	
    public Iterator iterateAttribute() {
        if(_attributes == null) {
            return NullIterator.getInstance();
        }
        return _attributes.values().iterator();
    }
    
    public void setParentNode(SpecificationNode parent) {
        if(parent == null) {
            throw new IllegalArgumentException();
        }
        _parent = parent;
    }
    
    public SpecificationNode getParentNode() {
        return _parent;
    }

    public void addChildNode(SpecificationNode child) {
        if(child == null) {
            throw new IllegalArgumentException();
        }
        synchronized(this) {
	        if(_childNodes == null) {
	        	_childNodes = new ArrayList();
	        }
        }
        synchronized(_childNodes) {
            _childNodes.add(child);
            child.setParentNode(this);
        }
    }
    
    public Iterator iterateChildNode() {
        if(_childNodes == null) {
            return NullIterator.getInstance();
        }        
        return _childNodes.iterator();
    }
    
    /**
	 * 属性と子ノードのクリア。
	 */
	protected void clear() {
	    synchronized(this) {
		    super.clear();
		    if(_attributes != null) {
		        _attributes.clear();
		    }
			if(_childNodes != null) {
	            _childNodes.clear();
			}
	    }
	}

    public String toString() {
	    StringBuffer path = new StringBuffer();
	    if(_parent != null && _parent instanceof Specification == false) {
	        path.append(_parent);
	    }
	    path.append("/");
	    String prefix = getPrefix();
	    if(StringUtil.hasValue(prefix)) {
	        path.append(prefix).append(":");
	    }
	    path.append(getQName().getLocalName());
        path.append(" (line: ").append(_locator.getLineNumber()).append(")");
        return path.toString();
	}
	
    public Locator getLocator() {
        return _locator;
    }

    public SpecificationNode copyTo(CopyToFilter filter) {
        SpecificationNodeImpl copy = new SpecificationNodeImpl(getQName(), getLocator());
        for(Iterator it = iterateAttribute(); it.hasNext(); ) {
            NodeAttribute attr = (NodeAttribute)it.next();
            if(filter.accept(attr)) {
                copy.addAttribute(attr.getQName(), attr.getValue());
            }
        }
        for(Iterator it = iterateChildNode(); it.hasNext(); ) {
            SpecificationNode node = (SpecificationNode)it.next();
            if(filter.accept(node)) {
                copy.addChildNode(node.copyTo(filter));
            }
        }
        for(Iterator it = iterateNamespace(); it.hasNext(); ) {
            NodeNamespace ns = (NodeNamespace)it.next();
            if(filter.accept(ns)) {
                copy.addNamespace(ns.getPrefix(), ns.getNamespaceURI());
            }
        }
        return copy;
    }
    
    public SpecificationNode copyTo() {
        return copyTo(FILTER_ALL);
    }

    private static final CopyToFilter FILTER_ALL = new AllCopyToFilter();
    
    private static class AllCopyToFilter implements CopyToFilter {
        
        public boolean accept(NodeObject test) {
            return true;
        }
        
    }
    
    private static final Locator LOCATOR_NULL = new NullLocator();
    
    private static class NullLocator implements Locator {
        
        private NullLocator() {
        }
        
        public int getColumnNumber() {
            return 0;
        }

        public int getLineNumber() {
            return 0;
        }
        
        public String getPublicId() {
            return "";
        }
        
        public String getSystemId() {
            return "";
        }

    }

}
