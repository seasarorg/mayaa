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

import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.engine.processor.ProcessorProperty;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.engine.EngineUtil;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ExecProcessor extends TemplateProcessorSupport {

    private static final long serialVersionUID = -1721362692638672941L;

    private ProcessorProperty _script;
    private ProcessorProperty _src;
    private ProcessorProperty _encoding;

    // MLD property, expectedClass=java.lang.String
    public void setSrc(ProcessorProperty src) {
        _src = src;
    }

    // MLD property, expectedClass=java.lang.String
    public void setEncoding(ProcessorProperty encoding) {
        _encoding = encoding;
    }

    // MLD property, expectedClass=void
    public void setScript(ProcessorProperty script) {
        _script = script;
    }

    public ProcessStatus doStartProcess(Page topLevelPage) {
        SpecificationUtil.endScope();
        try {
            if (_src != null) {
                ServiceCycle cycle = CycleUtil.getServiceCycle();

                String srcValue = StringUtil.valueOf(_src.getValue().execute(null));
                String encValue = StringUtil.valueOf(_encoding.getValue().execute(null));

                if (StringUtil.isRelativePath(srcValue)) {
                    String sourcePath = EngineUtil.getSourcePath(getStaticParentProcessor());
                    srcValue = StringUtil.adjustRelativePath(sourcePath, srcValue);
                }

                cycle.load(srcValue, encValue);
            }
            if (_script != null) {
                _script.getValue().execute(null);
            }
        } finally {
            SpecificationUtil.startScope(getVariables());
        }
        return ProcessStatus.EVAL_BODY_INCLUDE;
    }

}
