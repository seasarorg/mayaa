/*
 * Copyright 2004-2005 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.maya.impl.engine.processor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.specification.NodeAttribute;
import org.seasar.maya.engine.specification.QNameable;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class EchoProcessor extends ElementProcessor implements CONST_IMPL {

    private static final long serialVersionUID = 3924111635172574833L;

    public void setOriginalNode(SpecificationNode node) {
        super.setOriginalNode(node);
        setupElement(node);
    }

    private void setupElement(SpecificationNode node) {
        super.setName(node);
        for (Iterator it = node.iterateAttribute(); it.hasNext();) {
            NodeAttribute attribute = (NodeAttribute) it.next();
            ProcessorPropertyImpl property =
                new ProcessorPropertyImpl(
                        attribute, attribute.getValue(), String.class);
            super.addInformalProperty(property);
        }
    }

    // MLD property of ElementProcessor
    public void setName(QNameable name) {
        // doNothing
    }

    // MLD method of ElementProcessor
    public void addInformalProperty(ProcessorProperty attr) {
        // doNothing
    }

    // ProcessorTreeWalker implements --------------------------------
    public Map getVariables() {
    	Iterator it = iterateInformalProperties();
        if(it.hasNext()) {
            Map attributeMap = new HashMap();
            while(it.hasNext()) {
                ProcessorProperty prop = (ProcessorProperty) it.next();
                attributeMap.put(
                        prop.getName().getQName().getLocalName(),
                        prop.getValue().execute(null));
            }
            return attributeMap;
        }
        return null;
    }

}
