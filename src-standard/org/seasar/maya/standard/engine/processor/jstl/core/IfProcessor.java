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
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.processor.TemplateProcessorSupport;
import org.seasar.maya.impl.util.ObjectUtil;

/**
 * @author maruo_syunsuke
 */
public class IfProcessor extends TemplateProcessorSupport {

    private static final long serialVersionUID = -6997783269188138513L;
    
    private ProcessorProperty _test;
    private String _var;
    private String _scope;

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
        _scope = scope;
    }
    
    public ProcessStatus doStartProcess(ServiceCycle cycle) {
        if(cycle == null) {
            throw new IllegalArgumentException();
        }
        boolean test = ObjectUtil.booleanValue(_test.getValue(cycle), false);
        Boolean bool = new Boolean(test);
        AttributeScope scope = cycle.getAttributeScope(_scope);
        scope.setAttribute(_var, bool);
        if (test) {
            return EVAL_BODY_INCLUDE;
        }
        return SKIP_BODY;
    }

}