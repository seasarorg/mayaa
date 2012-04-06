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

import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.engine.processor.ProcessorTreeWalker;
import org.seasar.mayaa.impl.engine.RenderUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class DoBaseProcessor extends DoBodyProcessor {

    private static final long serialVersionUID = 6825307534213593235L;

    protected InsertProcessor getInsertProcessor() {
        for (ProcessorTreeWalker current = this;
                current != null; current = current.getParentProcessor()) {
            if (current instanceof DoRenderProcessor) {
                DoRenderProcessor doRender = (DoRenderProcessor) current;
                return doRender.peekInsertProcessor();
            }
        }
        return null;
    }

    public ProcessStatus doStartProcess(Page topLevelPage) {
        InsertProcessor insert = getInsertProcessor();
        if (insert != null) {
            return RenderUtil.renderProcessorTree(topLevelPage, insert);
        }
        // direct access to component page.
        return ProcessStatus.EVAL_BODY_INCLUDE;
    }

}
