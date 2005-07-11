/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
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
import org.seasar.maya.standard.engine.processor.AttributeValue;
import org.seasar.maya.standard.engine.processor.AttributeValueFactory;
import org.seasar.maya.standard.util.JspUtil;

/**
 * @author maruo_syunsuke
 */
public class IfProcessor extends TemplateProcessorSupport {

    private static final long serialVersionUID = -6997783269188138513L;
    
    private ProcessorProperty _test;
    private String _var;
    private int _scope;

    // MLD property (dynamic, required)
    public void setTest(ProcessorProperty test) {
        if(test == null) {
            throw new IllegalArgumentException();
        }
        _test = test;
    }
    
    // MLD property (static)
    public void setVar(String var) {
        _var = var;
    }
    
    // MLD property (static)
    public void setScope(String scope) {
        _scope = JspUtil.getScopeFromString(scope);
    }
    
    public int doStartProcess(PageContext context) {
        if(context == null) {
            throw new IllegalArgumentException();
        }
        boolean test = ObjectUtil.booleanValue(_test.getValue(context), false);
        Boolean bool = new Boolean(test);
        //++ FIXME ServiceCycle導入後はコメントアウトコードに代える。
        AttributeValue val = AttributeValueFactory.create(_var, _scope);
        val.setValue(context, bool);
        // cycle.setAttribute(_var, _scope);
        // ServiceCycle#setAttribute()の引数はAttributeValueの趣旨を汲んで、nullも可能にした。
        //--
        if (test) {
            return Tag.EVAL_BODY_INCLUDE;
        }
        return Tag.SKIP_BODY;
    }

}