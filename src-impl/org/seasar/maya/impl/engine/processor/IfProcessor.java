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

import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.impl.util.ObjectUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class IfProcessor extends TemplateProcessorSupport {

	private static final long serialVersionUID = -7463140786021700658L;

	private ProcessorProperty _test;
    
    // MLD property, expectedType=boolean
    public void setTest(ProcessorProperty test) {
        if(test == null) {
        	throw new IllegalArgumentException();
        }
    	_test = test;
    }
    
    public ProcessStatus doStartProcess() {
        if(_test == null) {
        	throw new IllegalStateException();
        }
        boolean test = ObjectUtil.booleanValue(_test.getValue().execute(), false);
        return test ? TemplateProcessor.EVAL_BODY_INCLUDE : TemplateProcessor.SKIP_BODY;
    }
    
}
