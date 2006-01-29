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
package org.seasar.mayaa.impl.engine.processor;

import java.util.Iterator;

import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.processor.IterationProcessor;
import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.engine.processor.ProcessorProperty;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.util.IteratorUtil;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class ForEachProcessor extends TemplateProcessorSupport
        implements IterationProcessor {

    private static final long serialVersionUID = -1762792311844341560L;

    private String _var;
    private ProcessorProperty _items;
    private String _indexName;
    private ThreadLocal _iterator = new ThreadLocal();
    private ThreadLocal _index = new ThreadLocal();

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

    protected boolean next() {
        Iterator iterator = (Iterator) _iterator.get();
        if (iterator.hasNext() == false) {
            return false;
        }

        inclementIndex();
        CycleUtil.setAttribute(_var, iterator.next(), ServiceCycle.SCOPE_PAGE);
        return true;
    }

    public ProcessStatus doStartProcess(Page topLevelPage) {
        if (_items == null || _var == null) {
            throw new IllegalStateException();
        }

        Object obj =
                ProviderUtil.getScriptEnvironment().convertFromScriptObject(
                        _items.getValue().execute(null));
        Iterator iterator = IteratorUtil.toIterator(obj);
        _iterator.set(iterator);

        clear();

        return next() ? ProcessStatus.EVAL_BODY_INCLUDE : ProcessStatus.SKIP_BODY;
    }

    public ProcessStatus doAfterChildProcess() {
        return next() ? ProcessStatus.EVAL_BODY_AGAIN : ProcessStatus.SKIP_BODY;
    }

    protected void inclementIndex() {
        if (_indexName != null) {
            Integer next =
                new Integer(((Integer) _index.get()).intValue() + 1);
            _index.set(next);
            CycleUtil.setAttribute(_indexName, next, ServiceCycle.SCOPE_PAGE);
        }
    }

    protected void clear() {
        if (_indexName != null) {
            _index.set(new Integer(-1));
        }
    }

}
