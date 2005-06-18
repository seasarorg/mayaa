/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.seasar.maya.builder.SpecificationBuilder;
import org.seasar.maya.engine.specification.NodeAttribute;
import org.seasar.maya.engine.specification.NodeNamespace;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.Specification;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.source.NullSourceDescriptor;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.collection.NullIterator;
import org.seasar.maya.impl.util.xml.NullLocator;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ServiceProviderFactory;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class SpecificationImpl 
		extends SpecificationNodeImpl implements Specification, CONST_IMPL {
    
    private static final Date DATE_ZERO = new Date(0);
    private static final NodeNamespace NS_MAYA = 
        new NodeNamespaceImpl(PREFIX_MAYA, URI_MAYA);
    
    private Date _buildTimestamp;
    private SourceDescriptor _source;
    private Specification _parent;
    private List _children;
    
    public SpecificationImpl(QName qName, Specification parent) {
        super(qName, NullLocator.getInstance());
        _parent = parent;
    }

    public void setSource(SourceDescriptor source) {
        _source = source;
    }
    
    public SourceDescriptor getSource() {
		if(_source == null) {
		    _source = new NullSourceDescriptor();
		}
		return _source;
    }
    
    public Date getTimestamp() {
        if(_buildTimestamp == null) {
            return DATE_ZERO;
        }
        return _buildTimestamp;
    }
    
    /**
     * ビルド時の設定を行う。
     * @param buildTimestamp ビルド時もしくはnull。
     */
    protected void setTimestamp(Date buildTimestamp) {
    	_buildTimestamp = buildTimestamp;
    }
	
    /**
     * 設定XMLの保存日付をチェックして、再ビルドの必要の有無を調べる。
     * @return ファイルが前回ビルド時より更新されていたら、trueを返す。
     */
    protected boolean isOldSpecification() {
    	Date buildTimestamp = getTimestamp();
        if(buildTimestamp == null) {
            return true;
        }
        Date source = getSource().getTimestamp();
        return source.after(buildTimestamp);
    }

    /**
     * 設定XMLをパースする。
     */
    protected void parseSpecification() {
		setTimestamp(new Date());
        if(getSource().exists()) {
	    	clear();
	        ServiceProvider provider = ServiceProviderFactory.getServiceProvider();
	        SpecificationBuilder builder = provider.getSpecificationBuilder();
	        builder.build(this);
        }
    }
    
    public NodeNamespace getDefaultNamespace() {
        return NS_MAYA;
    }
    
    public void kill() {
        setTimestamp(null);
    }
    
    public Specification getParentSpecification() {
        return _parent;
    }

    public void addChildSpecification(Specification child) {
        if(child == null) {
            throw new IllegalArgumentException();
        }
        synchronized(this) {
	        if(_children == null) {
	            _children = new ArrayList();
	        }
	        _children.add(child);
        }
    }
    
    public Iterator iterateChildSpecification() {
        if(_children == null) {
            return NullIterator.getInstance();
        }
        return _children.iterator();
    }

    public void setParentNode(SpecificationNode parent) {
       	throw new UnsupportedOperationException();
    }

    public void addAttribute(NodeAttribute nodeAttribute) {
        throw new IllegalStateException();
    }

    public Iterator iterateAttribute() {
        return NullIterator.getInstance();
    }

    public Iterator iterateChildNode() {
        synchronized(this) {
	        if(isOldSpecification()) {
	            parseSpecification();
	        }
        }
        return super.iterateChildNode();
    }
    
    public String toString() {
        return getKey();
    }

    public boolean booleanValueOf(String xpathExpr, Map namespaces) {
        XPath xpath = SpecificationXPath.createXPath(xpathExpr, namespaces);
        try {
            return xpath.booleanValueOf(this);
        } catch(JaxenException e) {
            throw new RuntimeException(e);
        }
    }

    public Number numberValueOf(String xpathExpr, Map namespaces) {
        XPath xpath = SpecificationXPath.createXPath(xpathExpr, namespaces);
        try {
            return xpath.numberValueOf(this);
        } catch(JaxenException e) {
            throw new RuntimeException(e);
        }
    }
    
    public String stringValueOf(String xpathExpr, Map namespaces) {
        XPath xpath = SpecificationXPath.createXPath(xpathExpr, namespaces);
        try {
            return xpath.stringValueOf(this);
        } catch(JaxenException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Iterator selectChildNodes(String xpathExpr, Map namespaces, boolean cascade) {
    	if(StringUtil.isEmpty(xpathExpr)) {
    		throw new IllegalArgumentException();
    	}
        if(cascade) {
        	return new CascadeSelectNodesIterator(this, xpathExpr, namespaces);
        }
        try {
            XPath xpath = SpecificationXPath.createXPath(xpathExpr, namespaces);
            return xpath.selectNodes(this).iterator();
        } catch(JaxenException e) {
            throw new RuntimeException(e);
        }
    }
    
    public SpecificationNode copyTo() {
        throw new UnsupportedOperationException();
    }

    private class CascadeSelectNodesIterator implements Iterator {

    	private Specification _specification;
    	private String _xpathExpr;
        private Map _namespaces;
        private Iterator _iterator;
    	
    	private CascadeSelectNodesIterator(Specification specification, 
    	        String xpathExpr, Map namespaces) {
    		if(specification == null || StringUtil.isEmpty(xpathExpr)) {
    			throw new IllegalArgumentException();
    		}
    		_specification = specification;
    		_xpathExpr = xpathExpr;
    		_namespaces = namespaces;
    	}
    	
        public boolean hasNext() {
        	while(true) {
    	        if(_iterator == null) {
    	        	XPath xpath = SpecificationXPath.createXPath(_xpathExpr, _namespaces);
    	            try {
    					_iterator = xpath.selectNodes(_specification).iterator();
    				} catch (JaxenException e) {
    					throw new RuntimeException(e);
    				}
    	        }
    	        if(_iterator.hasNext()) {
    	        	return true;
    	        }
                Specification parent = _specification.getParentSpecification();
                if(parent == null) {
                    return false;
                }
                _specification = parent;
                _iterator = null;
        	}
        }
    	
    	public Object next() {
    		if(hasNext()) {
    			return _iterator.next();
    		}
    		throw new NoSuchElementException();
    	}
    	
    	public void remove() {
    		throw new UnsupportedOperationException();
    	}
    	
    }

}
