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
import org.seasar.maya.impl.ParameterAwareImpl;
import org.seasar.maya.impl.builder.BuilderUtil;
import org.seasar.maya.impl.engine.specification.SpecificationUtil;
import org.seasar.maya.impl.util.ObjectUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class RenderedSetter extends ParameterAwareImpl	
        implements InjectionResolver, CONST_IMPL {
	
    protected static final QName QM_NULL = 
        SpecificationUtil.createQName("null");
    protected static final QName QM_RENDERED = 
        SpecificationUtil.createQName("rendered");

	protected boolean isRendered(SpecificationNode node) {
	    if(node == null) {
	        throw new IllegalArgumentException();
	    }
        NodeAttribute attr = node.getAttribute(QM_RENDERED);
        if(attr != null) {
            return ObjectUtil.booleanValue(attr.getValue(), true);
        }
    	return true;
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
        if(isRendered(original) == false || isRendered(injected) == false) {
                return BuilderUtil.createInjectedNode(
                        QM_NULL, null, original, false);
        } 
        return injected;
    }
	
}