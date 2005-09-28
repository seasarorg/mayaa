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
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.impl.cycle.CycleUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ExecProcessor extends TemplateProcessorSupport {

	private static final long serialVersionUID = -1413583265341468324L;

    private ProcessorProperty _script;
    private ProcessorProperty _src;
    private ProcessorProperty _encoding;
    
    // MLD property, expectedType=java.lang.String
    public void setSrc(ProcessorProperty src) {
        _src = src;
    }
    
    // MLD property, expectedType=java.lang.String
    public void setEncoding(ProcessorProperty encoding) {
        _encoding = encoding;
    }

    // MLD property, expectedType=void
    public void setScript(ProcessorProperty script) {
        _script = script;
    }
    
    public ProcessStatus doStartProcess(Page topLevelPage) {
        if(_src != null) {
            ServiceCycle cycle = CycleUtil.getServiceCycle();
            String srcValue = (String)_src.getValue().execute(null);
            String encValue = (String)_encoding.getValue().execute(null);
            cycle.load(srcValue, encValue);
        }
        if(_script != null) {
            _script.getValue().execute(null);
        }
        return ProcessStatus.EVAL_BODY_INCLUDE;
    }
    
}
