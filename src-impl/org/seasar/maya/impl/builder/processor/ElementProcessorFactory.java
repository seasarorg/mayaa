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
package org.seasar.maya.impl.builder.processor;

import java.util.Iterator;

import org.seasar.maya.builder.processor.ProcessorFactory;
import org.seasar.maya.engine.Template;
import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.engine.specification.NodeNamespace;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.QNameable;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.engine.processor.ElementProcessor;
import org.seasar.maya.impl.util.SpecificationUtil;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ElementProcessorFactory implements ProcessorFactory, CONST_IMPL {

    public TemplateProcessor createProcessor(Template template, SpecificationNode injected) {
        ElementProcessor processor = new ElementProcessor();
        String qNameString = SpecificationUtil.getAttributeValue(injected, QM_Q_NAME);
        if(StringUtil.hasValue(qNameString)) {
	        QNameable qNameable = SpecificationUtil.parseName(injected, template,
	                injected.getLocator(), qNameString, URI_HTML);
	        processor.setQName(qNameable.getQName());
	        processor.setPrefix(qNameable.getPrefix());
	        return processor;
        }
        String uri = SpecificationUtil.getAttributeValue(injected, QM_NAMESPACE_URI);
        String name = SpecificationUtil.getAttributeValue(injected, QM_LOCAL_NAME);
        if(StringUtil.hasValue(uri) && StringUtil.hasValue(name)) {
            QName qName = new QName(uri, name);
            processor.setQName(qName);
            Iterator it = injected.iterateNamespace(uri);
            if(it.hasNext()) {
                NodeNamespace namespace = (NodeNamespace)it.next();
                processor.setPrefix(namespace.getPrefix());
            }
            return processor;
        }
        throw new IllegalStateException();
    }
    
}
