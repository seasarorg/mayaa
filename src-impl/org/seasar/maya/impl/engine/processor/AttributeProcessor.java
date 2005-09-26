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

import org.seasar.maya.cycle.script.CompiledScript;
import org.seasar.maya.engine.Page;
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

    protected AbstractAttributableProcessor findParentAttributable() {
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

    public ProcessStatus doStartProcess(Page topLevelPage) {
        if(_value == null) {
            throw new IllegalStateException();
        }
        AbstractAttributableProcessor parent = findParentAttributable();
        parent.addProcesstimeProperty(
                new ProcessorPropertyWrapper(_name, _value));
        return SKIP_BODY;
    }

    // support class ------------------------------------------------
    
    protected class ProcessorPropertyWrapper 
            implements ProcessorProperty {

        private QNameable _attrName;
        private ProcessorProperty _attrValue;

        public ProcessorPropertyWrapper(
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

        public boolean equals(Object obj) {
            if (obj instanceof ProcessorProperty) {
                QNameable otherName = ((ProcessorProperty) obj).getName();
                return _name.getQName().equals(otherName.getQName());
            }
            return false;
        }

        public int hashCode() {
            return toString().hashCode();
        }

        public String toString() {
            return _name.toString() + "=\"" + _attrValue.getValue() + "\"";
        }
 
    }
    
}
