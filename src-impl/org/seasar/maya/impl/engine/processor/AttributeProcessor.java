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

import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.engine.processor.TemplateProcessorSupport;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class AttributeProcessor extends TemplateProcessorSupport {

	private static final long serialVersionUID = -3985340947416654455L;

	private ProcessorProperty _value;
    
    private AbstractAttributableProcessor findParentAttributable() {
        for(TemplateProcessor parent = getParentProcessor();
        		parent != null;
        		parent = parent.getParentProcessor()) {
	        if(parent instanceof AbstractAttributableProcessor) {
	            return (AbstractAttributableProcessor)parent;
	        }
        }
        throw new IllegalStateException();
    }
    
    // Factory property
    public void setAttribute(ProcessorProperty value) {
        if(value == null) {
            throw new IllegalArgumentException();
        }
        _value = value;
    }
    
    public ProcessStatus doStartProcess() {
        if(_value == null) {
            throw new IllegalStateException();
        }
        AbstractAttributableProcessor parent = findParentAttributable();
        parent.addProcesstimeProperty(_value);
        return SKIP_BODY;
    }
    
}
