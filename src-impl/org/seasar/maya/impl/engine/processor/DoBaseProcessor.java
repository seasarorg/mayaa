/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License"); you may
 * not use this file except in compliance with the License which accompanies
 * this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.seasar.maya.impl.engine.processor;

import org.seasar.maya.engine.processor.ProcessorTreeWalker;
import org.seasar.maya.impl.engine.RenderUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class DoBaseProcessor extends DoBodyProcessor {

    private static final long serialVersionUID = 6825307534213593235L;

    protected InsertProcessor getInsertProcessor() {
        for(ProcessorTreeWalker current = this;
                current != null; current = current.getParentProcessor()) {
            if(current instanceof DoRenderProcessor) {
                DoRenderProcessor doRender = (DoRenderProcessor)current;
                return doRender.peekInsertProcessor();
            }
        }
        return null;
    }
    
    public ProcessStatus doStartProcess() {
        InsertProcessor insert = getInsertProcessor();
        if(insert != null) {
            return RenderUtil.render(insert);
        }
        // direct access to component page.
        return EVAL_BODY_INCLUDE;
    }
    
}
