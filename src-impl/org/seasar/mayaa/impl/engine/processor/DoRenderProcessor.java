/*
 * Copyright 2004-2012 the Seasar Foundation and the Others.
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
import org.seasar.mayaa.engine.processor.ProcessorTreeWalker;
import org.seasar.mayaa.impl.cycle.CycleUtil;

/**
 * コンポーネント、およびレイアウト共有時の本文となるプロセッサ。
 *
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class DoRenderProcessor extends TemplateProcessorSupport {

    private static final long serialVersionUID = -4923388726582458101L;

    private transient String _stackKey;
    private boolean _replace = true;
    private String _name = "";

    /**
     * InsertProcessorのスタックをリクエストスコープから取得します。
     * もし無ければ作成して返します。
     *
     * @return InsertProcessorのスタック
     */
    protected Stack getInsertProcessorStack() {
        if (_stackKey == null) {
            // スタックはインスタンスごとにひとつ
            _stackKey = DoRenderProcessor.class.getName() + ":" + hashCode();
        }
        RequestScope request = CycleUtil.getRequestScope();
        Stack stack = (Stack) request.getAttribute(_stackKey);
        if (stack == null) {
            stack = new Stack();
            request.setAttribute(_stackKey, stack);
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

    /**
     * 親プロセッサとして、直上のInsertProcessorがあるならそれを返す。
     */
    public ProcessorTreeWalker getParentProcessor() {
        InsertProcessor insert = peekInsertProcessor();
        if (insert != null) {
            return insert;
        }
        return super.getParentProcessor();
    }

    public ProcessorTreeWalker getStaticParentProcessor() {
        return super.getStaticParentProcessor();
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

    public InsertProcessor popInsertProcessor() {
        Stack stack = getInsertProcessorStack();
        return (InsertProcessor) stack.pop();
    }

}
