/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.maya.builder.specification.InjectionChain;
import org.seasar.maya.builder.specification.InjectionResolver;
import org.seasar.maya.engine.Template;
import org.seasar.maya.engine.specification.NodeAttribute;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.util.SpecificationUtil;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ContentTypeSetter implements InjectionResolver, CONST_IMPL {

	private static final Log LOG = LogFactory.getLog(ContentTypeSetter.class);
	
    private void setContentType(Template template, SpecificationNode specificationNode,
            QName httpEquivName, QName contentName) {
        NodeAttribute equiv = specificationNode.getAttribute(httpEquivName);
        if(equiv != null && "Content-Type".equalsIgnoreCase(equiv.getValue())) {
            NodeAttribute content = specificationNode.getAttribute(contentName);
            String value = content.getValue();
            if(StringUtil.hasValue(value)) {
        		SpecificationNode maya = SpecificationUtil.getMayaNode(template);
        		if(maya == null) {
        		    throw new IllegalStateException();
        		}
        		maya.addAttribute(QM_CONTENT_TYPE, value);
                if(LOG.isTraceEnabled()) {
                	LOG.trace("contentType setting " + value);
                }
            }
        }
    }
    
    public SpecificationNode getNode(Template template, 
            SpecificationNode original, InjectionChain chain) {
        if(template == null) {
            throw new IllegalArgumentException();
        }
        QName originalName = original.getQName();
        if(QH_META.equals(originalName)) {
            setContentType(template, original, QH_HTTP_EQUIV, QH_CONTENT);
        } else if(QX_META.equals(originalName)) {
            setContentType(template, original, QX_HTTP_EQUIV, QX_CONTENT);
        }
        return chain.getNode(template, original);
    }
    
    public void putParameter(String name, String value) {
        throw new UnsupportedOperationException();
    }

}
