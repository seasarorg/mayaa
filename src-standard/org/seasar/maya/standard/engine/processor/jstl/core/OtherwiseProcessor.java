/*
 * Copyright (c) 2004 the Seasar Project and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which 
 * accompanies this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 *
 * Created on 2005/03/19
 */
package org.seasar.maya.standard.engine.processor.jstl.core;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.seasar.maya.engine.processor.TemplateProcessorSupport;

/**
 * @version $Revision: 1.1 $ $Date: 2005/06/07 00:30:46 $
 * @author maruo_syunsuke
 */
public class OtherwiseProcessor extends TemplateProcessorSupport {
    public int doStartProcess(PageContext context) {
        if( (getParentProcessor() instanceof ChooseProcessor) == false )
            throw new IllegalStateException();
        ChooseProcessor chooseProcessor = (ChooseProcessor)getParentProcessor();
        if( chooseProcessor.isAlreadyRun() ) 
            return Tag.SKIP_BODY ;
        chooseProcessor.setRun();
        return Tag.EVAL_BODY_INCLUDE;
    }
}