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

import org.seasar.mayaa.builder.SequenceIDGenerator;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.engine.processor.ProcessorTreeWalker;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.builder.BuilderUtil;
import org.seasar.mayaa.impl.cycle.CycleUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CDATAProcessor extends TemplateProcessorSupport
        implements CONST_IMPL {

    private static final long serialVersionUID = -4267623139201513906L;
    private static final String CDATAIN = "<![CDATA[";
    private static final String CDATAOUT = "]]>";

    public ProcessStatus doStartProcess(Page topLevelPage) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.getResponse().write(CDATAIN);
        return ProcessStatus.EVAL_BODY_INCLUDE;
    }

    public ProcessStatus doEndProcess() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.getResponse().write(CDATAOUT);
        return ProcessStatus.EVAL_PAGE;
    }

    public ProcessorTreeWalker[] divide(SequenceIDGenerator sequenceIDGenerator) {
        if (getOriginalNode().getQName().equals(QM_CDATA) == false) {
            return new ProcessorTreeWalker[] { this };
        }
        ProcessorTreeWalker[] results =
                new ProcessorTreeWalker[2 + getChildProcessorSize()];

        LiteralCharactersProcessor literalProcessor =
            new LiteralCharactersProcessor(CDATAIN);
        BuilderUtil.characterProcessorCopy(
                this, literalProcessor,sequenceIDGenerator);
        results[0] = literalProcessor;

        for (int i = 0; i < getChildProcessorSize(); i++) {
            results[i + 1] = getChildProcessor(i);
            results[i + 1].setParentProcessor(getStaticParentProcessor());
        }

        literalProcessor = new LiteralCharactersProcessor(CDATAOUT);
        BuilderUtil.characterProcessorCopy(
                this, literalProcessor, sequenceIDGenerator);
        results[results.length - 1] = literalProcessor;
        getStaticParentProcessor().removeProcessor(this);

        return results;
    }

}
