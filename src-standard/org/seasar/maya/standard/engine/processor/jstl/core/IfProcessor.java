/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which 
 * accompanies this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package org.seasar.maya.standard.engine.processor.jstl.core;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.processor.TemplateProcessorSupport;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.SpecificationUtil;
import org.seasar.maya.standard.engine.processor.AttributeValue;
import org.seasar.maya.standard.engine.processor.AttributeValueFactory;

/**
 * @author maruo_syunsuke
 */
public class IfProcessor extends TemplateProcessorSupport {

    private ProcessorProperty _test;
    private ProcessorProperty _var;
    private ProcessorProperty _scope;
    
    public int doStartProcess(PageContext context) {
        if(context == null) {
            throw new IllegalArgumentException();
        }
        boolean testValue  = ObjectUtil.booleanValue(_test.getValue(context), false);
        int scopeIndex     = SpecificationUtil.getScopeFromString(_scope.getLiteral());
        String variantName = _var.getLiteral();
        AttributeValue val = AttributeValueFactory.create(variantName, scopeIndex);
        val.setValue(context, new Boolean(testValue));
        if ( testValue ) {
            return Tag.EVAL_BODY_INCLUDE;
        }
        return Tag.SKIP_BODY;
    }

    public void setTest(ProcessorProperty test) {
        if(test == null) {
            throw new IllegalArgumentException();
        }
        _test = test;
    }
    
    public void setVar(ProcessorProperty var) {
        _var = var;
    }
    
    public void setScope(ProcessorProperty scope) {
        _scope = scope;
    }
}