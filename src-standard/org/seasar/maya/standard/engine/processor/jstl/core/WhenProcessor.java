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

import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.engine.processor.TemplateProcessorSupport;

/**
 * @version $Revision: 1.1 $ $Date: 2005/06/07 00:30:46 $
 * @author maruo_syunsuke
 */
public class WhenProcessor extends TemplateProcessorSupport {

    private ProcessorProperty _test;
    
    public int doStartProcess(PageContext context) {
        if(context == null) {
            throw new IllegalArgumentException();
        }
        checkThatParentIsChooseTag();
        
        if( isAlreadyRunAnotherTagInChooseTag()) return Tag.SKIP_BODY;
        
        if ( isTestValueTrue(context) ) {
            getParentChooseProcessor().setRun();
            return Tag.EVAL_BODY_INCLUDE;
        }
        return Tag.SKIP_BODY;
    }

    private boolean isAlreadyRunAnotherTagInChooseTag() {
        ChooseProcessor chooseProcessor = getParentChooseProcessor();
        if( chooseProcessor.isAlreadyRun() ) return true ;
        return false ;
    }

    private void checkThatParentIsChooseTag(){
        TemplateProcessor parentProcessor = getParentProcessor();
        if( parentProcessor == null || parentProcessor instanceof ChooseProcessor == false )
            throw new IllegalStateException();
    }
    private ChooseProcessor getParentChooseProcessor() {
        return (ChooseProcessor)getParentProcessor();
    }

    private boolean isTestValueTrue(PageContext context) {
        if( _test == null )
            throw new IllegalStateException();
        Object elValue = _test.getValue(context);
        if( (elValue instanceof Boolean) == false ) 
            throw new IllegalStateException();
        
        Boolean bool = (Boolean)elValue;
        return bool.booleanValue();
    }
    
    ////
    public void setTest(ProcessorProperty test) {
        _test = test;
    }
}