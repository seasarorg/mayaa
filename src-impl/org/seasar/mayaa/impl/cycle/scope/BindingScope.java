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
package org.seasar.mayaa.impl.cycle.scope;

import java.util.Iterator;
import java.util.Map;

import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.cycle.scope.AttributeScope;
import org.seasar.mayaa.engine.processor.ProcessorProperty;
import org.seasar.mayaa.engine.processor.ProcessorTreeWalker;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.engine.processor.DoRenderProcessor;
import org.seasar.mayaa.impl.engine.processor.InsertProcessor;

/**
 * @author Masataka Kurihara (Gluegent, Inc)
 */
public class BindingScope extends AbstractReadOnlyAttributeScope {

    private static final long serialVersionUID = 5954219830862345209L;

    protected InsertProcessor getInsertProcessor() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        for (ProcessorTreeWalker current = cycle.getProcessor();
                current != null; current = current.getParentProcessor()) {
            if (current instanceof DoRenderProcessor) {
                DoRenderProcessor doRender = (DoRenderProcessor) current;
                return doRender.peekInsertProcessor();
            }
        }
        return null;
    }

    // AttributeScope implements -------------------------------------

    public String getScopeName() {
        return "binding";
    }

    public Iterator iterateAttributeNames() {
        InsertProcessor processor = getInsertProcessor();
        if (processor != null) {
            return new BindingIterator(
                    processor.getInformalProperties().iterator());
        }
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        AttributeScope param = cycle.getAttributeScope("param");
        return param.iterateAttributeNames();
    }

    protected ProcessorProperty getTargetAttribute(
            InsertProcessor processor, String name) {
        for (Iterator it = processor.getInformalProperties().iterator();
                it.hasNext();) {
            ProcessorProperty prop = (ProcessorProperty) it.next();
            if (prop.getName().getQName().getLocalName().equals(name)) {
                return prop;
            }
        }
        return null;
    }

    public boolean hasAttribute(String name) {
        InsertProcessor processor = getInsertProcessor();
        if (processor != null) {
            return getTargetAttribute(processor, name) != null;
        }
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        AttributeScope param = cycle.getAttributeScope("param");
        return param.hasAttribute(name);
    }

    public Object getAttribute(String name) {
        InsertProcessor processor = getInsertProcessor();
        if (processor != null) {
            ProcessorProperty prop = getTargetAttribute(processor, name);
            if (prop != null) {
                Map binding = processor.getRenderingParameters();
                if (binding != null) {
                    return binding.get(name);
                }
                return prop.getValue().execute(null);
            }
            return null;
        }
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        AttributeScope param = cycle.getAttributeScope("param");
        return param.getAttribute(name);
    }

    // support class ------------------------------------------------

    private class BindingIterator implements Iterator {

        private Iterator _it;

        private BindingIterator(Iterator it) {
            if (it == null) {
                throw new IllegalArgumentException();
            }
            _it = it;
        }

        public boolean hasNext() {
            return _it.hasNext();
        }

        public Object next() {
            ProcessorProperty prop = (ProcessorProperty) _it.next();
            return prop.getName().getQName().getLocalName();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

}
