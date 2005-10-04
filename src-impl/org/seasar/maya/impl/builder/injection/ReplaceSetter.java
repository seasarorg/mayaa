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

import org.seasar.maya.builder.injection.InjectionChain;
import org.seasar.maya.builder.injection.InjectionResolver;
import org.seasar.maya.engine.specification.NodeAttribute;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.builder.BuilderUtil;
import org.seasar.maya.impl.engine.specification.SpecificationUtil;
import org.seasar.maya.impl.provider.UnsupportedParameterException;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ReplaceSetter
        implements InjectionResolver, CONST_IMPL {
	
    protected static final QName QM_REPLACE =
        SpecificationUtil.createQName("replace");

	protected boolean isReplace(SpecificationNode node) {
	    if(node == null) {
	        throw new IllegalArgumentException();
	    }
    	NodeAttribute attr = node.getAttribute(QM_REPLACE);
    	if(attr != null) {
    		return ObjectUtil.booleanValue(attr.getValue(), false);
    	}
    	return false;
	}
    
	public SpecificationNode getNode( 
	        SpecificationNode original, InjectionChain chain) {
		if(original == null || chain == null) {
			throw new IllegalArgumentException();
		}
		SpecificationNode injected = chain.getNode(original);
	    if(injected == null) {
	    	return null;
	    }
        if(isReplace(original) || isReplace(injected)) {
   		    QName qName = original.getQName(); 
   		    String uri = qName.getNamespaceURI();
   		    SpecificationNode element = BuilderUtil.createInjectedNode(
   		            QM_DUPLECATED, uri, original, false);
            StringBuffer name = new StringBuffer();
            String prefix = original.getPrefix();
	        if(StringUtil.hasValue(prefix)) {
	            name.append(prefix).append(":");
	        }
	        name.append(qName.getLocalName());
	        element.addAttribute(QM_NAME, name.toString());
            element.addChildNode(injected);
   			return element;
   		}
        return injected;
    }
    
    // Parameterizable implements ------------------------------------
    
    public void setParameter(String name, String value) {
        throw new UnsupportedParameterException(getClass(), name);
    }
	
}