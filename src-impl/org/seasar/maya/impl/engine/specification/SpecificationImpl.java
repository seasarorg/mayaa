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
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.seasar.maya.builder.SpecificationBuilder;
import org.seasar.maya.engine.specification.NodeAttribute;
import org.seasar.maya.engine.specification.NodeNamespace;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.Specification;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.source.NullSourceDescriptor;
import org.seasar.maya.impl.util.SpecificationUtil;
import org.seasar.maya.impl.util.collection.NullIterator;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ProviderFactory;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class SpecificationImpl 
		extends SpecificationNodeImpl implements Specification, CONST_IMPL {
    
    private static final NodeNamespace NS_MAYA = 
        new NodeNamespaceImpl(PREFIX_MAYA, URI_MAYA);
    
    private Date _buildTimestamp;
    private SourceDescriptor _source;
    private Specification _parent;
    private List _children;
    
    public SpecificationImpl(QName qName, Specification parent) {
        super(qName, "", 0);
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
        return _buildTimestamp;
    }
    
    protected void setTimestamp(Date buildTimestamp) {
    	_buildTimestamp = buildTimestamp;
    }
	
    protected boolean isOldSpecification() {
    	boolean check = SpecificationUtil.getEngineSettingBoolean(
                CHECK_TIMESTAMP, true);
        if(check == false) {
            return false;
        }
        Date buildTimestamp = getTimestamp();
        if(buildTimestamp == null) {
            return true;
        }
        Date source = getSource().getTimestamp();
        Date now = new Date();
        return source.after(buildTimestamp) && now.after(source);
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
    
    public String getSystemID() {
    	if(_source != null) {
    		return _source.getSystemID();
    	}
		return null;
	}

	public SpecificationNode copyTo() {
        throw new UnsupportedOperationException();
    }

}
