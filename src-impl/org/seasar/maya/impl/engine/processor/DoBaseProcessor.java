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

import org.seasar.maya.engine.processor.TemplateProcessor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class DoBaseProcessor extends DoBodyProcessor {

    private static final long serialVersionUID = 6825307534213593235L;

    protected PageProcessor findPageProcessor() {
        for(TemplateProcessor current = this;
                current != null; current = current.getParentProcessor()) {
            if(current instanceof PageProcessor) {
                return (PageProcessor)current;
            }
        }
        return null;
    }
    
    public ProcessStatus doStartProcess() {
        PageProcessor pageProcessor = findPageProcessor();
        if(pageProcessor != null) {
            return pageProcessor.doBody();
        }
        return EVAL_BODY_INCLUDE;
    }
    
}
