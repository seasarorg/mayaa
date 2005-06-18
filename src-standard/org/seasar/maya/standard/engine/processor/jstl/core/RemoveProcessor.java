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

import org.seasar.maya.engine.processor.TemplateProcessorSupport;

/**
 * @author maruo_syunsuke
 */
public class RemoveProcessor extends TemplateProcessorSupport {

    private String _var   = null ;
    private int    _scope = PageContext.PAGE_SCOPE;
    
    public int doStartProcess(PageContext context) {
        remove(context);
        return Tag.EVAL_PAGE;
    }
    private void remove(PageContext context) {
        if( _var == null ) return ;

        context.removeAttribute(_var, _scope);
    }
    
    public void setScope(int scope) {
        if( ScopeUtil.isScopeValue(scope) == false ) return ; 
        _scope = scope;
    }
    public void setVar(String var) {
        _var = var;
    }
}