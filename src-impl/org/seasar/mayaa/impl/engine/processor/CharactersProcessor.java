/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
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

import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.engine.processor.ProcessorProperty;
import org.seasar.mayaa.impl.cycle.CycleUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CharactersProcessor extends TemplateProcessorSupport {

    private static final long serialVersionUID = 2054159396651833214L;

    public CharactersProcessor() {
        // doNothing
    }

    public CharactersProcessor(CharactersProcessor copy, String text) {
        this(copy, copy.getText(), text);
    }

    public CharactersProcessor(
            TemplateProcessorSupport copy, ProcessorProperty prop, String text) {
        setOriginalNode(copy.getOriginalNode());
        setInjectedNode(copy.getInjectedNode());
        setEvalBodyInclude(copy.isEvalBodyInclude());
        if (copy.getParentProcessor() != null) {
            setParentProcessor(copy.getParentProcessor(), copy.getIndex());
        }
        try {
            setProcessorDefinition(copy.getProcessorDefinition());
        } catch(IllegalStateException e) {
            // doNothing
        }
        ProcessorProperty propCopy = new ProcessorPropertyImpl(
                prop.getName(), text, prop.getValue().getExpectedClass());
        setText(propCopy);
    }

    private ProcessorProperty _text;

    public void setText(ProcessorProperty text) {
        _text = text;
    }

    public ProcessorProperty getText() {
        return _text;
    }

    public ProcessStatus doStartProcess(Page topLevelPage) {
        Object value = getText().getValue().execute(null);
        if (value != null) {
            ServiceCycle cycle = CycleUtil.getServiceCycle();
            cycle.getResponse().write(value.toString());
        }
        return ProcessStatus.SKIP_BODY;
    }

}
