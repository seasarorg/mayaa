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

import java.util.Stack;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class DoRenderProcessor extends TemplateProcessorSupport {

	private static final long serialVersionUID = 4309532215454978747L;

    private boolean _rendered = false;
    private String _name = "";
    private ThreadLocal _insertProcs = new ThreadLocal();
    
    // MLD property, default=false
    public void setRendered(boolean rendered) {
        _rendered = rendered;
    }
    
    public boolean isRendered() {
        return _rendered;
    }
    
    // MLD property, default=""
    public void setName(String name) {
        if(name == null) {
            _name = name;
        }
        _name = name;
    }
    
    public String getName() {
        return _name;
    }

    private Stack getStack() {
    	Stack stack = (Stack)_insertProcs.get();
    	if(stack == null) {
    		stack = new Stack();
    		_insertProcs.set(stack);
    	}
    	return stack;
    }
    
    public void pushInsertProcessor(InsertProcessor proc) {
    	Stack stack = getStack();
        stack.push(proc);
    }
    
    public InsertProcessor peekInsertProcessor() {
       	Stack stack = getStack();
       	InsertProcessor proc = (InsertProcessor)stack.peek();
        return proc;
    }
    
    public void popInsertProcessor() {
    	Stack stack = getStack();
    	stack.pop();
    }
    
}
