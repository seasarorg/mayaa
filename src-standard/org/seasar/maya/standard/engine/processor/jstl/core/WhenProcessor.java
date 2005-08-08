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

import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.engine.processor.TemplateProcessorSupport;
import org.seasar.maya.impl.util.ObjectUtil;

/**
 * @author maruo_syunsuke
 */
public class WhenProcessor extends TemplateProcessorSupport {

    private static final long serialVersionUID = -7666038937915220122L;

    private ProcessorProperty _test;
    
    public ProcessStatus doStartProcess() {
        checkThatParentIsChooseTag();
        if ( isAlreadyRunAnotherTagInChooseTag()) {
            return SKIP_BODY;
        }
        if (isTestValueTrue()) {
            getParentChooseProcessor().setRun();
            return EVAL_BODY_INCLUDE;
        }
        return SKIP_BODY;
    }

    private boolean isAlreadyRunAnotherTagInChooseTag() {
        ChooseProcessor chooseProcessor = getParentChooseProcessor();
        return chooseProcessor.isAlreadyRun();
    }

    private void checkThatParentIsChooseTag() {
        TemplateProcessor parentProcessor = getParentProcessor();
        if(parentProcessor == null || 
                parentProcessor instanceof ChooseProcessor == false) {
            throw new IllegalStateException();
        }
    }
    
    private ChooseProcessor getParentChooseProcessor() {
        return (ChooseProcessor)getParentProcessor();
    }

    private boolean isTestValueTrue() {
        if(_test == null) {
            throw new IllegalStateException();
        }
        return ObjectUtil.booleanValue(_test.getValue(), false);
    }
    
    public void setTest(ProcessorProperty test) {
        _test = test;
    }

}