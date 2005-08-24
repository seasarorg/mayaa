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
package org.seasar.maya.impl.builder.specification;

import org.seasar.maya.builder.specification.InjectionChain;
import org.seasar.maya.builder.specification.InjectionResolver;
import org.seasar.maya.engine.Template;
import org.seasar.maya.engine.specification.NodeAttribute;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.provider.UnsupportedParameterException;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.SpecificationUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ElementDuplicator	implements InjectionResolver, CONST_IMPL {
	
	private boolean isRendered(SpecificationNode node) {
	    if(node == null) {
	        throw new IllegalArgumentException();
	    }
    	NodeAttribute attr = node.getAttribute(QM_RENDERED);
    	if(attr != null) {
    		return ObjectUtil.booleanValue(attr.getValue(), false);
    	}
    	return false;
	}
	
	private boolean isRendered(
	        SpecificationNode original, SpecificationNode injected) {
	    return isRendered(original) || isRendered(injected);
	}
	
	public SpecificationNode getNode(Template template, 
	        SpecificationNode original, InjectionChain chain) {
		if(template == null) {
			throw new IllegalArgumentException();
		}
		SpecificationNode injected = chain.getNode(template, original);
	    if(injected == null) {
	    	return null;
	    }
   		if(isRendered(original, injected)) {
   		    QName qName = original.getQName(); 
   		    String uri = qName.getNamespaceURI();
   		    SpecificationNode element = SpecificationUtil.createInjectedNode(
   		            QM_DUPLECATED_ELEMENT, uri, original);
   	        element.addAttribute(QM_NAMESPACE_URI, uri);
   	        element.addAttribute(QM_LOCAL_NAME, qName.getLocalName());
   			element.addChildNode(injected);
   			return element;
   		}
        return injected;
    }

    public void setParameter(String name, String value) {
        throw new UnsupportedParameterException(name);
    }
	
}