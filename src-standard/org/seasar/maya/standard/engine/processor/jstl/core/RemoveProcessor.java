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

import org.seasar.maya.cycle.AttributeScope;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.processor.TemplateProcessorSupport;

/**
 * @author maruo_syunsuke
 */
public class RemoveProcessor extends TemplateProcessorSupport {

    private static final long serialVersionUID = 277626645808267048L;

    private String _var;
    private String _scope;

    public void setVar(String var) {
        _var = var;
    }
    
    public void setScope(String scope) {
        _scope = scope;
    }
    
    public ProcessStatus doStartProcess(ServiceCycle cycle) {
        if(_var != null) {
            AttributeScope scope = cycle.getAttributeScope(_scope);
            scope.setAttribute(_var, null);
        }
        return EVAL_PAGE;
    }

}