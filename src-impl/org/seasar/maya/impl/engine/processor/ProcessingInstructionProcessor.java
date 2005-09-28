/*
 * Copyright 2004-2005 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.maya.impl.engine.processor;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.Page;
import org.seasar.maya.engine.processor.ProcessStatus;
import org.seasar.maya.impl.cycle.CycleUtil;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ProcessingInstructionProcessor 
		extends TemplateProcessorSupport {
    
	private static final long serialVersionUID = 6717263251948534639L;

	private String _target;
    private String _data;
    
    public void setTarget(String target) {
        if(StringUtil.isEmpty(target)) {
        	throw new IllegalArgumentException();
        }
        _target = target;
    }
    
    public String getTarget(){
        return _target;
    }

    public void setData(String data) {
        _data = data;
    }
    
    public String getData() {
        return _data;
    }
    
    public ProcessStatus doStartProcess(Page topLevelPage) {
        StringBuffer processingInstruction = new StringBuffer(128);
        processingInstruction.append("<?").append(_target);
        if(StringUtil.hasValue(_data)) {
            processingInstruction.append(" ").append(_data);
        }
        processingInstruction.append("?>\r\n");
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.getResponse().write(processingInstruction.toString());
        return ProcessStatus.SKIP_BODY;
    }

}
