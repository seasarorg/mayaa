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
package org.seasar.mayaa.impl.engine.processor;

import org.seasar.mayaa.cycle.script.CompiledScript;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.engine.processor.ProcessorProperty;
import org.seasar.mayaa.engine.processor.ProcessorTreeWalker;
import org.seasar.mayaa.engine.specification.PrefixAwareName;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class AttributeProcessor extends TemplateProcessorSupport {

    private static final long serialVersionUID = -8399232805371906530L;

    private PrefixAwareName _name;
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
    public void setName(PrefixAwareName name) {
        if(name == null) {
            throw new IllegalArgumentException();
        }
        _name = name;
    }

    protected PrefixAwareName getName() {
        return _name;
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
        return ProcessStatus.SKIP_BODY;
    }

    // support class ------------------------------------------------

    protected class ProcessorPropertyWrapper
            implements ProcessorProperty {

        private PrefixAwareName _attrName;
        private ProcessorProperty _attrValue;
        private CompiledScript _script;

        public ProcessorPropertyWrapper(
                PrefixAwareName name, ProcessorProperty property) {
            if(name == null || property == null) {
                throw new IllegalArgumentException();
            }
            _attrName = name;
            _attrValue = property;
            if (_attrValue.getValue().isLiteral()) {
                _script = new EscapedLiteralScript(_attrValue.getValue());
            } else {
                _script = new EscapableScript(_attrValue.getValue());
            }
        }

        public PrefixAwareName getName() {
            return _attrName;
        }

        public CompiledScript getValue() {
            return _script;
        }

        public boolean equals(Object obj) {
            if (obj instanceof ProcessorProperty) {
                PrefixAwareName otherName = 
                    ((ProcessorProperty) obj).getName();
                return getName().getQName().equals(otherName.getQName());
            }
            return false;
        }

        public int hashCode() {
            return toString().hashCode();
        }

        public String toString() {
            return getName().toString() + "=\"" + _script + "\"";
        }

    }

    protected class EscapableScript extends ScriptWrapper {

        private static final long serialVersionUID = -5393294025521796857L;

        public EscapableScript(CompiledScript script) {
            super(script);
        }

        public Object execute(Object[] args) {
            Object result = super.execute(args);
            if (_string && StringUtil.hasValue((String) result)) {
                result = escape((String) result);
            }
            return result;
        }

    }

    protected class EscapedLiteralScript extends ScriptWrapper {

        private static final long serialVersionUID = -441522603771461865L;

        protected String _escapedValue;

        public EscapedLiteralScript(CompiledScript script) {
            super(script);

            _escapedValue = (String) super.execute(null);
            if (_string && StringUtil.hasValue(_escapedValue)) {
                _escapedValue = escape(_escapedValue);
            }
        }

        public Object execute(Object[] args) {
            return _escapedValue;
        }

    }

    protected abstract class ScriptWrapper implements CompiledScript {

        protected CompiledScript _script;
        protected boolean _string;

        public ScriptWrapper(CompiledScript script) {
            _script = script;
            _string = String.class.equals(_script.getExpectedClass());
        }

        public void setExpectedClass(Class expectedClass) {
            _script.setExpectedClass(expectedClass);
        }

        public Class getExpectedClass() {
            return _script.getExpectedClass();
        }

        public Object execute(Object[] args) {
            return _script.execute(args);
        }

        public void setMethodArgClasses(Class[] methodArgClasses) {
            _script.setMethodArgClasses(methodArgClasses);
        }

        public Class[] getMethodArgClasses() {
            return _script.getMethodArgClasses();
        }

        public boolean isLiteral() {
            return _script.isLiteral();
        }

        public String getScriptText() {
            return _script.getScriptText();
        }

        public boolean isReadOnly() {
            return _script.isReadOnly();
        }

        public void assignValue(Object value) {
            _script.assignValue(value);
        }

        public String toString() {
            return _script.toString();
        }

        public String escape(String value) {
            return StringUtil.escapeWhitespace(StringUtil.escapeXml(value));
        }

    }

}
