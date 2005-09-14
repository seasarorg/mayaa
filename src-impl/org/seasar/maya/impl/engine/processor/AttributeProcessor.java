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

import org.seasar.maya.cycle.script.CompiledScript;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.processor.ProcessorTreeWalker;
import org.seasar.maya.engine.specification.QNameable;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class AttributeProcessor extends TemplateProcessorSupport {

	private static final long serialVersionUID = -3985340947416654455L;

    private QNameable _name;
	private ProcessorProperty _value;
    
    private AbstractAttributableProcessor findParentAttributable() {
        for(ProcessorTreeWalker parent = getParentProcessor();
        		parent != null;
        		parent = parent.getParentProcessor()) {
	        if(parent instanceof AbstractAttributableProcessor) {
	            return (AbstractAttributableProcessor)parent;
	        }
        }
        throw new IllegalStateException();
    }
    
    // MLD property
    public void setName(QNameable name) {
        if(name == null) {
            throw new IllegalArgumentException();
        }
        _name = name;
    }
    
    // MLD property
    public void setValue(ProcessorProperty value) {
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
        parent.addProcesstimeProperty(
                new ProcessorPropertyWrapper(_name, _value));
        return SKIP_BODY;
    }
    
    private class ProcessorPropertyWrapper 
            implements ProcessorProperty {

        private QNameable _attrName;
        private ProcessorProperty _attrValue;
        
        private ProcessorPropertyWrapper(
                QNameable name, ProcessorProperty property) {
            if(name == null || property == null) {
                throw new IllegalArgumentException();
            }
            _attrName = name;
            _attrValue = property;
        }
        
        public QNameable getName() {
            return _attrName;
        }

        public CompiledScript getValue() {
            return _attrValue.getValue();
        }
        
    }
    
}
