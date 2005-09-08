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
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class WriteProcessor extends TemplateProcessorSupport {

	private static final long serialVersionUID = -8069702863937020350L;

    private ProcessorProperty _value;
    private String _default;
    
    // MLD property, expectedType=java.lang.String
    public void setValue(ProcessorProperty value) {
        _value = value;
    }
    
    public void setDefault(String defaultValue) {
    	_default = defaultValue;
    }
    
    public ProcessStatus doStartProcess() {
        if(_value != null) {
            String ret = (String)_value.getValue().execute();
            if(StringUtil.isEmpty(ret) && StringUtil.hasValue(_default)) {
            	ret = _default;
            }
            ServiceCycle cycle = AbstractServiceCycle.getServiceCycle();
            cycle.getResponse().write(ret);
        }
        return SKIP_BODY;
    }
    
}
