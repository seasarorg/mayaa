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

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.processor.ProcessorProperty;

/**
 * @author maruo_syunsuke
 */
public class ForTokensProcessor extends ForEachProcessor {

    private static final long serialVersionUID = 5664728733042219315L;
    private ProcessorProperty 	_item ;
    private ProcessorProperty 	_delims ;

    protected ReadOnlyList initReadOnlyList(ServiceCycle cycle) {
        return ForEachSupportUtil.toForEachList(
            (String)_item.getValue(cycle),
            (String)_delims.getValue(cycle) 
        );
    }
    
    public void setDelims(ProcessorProperty delims) {
        _delims = delims;
    }
    
    public void setItem(ProcessorProperty item) {
        _item = item;
    }
    
}

