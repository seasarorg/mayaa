/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
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

import java.util.Stack;

import org.seasar.mayaa.cycle.scope.RequestScope;
import org.seasar.mayaa.impl.cycle.CycleUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class DoRenderProcessor extends TemplateProcessorSupport {

    private static final long serialVersionUID = -4276639032963176260L;

    private static final String INSERT_PROCESSOR_STACK =
        DoRenderProcessor.class.getName();

    private boolean _replace = true;
    private String _name = "";

    protected String getStackKey() {
        return INSERT_PROCESSOR_STACK + ":" + hashCode();
    }

    protected Stack getInsertProcessorStack() {
        RequestScope request = CycleUtil.getRequestScope();
        Stack stack = (Stack) request.getAttribute(getStackKey());
        if (stack == null) {
            stack = new Stack();
            request.setAttribute(getStackKey(), stack);
        }
        return stack;
    }

    // MLD property, default=true
    public void setReplace(boolean replace) {
        _replace = replace;
    }

    public boolean isReplace() {
        return _replace;
    }

    // MLD property, default=""
    public void setName(String name) {
        if (name != null) {
            _name = name;
        }
    }

    public String getName() {
        return _name;
    }

    public void pushInsertProcessor(InsertProcessor proc) {
        Stack stack = getInsertProcessorStack();
        stack.push(proc);
    }

    public InsertProcessor peekInsertProcessor() {
           Stack stack = getInsertProcessorStack();
        if (stack.size() > 0) {
               InsertProcessor proc = (InsertProcessor) stack.peek();
            return proc;
        }
        return null;
    }

    public void popInsertProcessor() {
        Stack stack = getInsertProcessorStack();
        stack.pop();
    }

}
