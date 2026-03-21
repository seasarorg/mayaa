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

import org.mozilla.javascript.Undefined;
import org.seasar.mayaa.builder.SequenceIDGenerator;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.cycle.script.CompiledScript;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.engine.processor.ProcessorProperty;
import org.seasar.mayaa.engine.processor.ProcessorTreeWalker;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.builder.BuilderUtil;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.cycle.script.ComplexScript;
import org.seasar.mayaa.impl.cycle.script.LiteralScript;
import org.seasar.mayaa.impl.cycle.script.RawOutputCompiledScript;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.util.EscapeUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CharactersProcessor extends TemplateProcessorSupport
        implements CONST_IMPL {

    private static final long serialVersionUID = -6762409726256198534L;

    private ProcessorProperty _text;
    private boolean _suppressAutoEscape;

    public CharactersProcessor() {
        // doNothing
    }

    public CharactersProcessor(CharactersProcessor share, String text) {
        this(share, text, false);
    }

    public CharactersProcessor(CharactersProcessor share, String text,
            boolean suppressAutoEscape) {
        this(share.getText(), text, suppressAutoEscape);
    }

    public CharactersProcessor(ProcessorProperty prop, String text) {
        this(prop, text, false);
    }

    public CharactersProcessor(ProcessorProperty prop, String text,
            boolean suppressAutoEscape) {
        ProcessorProperty propCopy = new ProcessorPropertyImpl(
                prop.getName(), text, prop.getExpectedClass());
        setText(propCopy);
        _suppressAutoEscape = suppressAutoEscape;
    }

    public void setText(ProcessorProperty text) {
        _text = text;
    }

    public ProcessorProperty getText() {
        return _text;
    }

    public ProcessStatus doStartProcess(Page topLevelPage) {
        String output = null;
        ProcessorProperty text = getText();
        CompiledScript script = text.getValue();
        SpecificationUtil.endScope();
        try {
            boolean autoEscape = AutoEscapeContext.isAutoEscapeEnabled()
                    && !_suppressAutoEscape
                    && !AutoEscapeContext.isAutoEscapeSuppressed();
            String escapeDetectionLevel = AutoEscapeContext.getEscapeDetectionLevel();
            OutputContext outputContext = OutputContextStack.current();

            if (script instanceof ComplexScript) {
                output = applyHtmlBodyAutoEscapePerBlock(
                        ((ComplexScript) script).getCompiledScripts(),
                        text.getExpectedClass(), autoEscape,
                        escapeDetectionLevel, outputContext);
            } else {
                Object value = text.getExecutedValue(null);
                if (value != null && value instanceof Undefined == false) {
                    output = applyHtmlBodyAutoEscape(value.toString(),
                            script, autoEscape, escapeDetectionLevel,
                            outputContext);
                }
            }
        } finally {
            SpecificationUtil.startScope(this.getVariables());
        }
        if (output != null) {
            ServiceCycle cycle = CycleUtil.getServiceCycle();
            cycle.getResponse().write(output);
        }
        return ProcessStatus.SKIP_BODY;
    }

    static String applyHtmlBodyAutoEscapePerBlock(CompiledScript[] scripts,
            Class<?> expectedClass,
            boolean autoEscapeEnabled,
            String escapeDetectionLevel,
            OutputContext outputContext) {
        if (scripts == null || scripts.length == 0) {
            return null;
        }
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < scripts.length; i++) {
            CompiledScript script = scripts[i];
            if (script == null) {
                continue;
            }
            Object value = script.execute(expectedClass, null);
            if (value == null) {
                continue;
            }
            String escaped = applyHtmlBodyAutoEscape(value.toString(),
                    script, autoEscapeEnabled, escapeDetectionLevel,
                    outputContext);
            if (escaped != null) {
                buffer.append(escaped);
            }
        }
        // 各script は実行しつつも、expectedClass が Void.class の場合は出力文字列は不要。
        if (expectedClass == Void.class) {
            return null;
        }
        return buffer.toString();
    }

    static String applyHtmlBodyAutoEscape(String value,
            CompiledScript script,
            boolean autoEscapeEnabled,
            String escapeDetectionLevel,
            OutputContext outputContext) {
        if (value == null) {
            return null;
        }
        if (outputContext == OutputContext.SCRIPT) {
            if (autoEscapeEnabled == false) {
                return value;
            }
            if (script == null || script instanceof LiteralScript
                    || script instanceof RawOutputCompiledScript) {
                return value;
            }
            if (EscapeUtil.isEscaped(value, escapeDetectionLevel)) {
                return value;
            }
            return EscapeUtil.escapeJavaScriptString(value);
        }
        if (autoEscapeEnabled == false) {
            return value;
        }
        if (script == null || script instanceof LiteralScript
                || script instanceof RawOutputCompiledScript) {
            return value;
        }
        if (outputContext == OutputContext.HTML_COMMENT) {
            return value;
        }
        if (EscapeUtil.isEscaped(value, escapeDetectionLevel)) {
            return value;
        }
        if (outputContext == OutputContext.STYLE) {
            return EscapeUtil.escapeCssString(value);
        }
        if (outputContext == OutputContext.HTML_BODY
                || outputContext == OutputContext.TEXTAREA_PRE) {
            return EscapeUtil.escapeHtmlBody(value);
        }
        return value;
    }


    public ProcessorTreeWalker[] divide(SequenceIDGenerator sequenceIDGenerator) {
        if (getOriginalNode().getQName().equals(QM_CHARACTERS) == false) {
            return new ProcessorTreeWalker[] { this };
        }
        CompiledScript script = getText().getValue();
        if (script.isLiteral()) {
            LiteralCharactersProcessor literal =
                new LiteralCharactersProcessor(script.getScriptText());
            BuilderUtil.characterProcessorCopy(this, literal, sequenceIDGenerator);
            return new ProcessorTreeWalker[] { literal };
        }
        return super.divide(sequenceIDGenerator);
    }

}
