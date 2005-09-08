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
package org.seasar.maya.impl.engine.processor;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.impl.cycle.AbstractServiceCycle;

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
    
    public ProcessStatus doStartProcess() {
        if(_src != null) {
            ServiceCycle cycle = AbstractServiceCycle.getServiceCycle();
            String srcValue = (String)_src.getValue().execute();
            String encValue = (String)_encoding.getValue().execute();
            cycle.load(srcValue, encValue);
        }
        if(_script != null) {
            _script.getValue().execute();
        }
        return EVAL_BODY_INCLUDE;
    }
    
}
