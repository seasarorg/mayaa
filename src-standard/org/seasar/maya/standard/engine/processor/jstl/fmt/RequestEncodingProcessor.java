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
package org.seasar.maya.standard.engine.processor.jstl.fmt;

import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.standard.engine.processor.AbstractBodyProcessor;
import org.seasar.maya.standard.engine.processor.jstl.ProcessorPropertyString;

/**
 * @author maruo_syunsuke
 */
public class RequestEncodingProcessor extends AbstractBodyProcessor {

    private static final long serialVersionUID = -1290592779062531932L;

    public void setValue(String var) {
        if(StringUtil.isEmpty(var)) {
            throw new IllegalArgumentException();
        }
        super.setValue(new ProcessorPropertyString( var ));
    }
    
    public void setValue(ProcessorProperty value) {
        super.setValue(value);
    }
    
    public ProcessStatus process(Object obj) {
        // TODO impl
        return EVAL_PAGE;
    }

}