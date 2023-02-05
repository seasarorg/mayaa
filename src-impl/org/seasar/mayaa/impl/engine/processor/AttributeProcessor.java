/*
 * Copyright 2004-2012 the Seasar Foundation and the Others.
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

import org.seasar.mayaa.builder.PathAdjuster;
import org.seasar.mayaa.cycle.script.CompiledScript;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.engine.processor.ProcessorProperty;
import org.seasar.mayaa.engine.processor.ProcessorTreeWalker;
import org.seasar.mayaa.engine.specification.PrefixAwareName;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.engine.EngineUtil;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class AttributeProcessor extends TemplateProcessorSupport {

    private static final long serialVersionUID = -3393961364808306951L;

    private PrefixAwareName _name;
    private ProcessorProperty _value;
    private ProcessorProperty _escapeAmp;

    protected AbstractAttributableProcessor findParentAttributable() {
        for (ProcessorTreeWalker parent = getParentProcessor();
                parent != null;
                parent = parent.getParentProcessor()) {
            if (parent instanceof AbstractAttributableProcessor) {
                if (parent instanceof ElementProcessor) {
                    // duplicated ElementProcessor is not attributable.
                    if (((ElementProcessor) parent).isDuplicated()) {
                        continue;
                    }
                }
                return (AbstractAttributableProcessor) parent;
            }
        }
        throw new IllegalStateException("no attributable processor.");
    }

    // MLD property
    public void setName(PrefixAwareName name) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        _name = name;
    }

    protected PrefixAwareName getName() {
        return _name;
    }

    // MLD property
    public void setValue(ProcessorProperty value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }
        _value = value;
    }

    // MLD property
    public void setEscapeAmp(ProcessorProperty escapeAmp) {
        ProcessorUtil.checkBoolableProperty(escapeAmp);
        _escapeAmp = escapeAmp;
    }

    boolean isEscapeAmp() {
        return ProcessorUtil.toBoolean(_escapeAmp);
    }

    /**
     * 親プロセッサの{@link QName}を返す。ただし
     * {@code parent}が{@link ElementProcessor}の場合にはそのnameを返す。
     * @param parent 親プロセッサ
     * @return 親プロセッサの{@link QName}。
     */
    protected QName getParentQName(AbstractAttributableProcessor parent) {
        QName parentQName;
        if (parent.getClass() == ElementProcessor.class) {
            parentQName = ((ElementProcessor) parent).getName().getQName();
        } else {
            parentQName = parent.getOriginalNode().getQName();
        }
        return parentQName;
    }

    protected ProcessorProperty createProcessorProperty(PrefixAwareName name, ProcessorProperty value, String basePath) {
        return new ProcessorPropertyWrapper(_name, _value, basePath);
    }

    public ProcessStatus doStartProcess(Page topLevelPage) {
    	/* 値なし属性もサポート(ただし非推奨のため、mayaa.mldのrequiredを外さない限りこの機能は無効。)
        if (_value == null) {
            throw new IllegalStateException();
        }
        */
        AbstractAttributableProcessor parent = findParentAttributable();

        QName parentQName = getParentQName(parent);
        QName attributeQName = getName().getQName();
        // 自動的にmayaaネームスペースを引き継いだだけであれば、親要素と同じにする。
        if (getName().getPrefix().equals("")
                && parentQName.equals(getName().getQName()) == false) {
            attributeQName = SpecificationUtil.createQName(
                    parentQName.getNamespaceURI(),
                    attributeQName.getLocalName());
            setName(SpecificationUtil.createPrefixAwareName(attributeQName, ""));
        }

        String basePath = null;
        PathAdjuster adjuster = ProviderUtil.getPathAdjuster();
        if (adjuster.isTargetAttribute(parentQName, attributeQName)) {
            String contextPath = CycleUtil.getRequestScope().getContextPath();
            String sourcePath = EngineUtil.getSourcePath(getStaticParentProcessor());
            basePath = contextPath + sourcePath;
        }

        parent.addProcesstimeProperty(createProcessorProperty(_name, _value, basePath));
        return ProcessStatus.SKIP_BODY;
    }

    // support class ------------------------------------------------

    protected class ProcessorPropertyWrapper implements ProcessorProperty {

        private static final long serialVersionUID = 8190328285510500572L;

        private PrefixAwareName _attrName;
        private ProcessorProperty _attrValue;
        private CompiledScript _script;
        private String _basePath;

        public ProcessorPropertyWrapper(
                PrefixAwareName name, ProcessorProperty property, String basePath) {
            if (name == null) {
                throw new IllegalArgumentException();
            }
            _attrName = name;
            _attrValue = property;
            _basePath = basePath;
            if (_attrValue != null) {
                _script = _attrValue.getValue();
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
        	if (_script == null) {
        		return getName().toString() + "=null";
        	}
            return getName().toString() + "=\"" + _script + "\"";
        }

        @Override
        public Object getExecutedValue(Object[] args) {
            Object result = _script.execute(String.class, args);
            if (StringUtil.hasValue(result)) {
                if (_basePath != null) {
                    PathAdjuster adjuster = ProviderUtil.getPathAdjuster();
                    result = adjuster.adjustRelativePath(_basePath, result.toString());
                }
            }
            return result;
        }
    
        @Override
        public Class<?> getExpectedClass() {
            return String.class;
        }

        public boolean escapeAmp() {
            return isEscapeAmp();
        }
    }
}
