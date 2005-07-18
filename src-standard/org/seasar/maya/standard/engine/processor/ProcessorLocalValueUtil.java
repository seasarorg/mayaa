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
package org.seasar.maya.standard.engine.processor;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.processor.TemplateProcessor;

public class ProcessorLocalValueUtil {
    
    public static void setObject(ServiceCycle cycle,
            TemplateProcessor processor, String localName, Object value) {
        cycle.setAttribute(getLocalValueName(processor) + localName, value);
    }

    public static Object getObject(ServiceCycle cycle,
            TemplateProcessor processor, String localName) {
        return cycle.getAttribute(getLocalValueName(processor) + localName);
    }
    
    private static String getLocalValueName(TemplateProcessor processor){
        String processorIdString = ProcessorLocalValueUtil.class.getName() + "@" ;
        while( processor == null ){
            processorIdString += Integer.toString(processor.getIndex()) + "_";
            processor = processor.getParentProcessor() ;
        }
        return processorIdString ;
    }

}
