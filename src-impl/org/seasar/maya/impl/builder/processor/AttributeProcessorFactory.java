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
package org.seasar.maya.impl.builder.processor;

import org.seasar.maya.builder.processor.ProcessorFactory;
import org.seasar.maya.el.CompiledExpression;
import org.seasar.maya.engine.Template;
import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.engine.specification.QNameable;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.engine.processor.AttributeProcessor;
import org.seasar.maya.impl.engine.processor.ProcessorPropertyImpl;
import org.seasar.maya.impl.util.ExpressionUtil;
import org.seasar.maya.impl.util.SpecificationUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class AttributeProcessorFactory implements ProcessorFactory, CONST_IMPL {
    
    public TemplateProcessor createProcessor(Template template, SpecificationNode injected) {
        AttributeProcessor processor = new AttributeProcessor();
        String qNameString = SpecificationUtil.getAttributeValue(injected, QM_Q_NAME);
        QNameable qNameable = SpecificationUtil.parseName(injected, template,
                injected.getLocator(), qNameString, URI_HTML);
        String value = SpecificationUtil.getAttributeValue(injected, QM_VALUE);
        CompiledExpression expression = ExpressionUtil.parseExpression(value, Object.class);
        Object obj = expression; 
        if(expression.isLiteralText()) {
            obj = expression.getExpression();
        }
        processor.setAttribute(new ProcessorPropertyImpl(
                qNameable.getQName(), qNameable.getPrefix(), obj));
        return processor;
    }
    
}
