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
package org.seasar.maya.impl.builder;

import java.util.Iterator;

import org.seasar.maya.engine.specification.Namespaceable;
import org.seasar.maya.engine.specification.NodeAttribute;
import org.seasar.maya.engine.specification.NodeNamespace;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.QNameable;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.engine.specification.QNameImpl;
import org.seasar.maya.impl.engine.specification.QNameableImpl;
import org.seasar.maya.impl.engine.specification.SpecificationNodeImpl;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class BuilderUtil implements CONST_IMPL {

    private BuilderUtil() {
    }

    public static SpecificationNode createInjectedNode(
            QName qName, String uri, SpecificationNode original, boolean maya) {
        if(qName == null || original == null) {
            throw new IllegalArgumentException();
        }
        SpecificationNodeImpl node = new SpecificationNodeImpl(
                qName, original.getSystemID(), original.getLineNumber());
        if(StringUtil.hasValue(uri)) {
            for(Iterator it = original.iterateAttribute(); it.hasNext(); ) {
                NodeAttribute attr = (NodeAttribute)it.next();
                String attrURI = attr.getQName().getNamespaceURI();
                if(uri.equals(attrURI) || (maya && URI_MAYA.equals(attrURI))) {
                    node.addAttribute(attr.getQName(), attr.getValue());
                }
            }
        }
        node.setParentScope(original.getParentScope());
        return node;
    }

    public static QNameable parseName(
            Namespaceable namespaces, String qName) {
        String[] parsed = qName.split(":");
        String prefix = null;
        String localName = null;
        String namespaceURI = null;
        if(parsed.length == 2) {
            prefix = parsed[0];
            localName = parsed[1];
            NodeNamespace namespace = namespaces.getNamespace(prefix, true);
            if(namespace == null) {
                throw new PrefixMappingNotFoundException(prefix);
            }
            namespaceURI = namespace.getNamespaceURI();
        } else if(parsed.length == 1) {
            localName = parsed[0];
            NodeNamespace namespace = namespaces.getNamespace("", true);
            if(namespace != null) {
                namespaceURI = namespace.getNamespaceURI();
            } else {
                throw new PrefixMappingNotFoundException("");
            }
        } else {
            throw new IllegalNameException(qName);
        }
        QName retName = new QNameImpl(namespaceURI, localName);
        QNameable ret = new QNameableImpl(retName);
        ret.setParentScope(namespaces);
        return ret;
    }

    
}
