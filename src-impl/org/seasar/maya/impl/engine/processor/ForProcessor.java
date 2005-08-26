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

import org.seasar.maya.engine.processor.IterationProcessor;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.processor.TemplateProcessorSupport;
import org.seasar.maya.impl.util.ObjectUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ForProcessor extends TemplateProcessorSupport
		implements IterationProcessor {

	private static final long serialVersionUID = -1762792311844341560L;

	private ProcessorProperty _init;
	private ProcessorProperty _test;
	private ProcessorProperty _after;
    
    // MLD property, expectedType=void
    public void setInit(ProcessorProperty init) {
        _init = init;
    }

    // MLD property, required=true, expectedType=boolean
    public void setTest(ProcessorProperty test) {
        if(test == null) {
        	throw new IllegalArgumentException();
        }
        _test = test;
    }
    
    // MLD property, expectedType=void
    public void setAfter(ProcessorProperty after) {
        _after = after;
    }

	public boolean isIteration() {
		return true;
	}

	protected boolean execTest() {
        if(_test == null) {
        	throw new IllegalStateException();
        }
        return ObjectUtil.booleanValue(_test.getValue(), false);
	}
	
    public ProcessStatus doStartProcess() {
    	if(_init != null) {
    		_init.getValue();
    	}
        return execTest() ? EVAL_BODY_INCLUDE : SKIP_BODY;
    }

	public ProcessStatus doAfterChildProcess() {
        if(_after != null) {
        	_after.getValue();
        }
        return execTest() ? EVAL_BODY_AGAIN : SKIP_BODY;
	}
    
}
