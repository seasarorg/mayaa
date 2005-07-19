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
package org.seasar.maya.standard.builder.library;

import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.builder.library.PropertyDefinitionImpl;
import org.seasar.maya.impl.engine.processor.ProcessorPropertyImpl;
import org.seasar.maya.impl.util.ExpressionUtil;
import org.seasar.maya.standard.engine.processor.jsp.JspCustomTagProcessor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class JspPropertyDefinition extends PropertyDefinitionImpl {
    
    public Object getProcessorProperty(
            SpecificationNode injected, TemplateProcessor processor) {
        if(processor instanceof JspCustomTagProcessor == false) {
            throw new IllegalStateException();
        }
        QName qName = getQName(injected);
        String stringValue = getProcessValue(injected, qName);
        if(stringValue != null) {
            Object value = ExpressionUtil.parseExpression(stringValue, Object.class);
            String prefix = getPrefix(injected, qName);
            return new ProcessorPropertyImpl(qName, prefix, value);
        }
        return null;
    }
    
}
