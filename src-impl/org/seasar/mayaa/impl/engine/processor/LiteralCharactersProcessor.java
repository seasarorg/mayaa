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

import org.seasar.mayaa.builder.library.LibraryManager;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.provider.ProviderUtil;

/**
 * @author Taro Kato (Gluegent, Inc.)
 */
public class LiteralCharactersProcessor extends TemplateProcessorSupport implements CONST_IMPL {
    private static final long serialVersionUID = -9037177269127933225L;

    private String _text;

    public LiteralCharactersProcessor(String text) {
        setText(text);
        LibraryManager libraryManager = ProviderUtil.getLibraryManager();
        setProcessorDefinition(
                libraryManager.getProcessorDefinition(QM_LITERALS));
    }


    public String getText() {
        return _text;
    }

    public void setText(String value) {
        _text = value;
    }

    public ProcessStatus doStartProcess(Page topLevelPage) {
        if (getText() != null) {
            ServiceCycle cycle = CycleUtil.getServiceCycle();
            cycle.getResponse().write(getText());
        }
        return ProcessStatus.SKIP_BODY;
    }

}
