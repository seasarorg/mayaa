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
import org.seasar.maya.engine.processor.TemplateProcessorSupport;
import org.seasar.maya.impl.util.CycleUtil;

/**
 * ボディテキストの出力を行うプロセッサ。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CharactersProcessor extends TemplateProcessorSupport {
    
	private static final long serialVersionUID = 2054159396651833214L;

	private ProcessorProperty _text;
    
    public void setText(ProcessorProperty text) {
        _text = text;
    }
    
    public ProcessorProperty getText() {
    	return _text;
    }
    
    protected Object getExpressed() {
        return _text.getValue();
    }
    
    public ProcessStatus doStartProcess() {
        Object value = getExpressed();
        if(value != null) {
            ServiceCycle cycle = CycleUtil.getServiceCycle();
            cycle.getResponse().write(value.toString());
        }
        return EVAL_BODY_INCLUDE;
    }

}
