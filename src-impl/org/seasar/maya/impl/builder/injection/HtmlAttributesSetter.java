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

import java.util.Iterator;

import org.seasar.maya.builder.injection.InjectionChain;
import org.seasar.maya.builder.injection.InjectionResolver;
import org.seasar.maya.engine.specification.NodeAttribute;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.provider.UnsupportedParameterException;
import org.seasar.maya.impl.util.SpecificationUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class HtmlAttributesSetter implements InjectionResolver, CONST_IMPL {

	public SpecificationNode getNode( 
	        SpecificationNode original, InjectionChain chain) {
		if(original == null || chain == null) {
			throw new IllegalArgumentException();
		}
		QName originalName = original.getQName();
		SpecificationNode maya = SpecificationUtil.getMayaNode(original);
		if(maya == null) {
		    throw new IllegalStateException();
		}
		if(QH_HTML.equals(originalName) || QX_HTML.equals(originalName)) {
   		    for(Iterator it = original.iterateAttribute(); it.hasNext(); ) {
   		        NodeAttribute attribute = (NodeAttribute)it.next();
       		    maya.addAttribute(attribute.getQName(), attribute.getValue());
   		    }
   		}
        return chain.getNode(original);
    }

    public void setParameter(String name, String value) {
        throw new UnsupportedParameterException(name);
    }
	
}