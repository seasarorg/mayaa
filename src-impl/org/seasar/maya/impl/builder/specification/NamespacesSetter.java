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

import java.util.Iterator;

import org.seasar.maya.builder.specification.InjectionChain;
import org.seasar.maya.builder.specification.InjectionResolver;
import org.seasar.maya.engine.Template;
import org.seasar.maya.engine.specification.NodeNamespace;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;

/**
 * オリジナルノードの名前空間モデルを、インジェクト結果にコピーする。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class NamespacesSetter implements InjectionResolver, CONST_IMPL {
    
    public SpecificationNode getNode(
            Template template, SpecificationNode original, InjectionChain chain) {
        if(template == null || original == null || chain == null) {
            throw new IllegalArgumentException();
        }
        SpecificationNode injected =  chain.getNode(template, original);
        for(Iterator it = original.iterateNamespace(); it.hasNext(); ) {
            NodeNamespace namespace = (NodeNamespace)it.next();
            String uri = namespace.getNamespaceURI();
            if(URI_MAYA.equals(uri) == false) { 
                injected.addNamespace(namespace.getPrefix(), uri);
            }
        }
        return injected;
    }
    
    public void putParameter(String name, String value) {
        throw new UnsupportedOperationException();
    }

}
