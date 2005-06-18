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
import org.seasar.maya.impl.util.SpecificationUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.standard.engine.processor.AbstractBodyProcessor;

/**
 * @author maruo_syunsuke
 */
public class SetAttributeProcessor extends AbstractBodyProcessor {

    private String _var;
    private int _scope = PageContext.PAGE_SCOPE;
    
    public void setVar(String var) {
        if(StringUtil.isEmpty(var)) {
            throw new IllegalArgumentException();
        }
        _var = var;
    }
    
    public void setValue(ProcessorProperty value) {
        super.setValue(value);
    }
    
    public void setScope(String scope) {
        _scope = SpecificationUtil.getScopeFromString(scope);
    }
    
    public int process(PageContext context, Object obj) {
        if(StringUtil.isEmpty(_var) || _scope < PageContext.PAGE_SCOPE || 
                PageContext.APPLICATION_SCOPE < _scope) {
            throw new IllegalStateException();
        }
        context.setAttribute(_var, obj, _scope);
        return Tag.EVAL_PAGE;
    }

}