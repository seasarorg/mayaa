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
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CharactersProcessor extends TemplateProcessorSupport
        implements CONST_IMPL {

    private static final long serialVersionUID = -3111998528603997211L;

    private ProcessorProperty _text;

    public CharactersProcessor() {
        // doNothing
    }

    public CharactersProcessor(CharactersProcessor share, String text) {
        this(share.getText(), text);
    }

    public CharactersProcessor(ProcessorProperty prop, String text) {
        ProcessorProperty propCopy = new ProcessorPropertyImpl(
                prop.getName(), text, prop.getValue().getExpectedClass());
        setText(propCopy);
    }

    public void setText(ProcessorProperty text) {
        _text = text;
    }

    public ProcessorProperty getText() {
        return _text;
    }

    public ProcessStatus doStartProcess(Page topLevelPage) {
        Object value = null;
        SpecificationUtil.endScope();
        try {
            value = getText().getValue().execute(null);
        } finally {
            SpecificationUtil.startScope(this.getVariables());
        }
        if (value != null && value instanceof Undefined == false) {
            ServiceCycle cycle = CycleUtil.getServiceCycle();
            cycle.getResponse().write(value.toString());
        }
        return ProcessStatus.SKIP_BODY;
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
