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
package org.seasar.maya.impl.engine.specification;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.seasar.maya.builder.SpecificationBuilder;
import org.seasar.maya.engine.specification.NodeTreeWalker;
import org.seasar.maya.engine.specification.Specification;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.ParameterAwareImpl;
import org.seasar.maya.impl.engine.EngineUtil;
import org.seasar.maya.impl.provider.ProviderUtil;
import org.seasar.maya.impl.source.NullSourceDescriptor;
import org.seasar.maya.impl.util.collection.NullIterator;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SpecificationImpl extends ParameterAwareImpl
		implements Specification, CONST_IMPL {
    
	private static final long serialVersionUID = 174451168836001746L;

	private Date _buildTimestamp;
    private SourceDescriptor _source;
    private List _childNodes;
    private boolean _hasSource;

    protected void clear() {
        synchronized(this) {
            if(_childNodes != null) {
                _childNodes.clear();
            }
        }
    }

    protected boolean checkTimestamp() {
        return EngineUtil.getEngineSettingBoolean(CHECK_TIMESTAMP, true);
    }

    protected boolean isSourceNotExists() {
        if (checkTimestamp() == false) {
            return false;
        }
        return getSource().exists() == false;
    }

    protected boolean isOldSpecification() {
        if (checkTimestamp() == false) {
            return false;
        }

        if (_hasSource != getSource().exists()) {
            clear();
            kill();
            _hasSource = getSource().exists();
            return true;
        }

        if (getTimestamp() == null) {
            return true;
        }
        Date source = getSource().getTimestamp();
        Date now = new Date();
        return source.after(getTimestamp()) && now.after(source);
    }

    protected void parseSpecification() {
        setTimestamp(new Date());
        if(getSource().exists()) {
            clear();
            SpecificationBuilder builder = ProviderUtil.getSpecificationBuilder();
            builder.build(this);
        }
    }
    
    protected void setTimestamp(Date buildTimestamp) {
        _buildTimestamp = buildTimestamp;
    }
    
    public Date getTimestamp() {
        return _buildTimestamp;
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
    
    public void kill() {
        setTimestamp(null);
    }
    
    // NodeTreeWalker implements ------------------------------------
    
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
        if(getSource() == null) {
            return null;
        }
        return getSource().getSystemID();
    }

    public int getLineNumber() {
        return 0;
    }

    // support class -------------------------------------------------
    
    protected class ChildSpecificationsIterator implements Iterator {

        private int _index;
        private Specification _next;
        private List _list;
        
        public ChildSpecificationsIterator(List list) {
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
