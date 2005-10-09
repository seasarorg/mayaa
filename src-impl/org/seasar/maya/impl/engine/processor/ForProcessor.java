/*
 * Copyright 2004-2005 the Seasar Foundation and the Others.
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
package org.seasar.maya.impl.engine.processor;

import org.seasar.maya.engine.Page;
import org.seasar.maya.engine.processor.IterationProcessor;
import org.seasar.maya.engine.processor.ProcessStatus;
import org.seasar.maya.engine.processor.ProcessorProperty;
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
    private int _max = 256;
    private ThreadLocal _counter = new ThreadLocal();
    
    // MLD property, expectedClass=void
    public void setInit(ProcessorProperty init) {
        _init = init;
    }

    // MLD property, required=true, expectedClass=boolean
    public void setTest(ProcessorProperty test) {
        if(test == null) {
        	throw new IllegalArgumentException();
        }
        _test = test;
    }
    
    // MLD property, expectedClass=void
    public void setAfter(ProcessorProperty after) {
        _after = after;
    }

    // MLD property
    public void setMax(int max) {
        _max = max;
    }
    
	public boolean isIteration() {
		return true;
	}

	protected boolean execTest() {
        if(_test == null) {
        	throw new IllegalStateException();
        }
        int count = ((Integer)_counter.get()).intValue();
        if(0 <= _max && _max< count) {
            throw new TooManyLoopException(_max);
        }
        count++;
        _counter.set(new Integer(count));
        return ObjectUtil.booleanValue(_test.getValue().execute(null), false);
	}
	
    public ProcessStatus doStartProcess(Page topLevelPage) {
    	_counter.set(new Integer(0));
        if(_init != null) {
    		_init.getValue().execute(null);
    	}
        return execTest() ? ProcessStatus.EVAL_BODY_INCLUDE : ProcessStatus.SKIP_BODY;
    }

	public ProcessStatus doAfterChildProcess() {
        if(_after != null) {
        	_after.getValue().execute(null);
        }
        return execTest() ? ProcessStatus.EVAL_BODY_AGAIN : ProcessStatus.SKIP_BODY;
	}
    
}
