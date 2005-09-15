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

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.seasar.maya.builder.SpecificationBuilder;
import org.seasar.maya.engine.specification.NodeAttribute;
import org.seasar.maya.engine.specification.NodeTreeWalker;
import org.seasar.maya.engine.specification.Specification;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.engine.EngineUtil;
import org.seasar.maya.impl.source.NullSourceDescriptor;
import org.seasar.maya.impl.util.collection.NullIterator;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ProviderFactory;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SpecificationImpl
		implements Specification, CONST_IMPL {
    
    private Date _buildTimestamp;
    private SourceDescriptor _source;
    private Specification _parentSpec;
    private List _childSpecs;
    private List _childNodes;
    
    public SpecificationImpl(Specification parentSpec) {
        _parentSpec = parentSpec;
    }

    public void setParentNode(NodeTreeWalker parentNode) {
        throw new IllegalStateException();
    }

    public NodeTreeWalker getParentNode() {
        return null;
    }

    public void addChildNode(NodeTreeWalker childNode) {
        if(childNode == null) {
            throw new IllegalArgumentException();
        }
        synchronized(this) {
            if(_childNodes == null) {
                _childNodes = new ArrayList();
            }
        }
        synchronized(_childNodes) {
            _childNodes.add(childNode);
            childNode.setParentNode(this);
        }
    }

    public Iterator iterateChildNode() {
        synchronized(this) {
            if(isOldSpecification()) {
                parseSpecification();
            }
        }
        if(_childNodes == null) {
            return NullIterator.getInstance();
        }        
        return _childNodes.iterator();
    }
    
    public String getSystemID() {
        if(_source == null) {
            return null;
        }
        return _source.getSystemID();
    }

    public int getLineNumber() {
        return 0;
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
        return _buildTimestamp;
    }
    
    protected void setTimestamp(Date buildTimestamp) {
    	_buildTimestamp = buildTimestamp;
    }
	
    protected boolean isOldSpecification() {
    	boolean check = EngineUtil.getEngineSettingBoolean(
                CHECK_TIMESTAMP, true);
        if(check == false) {
            return false;
        }
        if(_buildTimestamp == null) {
            return true;
        }
        Date source = getSource().getTimestamp();
        Date now = new Date();
        return source.after(_buildTimestamp) && now.after(source);
    }

    protected void clear() {
        synchronized(this) {
            if(_childNodes != null) {
                _childNodes.clear();
            }
        }
    }

    protected void parseSpecification() {
		setTimestamp(new Date());
        if(getSource().exists()) {
	    	clear();
	        ServiceProvider provider = ProviderFactory.getServiceProvider();
	        SpecificationBuilder builder = provider.getSpecificationBuilder();
	        builder.build(this);
        }
    }
    
    public void kill() {
        setTimestamp(null);
    }
    
    public Specification getParentSpecification() {
        return _parentSpec;
    }

    public void addChildSpecification(Specification child) {
        if(child == null) {
            throw new IllegalArgumentException();
        }
        synchronized(this) {
	        if(_childSpecs == null) {
	            _childSpecs = new ArrayList();
	        }
	        _childSpecs.add(new SoftReference(child));
        }
    }
    
    public Iterator iterateChildSpecification() {
        if(_childSpecs == null) {
            return NullIterator.getInstance();
        }
        return new ChildSpecificationsIterator(_childSpecs);
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
    
	public SpecificationNode copyTo() {
        throw new UnsupportedOperationException();
    }

    protected class ChildSpecificationsIterator implements Iterator {

        private int _index;
        private Specification _next;
        private List _list;
        
        protected ChildSpecificationsIterator(List list) {
            if(list == null) {
                throw new IllegalArgumentException();
            }
            _list = list;
            _index = list.size();
        }
        
        public boolean hasNext() {
            if(_next != null) {
                return true;
            }
            while(_next == null) {
                _index--;
                if(_index < 0) {
                    return false;
                }
                synchronized(_list) {
                    SoftReference ref = (SoftReference)_list.get(_index);
                    _next = (Specification)ref.get();
                    if(_next == null) {
                        _list.remove(_index);
                    }
                }
            }
            return true;
        }

        public Object next() {
            if(_next == null && hasNext() == false) {
                throw new NoSuchElementException();
            }
            Specification ret = _next;
            _next = null;
            return ret;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }       
        
    }
	
}
