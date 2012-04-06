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
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class DOCTYPEProcessor extends TemplateProcessorSupport
            implements CONST_IMPL {

    private static final long serialVersionUID = 8518993579074245108L;

    private static final int DEFAULT_BUFFER_SIZE = 128;

    private String _name;
    private String _publicID;
    private String _systemID;

    public void setName(String name) {
        if (StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException();
        }
        _name = name;
    }

    public String getName() {
        return _name;
    }

    public void setPublicID(String publicID) {
        _publicID = publicID;
    }

    public String getPublicID() {
        return _publicID;
    }

    public void setSystemID(String systemID) {
        _systemID = systemID;
    }

    public String getSystemID() {
        return _systemID;
    }

    private void writeData(StringBuffer docTypeDecl) {
        docTypeDecl.append("<!DOCTYPE ").append(_name);
        if (StringUtil.hasValue(_publicID)) {
            docTypeDecl.append(" PUBLIC \"").append(_publicID).append("\"");
        }
        if (StringUtil.hasValue(_systemID)) {
            docTypeDecl.append(" \"").append(_systemID).append("\"");
        }
        docTypeDecl.append(">");
    }

    public ProcessStatus doStartProcess(Page topLevelPage) {
        StringBuffer docTypeDecl = new StringBuffer(DEFAULT_BUFFER_SIZE);
        writeData(docTypeDecl);
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.getResponse().write(docTypeDecl.toString());
        return ProcessStatus.SKIP_BODY;
    }

    public ProcessorTreeWalker[] divide(SequenceIDGenerator sequenceIDGenerator) {
        if (getOriginalNode().getQName().equals(QM_DOCTYPE) == false) {
            return new ProcessorTreeWalker[] { this };
        }
        StringBuffer sb = new StringBuffer();
        writeData(sb);
        LiteralCharactersProcessor literal =
            new LiteralCharactersProcessor(sb.toString());
        BuilderUtil.characterProcessorCopy(
                this, literal, sequenceIDGenerator);
        getStaticParentProcessor().removeProcessor(this);
        return new ProcessorTreeWalker[] { literal };
    }

}
