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

import java.util.Iterator;
import java.util.Stack;

import org.seasar.mayaa.cycle.scope.AttributeScope;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.processor.IterationProcessor;
import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.engine.processor.ProcessorProperty;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.cycle.DefaultCycleLocalInstantiator;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.util.IteratorUtil;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class ForEachProcessor extends TemplateProcessorSupport
        implements IterationProcessor {

    private static final long serialVersionUID = 5143412585484588336L;

    private static final String PROCESS_TIME_INFO_KEY =
            ForEachProcessor.class.getName() + "#processTimeInfo";
    static {
        CycleUtil.registVariableFactory(PROCESS_TIME_INFO_KEY,
                new DefaultCycleLocalInstantiator() {
            public Object create(Object owner, Object[] params) {
                ForEachProcessor processor = (ForEachProcessor) owner;
                return processor.new IndexIteratorStack();
            }
        });
    }

    private String _var;
    protected ProcessorProperty _items;
    protected String _indexName;

    // MLD property, required
    public void setVar(String var) {
        _var = var;
    }

    // MLD property, required=true, expectedClass=void
    public void setItems(ProcessorProperty items) {
        if (items == null) {
            throw new IllegalArgumentException();
        }
        _items = items;
    }

    // MLD property
    public void setIndexName(String indexName) {
        _indexName = indexName;
    }

    public boolean isIteration() {
        return true;
    }

    /**
     * 次の要素があるかどうか判定します。
     * また、次の要素がある場合はそれを現在のスコープのvarにセットします。
     * このとき必要ならばindexもセットします。
     *
     * @return 次の要素があるならtrue
     */
    protected boolean prepareEvalBody() {
        IndexIteratorStack stack = (IndexIteratorStack) CycleUtil.getLocalVariable(
                PROCESS_TIME_INFO_KEY, this, null);
        IndexedIterator iterator = stack.peek();

        if (iterator.hasNext() == false) {
            return false;
        }

        AttributeScope currentScope = CycleUtil.getCurrentPageScope();
        currentScope.setAttribute(_var, iterator.next());
        if (_indexName != null) {
            currentScope.setAttribute(_indexName, iterator.getNextIndex());
        }
        return true;
    }

    public ProcessStatus doStartProcess(Page topLevelPage) {
        if (_items == null || _var == null) {
            throw new IllegalStateException();
        }
        IndexIteratorStack stack = (IndexIteratorStack) CycleUtil.getLocalVariable(
                PROCESS_TIME_INFO_KEY, this, null);
        stack.pushOne();

        if (prepareEvalBody() == false) {
            stack.pop();
            return ProcessStatus.SKIP_BODY;
        }
        return ProcessStatus.EVAL_BODY_INCLUDE;
    }

    public ProcessStatus doAfterChildProcess() {
        if (prepareEvalBody() == false) {
            IndexIteratorStack stack = (IndexIteratorStack) CycleUtil.getLocalVariable(
                    PROCESS_TIME_INFO_KEY, this, null);
            stack.pop();
            return ProcessStatus.SKIP_BODY;
        }
        return ProcessStatus.EVAL_BODY_AGAIN;
    }

    // for serialize

    private void readObject(java.io.ObjectInputStream in)
            throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
    }

    // support class

    /**
     * forEachの入れ子に対応するためのIndexIterator専用Stackクラス。
     *
     * @author Koji Suga (Gluegent Inc.)
     */
    private class IndexIteratorStack {
        private Stack _stack;

        public IndexIteratorStack() {
            _stack = new Stack();
        }

        public void pushOne() {
            _stack.push(new IndexedIterator());
        }

        public IndexedIterator pop() {
            return (IndexedIterator) _stack.pop();
        }

        public IndexedIterator peek() {
            return (IndexedIterator) _stack.peek();
        }
    }

    /**
     * nextの回数を取得できるIterator。
     * getNextIndex() は 0～を返します。
     * ただし余分な処理をしないために、next()の次にgetNextINdex()を呼ぶ
     * 形で使うことを前提とします。
     *
     * @author Koji Suga (Gluegent Inc.)
     */
    private class IndexedIterator {
        private int _index;
        private Iterator _iterator;

        public IndexedIterator() {
            if (_indexName != null) {
                _index = -1;
            }
            Object obj =
                ProviderUtil.getScriptEnvironment().convertFromScriptObject(
                        _items.getValue().execute(null));
            _iterator = IteratorUtil.toIterator(obj);
        }

        public Integer getNextIndex() {
            return new Integer(++_index);
        }

        public boolean hasNext() {
            return _iterator.hasNext();
        }

        public Object next() {
            return _iterator.next();
        }

    }
}
