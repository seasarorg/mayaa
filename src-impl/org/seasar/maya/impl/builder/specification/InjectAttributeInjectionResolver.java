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
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.QNameable;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.util.SpecificationUtil;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class InjectAttributeInjectionResolver implements InjectionResolver, CONST_IMPL {
	
    public SpecificationNode getNode(
            Template template, SpecificationNode original, InjectionChain chain) {
        if(template == null || original == null || chain == null) {
            throw new IllegalArgumentException();
        }
    	String injectName = SpecificationUtil.getAttributeValue(original, QM_INJECT);
        if(StringUtil.hasValue(injectName)) {
            QNameable qNameable = SpecificationUtil.parseName(
                    original, template, original.getLocator(), injectName, URI_MAYA);
            QName qName = qNameable.getQName();
            if(QM_IGNORE.equals(qName) == false) {
	            String uri = qName.getNamespaceURI();
	            return SpecificationUtil.createInjectedNode(qName, uri, original); 
            }
        }
        return chain.getNode(template, original);
    }
    
    public void putParameter(String name, String value) {
        throw new UnsupportedOperationException();
    }

}
