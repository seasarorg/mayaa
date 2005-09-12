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
package org.seasar.maya.impl.builder.injection;

import org.seasar.maya.builder.injection.InjectionChain;
import org.seasar.maya.builder.injection.InjectionResolver;
import org.seasar.maya.engine.specification.NodeAttribute;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.engine.specification.SpecificationImpl;
import org.seasar.maya.impl.provider.UnsupportedParameterException;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class RenderedSetter	implements InjectionResolver, CONST_IMPL {
	
	private boolean isRendered(SpecificationNode node, boolean def) {
	    if(node == null) {
	        throw new IllegalArgumentException();
	    }
    	NodeAttribute attr = node.getAttribute(QM_RENDERED);
    	if(attr != null) {
    		return ObjectUtil.booleanValue(attr.getValue(), false);
    	}
    	return def;
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
        if(QM_TEMPLATE_ELEMENT.equals(injected.getQName())) {
            if(isRendered(original, true) == false) {
                return SpecificationImpl.createInjectedNode(
                        QM_NULL, null, original);
            }
        } else if(isRendered(original, false) || isRendered(injected, false)) {
   		    QName qName = original.getQName(); 
   		    String uri = qName.getNamespaceURI();
   		    SpecificationNode element = SpecificationImpl.createInjectedNode(
   		            QM_DUPLECATED_ELEMENT, uri, original);
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

    public void setParameter(String name, String value) {
        throw new UnsupportedParameterException(getClass(), name);
    }
	
}