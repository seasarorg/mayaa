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
package org.seasar.maya.impl.engine.specification;

import java.util.Iterator;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.specification.NodeAttribute;
import org.seasar.maya.engine.specification.NodeTreeWalker;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.Specification;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.cycle.CycleUtil;
import org.seasar.maya.impl.cycle.script.ScriptUtil;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SpecificationUtil implements CONST_IMPL {

    private SpecificationUtil() {
    }

    public static String getAttributeValue(
            SpecificationNode node, QName qName) {
        NodeAttribute nameAttr = node.getAttribute(qName);
        if(nameAttr != null) {
            return nameAttr.getValue();
        }
        return null;
    }

    public static Specification findSpecification(NodeTreeWalker current) {
        while(current instanceof Specification == false) {
            current = current.getParentNode();
            if(current == null) {
                return null;
            }
        }
        return (Specification)current;
    }

    public static Specification findSpecification() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        NodeTreeWalker current = cycle.getOriginalNode();
        return findSpecification(current);
    }

    public static SpecificationNode getMayaNode(NodeTreeWalker current) {
        Specification specification = findSpecification(current);
        for(Iterator it = specification.iterateChildNode(); it.hasNext(); ) {
            SpecificationNode node = (SpecificationNode)it.next();
            if(node.getQName().equals(QM_MAYA)) {
                return node;
            }
        }
        return null;
    }

    public static String getNodeBodyText(SpecificationNode node) {
        StringBuffer buffer = new StringBuffer();
        for(Iterator it = node.iterateChildNode(); it.hasNext(); ) {
            SpecificationNode child = (SpecificationNode)it.next();
            QName qName = child.getQName();
            if(QM_CDATA.equals(qName)) {
                buffer.append(getNodeBodyText(child));
            } else if(QM_CHARACTERS.equals(qName)) {
                buffer.append(SpecificationUtil.getAttributeValue(
                        child, QM_TEXT));
            } else {
                String name = child.getPrefix() + ":" + qName.getLocalName();
                throw new IllegalChildNodeException(name);
            }
        }
        return buffer.toString();
    }

    public static String getBlockSignedText(String text) {
        if(StringUtil.isEmpty(text)) {
            return text;
        }
        String blockSign = 
            ScriptUtil.getScriptEnvironment().getBlockSign();
        return text = blockSign + "{" + text.trim() + "}"; 
    }

    public static void initScope() {
        ScriptUtil.getScriptEnvironment().initScope();
    }

    public static void startScope(Object model) {
        ScriptUtil.getScriptEnvironment().startScope(model);
    }

    public static void endScope() {
        ScriptUtil.getScriptEnvironment().endScope();
    }
    
}
