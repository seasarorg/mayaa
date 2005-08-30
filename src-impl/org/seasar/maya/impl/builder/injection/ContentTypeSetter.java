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
import org.seasar.maya.impl.provider.UnsupportedParameterException;
import org.seasar.maya.impl.util.SpecificationUtil;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ContentTypeSetter implements InjectionResolver, CONST_IMPL {

    private void setContentType(SpecificationNode original,
            QName httpEquivName, QName contentName) {
        NodeAttribute equiv = original.getAttribute(httpEquivName);
        if(equiv != null && "Content-Type".equalsIgnoreCase(equiv.getValue())) {
            NodeAttribute content = original.getAttribute(contentName);
            String value = content.getValue();
            if(StringUtil.hasValue(value)) {
        		SpecificationNode maya = SpecificationUtil.getMayaNode(original);
        		if(maya == null) {
        		    throw new IllegalStateException();
        		}
        		maya.addAttribute(QM_CONTENT_TYPE, value);
            }
        }
    }
    
    public SpecificationNode getNode(
            SpecificationNode original, InjectionChain chain) {
        if(original == null || chain == null) {
            throw new IllegalArgumentException();
        }
        QName originalName = original.getQName();
        if(QH_META.equals(originalName)) {
            setContentType(original, QH_HTTP_EQUIV, QH_CONTENT);
        } else if(QX_META.equals(originalName)) {
            setContentType(original, QX_HTTP_EQUIV, QX_CONTENT);
        }
        return chain.getNode(original);
    }
    
    public void setParameter(String name, String value) {
        throw new UnsupportedParameterException(name);
    }

}
