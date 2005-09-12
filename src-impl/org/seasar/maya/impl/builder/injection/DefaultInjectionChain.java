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
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.engine.specification.SpecificationImpl;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class DefaultInjectionChain implements InjectionChain, CONST_IMPL {
    
    private static final DefaultInjectionChain _instance = 
        new DefaultInjectionChain();
    
    public static InjectionChain getInstance() {
        return _instance;
    }

    private QName[] _specialNames = new QName[] {
        QM_CDATA,
        QM_CHARACTERS,
        QM_COMMENT,
        QM_DOCTYPE,
        QM_PROCESSING_INSTRUCTION
    };
    
    private DefaultInjectionChain() {
    }

    private boolean isSpecialNode(QName qName) {
        for(int i = 0; i < _specialNames.length; i++) {
            if(_specialNames[i].equals(qName)) {
                return true;
            }
        }
        return false;
    }
    
    public SpecificationNode getNode(SpecificationNode original) {
        if(original == null) {
            throw new IllegalArgumentException();
        }
        if(isSpecialNode(original.getQName())) {
            return original.copyTo();
        }
        QName qName = original.getQName(); 
        String uri = qName.getNamespaceURI();
        SpecificationNode element =  SpecificationImpl.createInjectedNode(
                QM_TEMPLATE_ELEMENT, uri, original);
        StringBuffer name = new StringBuffer();
            String prefix = original.getPrefix();
        if(StringUtil.hasValue(prefix)) {
            name.append(prefix).append(":");
        }
        name.append(qName.getLocalName());
        element.addAttribute(QM_NAME, name.toString());
        return element;
    }

}
