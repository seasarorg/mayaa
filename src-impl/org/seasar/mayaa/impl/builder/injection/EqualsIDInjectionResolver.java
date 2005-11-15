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
package org.seasar.maya.impl.builder.injection;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.maya.builder.injection.InjectionChain;
import org.seasar.maya.builder.injection.InjectionResolver;
import org.seasar.maya.engine.specification.CopyToFilter;
import org.seasar.maya.engine.specification.NodeAttribute;
import org.seasar.maya.engine.specification.NodeObject;
import org.seasar.maya.engine.specification.Specification;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.ParameterAwareImpl;
import org.seasar.maya.impl.engine.EngineUtil;
import org.seasar.maya.impl.engine.specification.SpecificationUtil;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class EqualsIDInjectionResolver extends ParameterAwareImpl
        implements InjectionResolver, CONST_IMPL {
    
    private static final Log LOG = 
        LogFactory.getLog(EqualsIDInjectionResolver.class);
    
    private CopyToFilter _idFilter = new CheckIDCopyToFilter();
    private boolean _reportResolvedID = true;

    protected CopyToFilter getCopyToFilter() {
        return _idFilter;
    }
    
    protected boolean isReportResolvedID() {
        return _reportResolvedID;
    }
    
    protected String getID(SpecificationNode node) {
	    if(node == null) {
	        throw new IllegalArgumentException();
	    }
    	NodeAttribute attr = node.getAttribute(QM_ID);
    	if(attr != null) {
    		return attr.getValue();
    	}
		attr = node.getAttribute(QX_ID);
		if(attr != null) {
			return attr.getValue();
		}
		attr = node.getAttribute(QH_ID);
		if(attr != null) {
			return attr.getValue();
		}
		return null;
    }

    protected SpecificationNode getEqualsIDNode(
            SpecificationNode node, String id) {
        if(node == null || StringUtil.isEmpty(id)) {
            throw new IllegalArgumentException();
        }
        for(Iterator it = node.iterateChildNode(); it.hasNext(); ) {
            SpecificationNode child = (SpecificationNode)it.next();
            if(id.equals(SpecificationUtil.getAttributeValue(child, QM_ID))) {
                return child;
            }
            SpecificationNode ret = getEqualsIDNode(child, id);
            if(ret != null) {
                return ret;
            }
        }
        return null;
    }
    
    public SpecificationNode getNode(
            SpecificationNode original, InjectionChain chain) {
        if(original == null || chain == null) {
            throw new IllegalArgumentException();
        }
        String id = getID(original);
        if(StringUtil.hasValue(id)) {
            Specification spec = SpecificationUtil.findSpecification(original);
            SpecificationNode injected = null;
            while(spec != null) {
                SpecificationNode maya = SpecificationUtil.getMayaNode(spec);
                if(maya != null) {
                    injected = getEqualsIDNode(maya, id);
                    if(injected != null) {
                        break;
                    }
                }
                spec = EngineUtil.getParentSpecification(spec);
            }
	        if(injected != null) {
	            if(QM_IGNORE.equals(injected.getQName())) {
	                return chain.getNode(original);
	            }
                return injected.copyTo(getCopyToFilter());
	        }
            if(isReportResolvedID()) {
                if(LOG.isWarnEnabled()) {
                    String systemID = original.getSystemID();
                    String lineNumber = Integer.toString(original.getLineNumber());
                    String msg = StringUtil.getMessage(
                            EqualsIDInjectionResolver.class, 0, 
                            id, systemID, lineNumber);
                    LOG.warn(msg);
                }
            }
        }
        return chain.getNode(original);
    }

    // Parameterizable implements ------------------------------------
    
    public void setParameter(String name, String value) {
        if("reportUnresolvedID".equals(name)) {
            _reportResolvedID = ObjectUtil.booleanValue(value, true);
        }
        super.setParameter(name, value);
    }
    
    // support class ------------------------------------------------
    
    protected class CheckIDCopyToFilter implements CopyToFilter {
   
        public boolean accept(NodeObject test) {
            if(test instanceof NodeAttribute) {
                NodeAttribute attr = (NodeAttribute)test;
                return attr.getQName().equals(QM_ID) == false;
            }
            return true;
        }
        
    }
    
}
