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
import org.seasar.mayaa.engine.processor.ProcessorProperty;
import org.seasar.mayaa.engine.processor.ProcessorTreeWalker;
import org.seasar.mayaa.engine.specification.PrefixAwareName;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.URI;
import org.seasar.mayaa.impl.builder.BuilderUtil;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CommentProcessor extends CharactersProcessor {

    private static final long serialVersionUID = -5176372123366627130L;
    private static final String COMMENTIN = "<!--";
    private static final String COMMENTOUT = "-->";

    private void writePart1(StringBuffer buffer) {
        buffer.append(COMMENTIN);
        if (getText() != null) {
            Object value;
            if (CycleUtil.isDraftWriting()) {
                value = getText().getValue().getScriptText();
            } else {
                value = getText().getValue().execute(null);
            }
            if (value != null) {
                buffer.append(value.toString());
            }
        }
    }

    private void writePart2(StringBuffer buffer) {
        buffer.append(COMMENTOUT);
    }

    public ProcessStatus doStartProcess(Page topLevelPage) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        StringBuffer buffer = new StringBuffer();
        writePart1(buffer);
        cycle.getResponse().write(buffer.toString());
        return ProcessStatus.EVAL_BODY_INCLUDE;
    }

    public ProcessStatus doEndProcess() {
        StringBuffer buffer = new StringBuffer();
        writePart2(buffer);
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.getResponse().write(buffer.toString());
        return ProcessStatus.EVAL_PAGE;
    }

    public ProcessorTreeWalker[] divide(SequenceIDGenerator sequenceIDGenerator) {
        if (getOriginalNode().getQName().equals(QM_COMMENT) == false) {
            return new ProcessorTreeWalker[] { this };
        }

        ProcessorTreeWalker[] results =
                new ProcessorTreeWalker[2 + getChildProcessorSize()];

        if (getText() == null) {
            setText(createVoidText());
        }

        StringBuffer sb = new StringBuffer();
        writePart1(sb);
        CharactersProcessor characterProcessor =
            new CharactersProcessor(this, sb.toString());
        BuilderUtil.characterProcessorCopy(this, characterProcessor, sequenceIDGenerator);
        results[0] = characterProcessor;

        for (int i = 0; i < getChildProcessorSize(); i++) {
            results[i + 1] = getChildProcessor(i);
            results[i + 1].setParentProcessor(getStaticParentProcessor());
        }

        LiteralCharactersProcessor literal =
            new LiteralCharactersProcessor(COMMENTOUT);
        BuilderUtil.characterProcessorCopy(this, literal, sequenceIDGenerator);
        results[results.length - 1] = literal;
        getStaticParentProcessor().removeProcessor(this);
        return results;
    }

    private ProcessorProperty createVoidText() {
        URI processorDefNameSpace =
            getProcessorDefinition().getLibraryDefinition().getNamespaceURI();
        String processorDefName = getProcessorDefinition().getName();
        QName qName = SpecificationUtil.createQName(processorDefNameSpace, processorDefName);
        // メモリ消費軽減と速度性能アップのためにキャッシュ利用
        // new NodeAttributeImpl(qName, "text");
        PrefixAwareName prefixAwareName =
            SpecificationUtil.createPrefixAwareName(qName, "");
        return new ProcessorPropertyImpl(
                prefixAwareName, "", String.class);
    }

}
