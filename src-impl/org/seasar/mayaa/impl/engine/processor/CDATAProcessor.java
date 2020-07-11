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
import org.seasar.mayaa.impl.cycle.DefaultCycleLocalInstantiator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CDATAProcessor extends TemplateProcessorSupport
        implements CONST_IMPL {

    private static final long serialVersionUID = -4267623139201513906L;
    private static final String CDATAIN = "<![CDATA[";
    private static final String CDATAOUT = "]]>";

    /**
     * CDATAは入れ子で出力できないため、CDATAProcessorの呼び出しネストレベルをカウントするGlobalVariableを使用する。
     * 閉じタグのバランスが取れていないとCDATAが閉じないため注意する。
     * テンプレートに元々記載されている<![CDATA[ ]]> は検知できない（そもそもXMLパーサにより評価されないため期待通り）。
     */
    private static final String CDATA_IN_PROCESS_KEY = CDATAProcessor.class.getName() + "#nestLevel";
    static {
        CycleUtil.registVariableFactory(CDATA_IN_PROCESS_KEY,
            new DefaultCycleLocalInstantiator() {
                @Override
                public Object create(Object[] params) {
                    return Integer.valueOf(0);
                }
            }
        );
    }

    public ProcessStatus doStartProcess(Page topLevelPage) {
        //　入れ子のレベルを確認する。
        Integer nestLevel = (Integer) CycleUtil.getGlobalVariable(CDATA_IN_PROCESS_KEY, null);
        if (nestLevel.intValue() == 0) {
            ServiceCycle cycle = CycleUtil.getServiceCycle();
            cycle.getResponse().write(CDATAIN);
        }
        CycleUtil.setGlobalVariable(CDATA_IN_PROCESS_KEY, Integer.valueOf(nestLevel.intValue() + 1));
        return ProcessStatus.EVAL_BODY_INCLUDE;
    }

    public ProcessStatus doEndProcess() {
        //　入れ子のレベルを確認する。
        Integer nestLevel = (Integer) CycleUtil.getGlobalVariable(CDATA_IN_PROCESS_KEY, null);
        CycleUtil.setGlobalVariable(CDATA_IN_PROCESS_KEY, Integer.valueOf(nestLevel.intValue() - 1));

        if (nestLevel.intValue() <= 1 /* 1:最後のネストレベル */) {
            ServiceCycle cycle = CycleUtil.getServiceCycle();
            cycle.getResponse().write(CDATAOUT);
            CycleUtil.clearGlobalVariable(CDATA_IN_PROCESS_KEY);
        }
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
