/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
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
 */
package org.seasar.maya.standard.engine.processor.jstl.core;

import org.seasar.maya.engine.processor.TemplateProcessorSupport;

/**
 * @author maruo_syunsuke
 */
public class ChooseProcessor extends TemplateProcessorSupport {

    private static final long serialVersionUID = 3983358462082328502L;

    private boolean run ;
    
    public boolean isAlreadyRun() {
        return run;
    }
    
    public void setRun() {
        this.run = true;
    }

}