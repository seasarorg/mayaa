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
package org.seasar.mayaa.impl.cycle.scope;

import java.util.Iterator;
import java.util.Map;

import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.cycle.scope.AttributeScope;
import org.seasar.mayaa.engine.processor.ProcessorProperty;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.engine.processor.InsertProcessor;

/**
 * @author Masataka Kurihara (Gluegent, Inc)
 */
public class BindingScope extends AbstractReadOnlyAttributeScope {

    private static final long serialVersionUID = 5954219830862345209L;

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

    // AttributeScope implements -------------------------------------

    private static AttributeScope _paramScope;

    private static AttributeScope getParamScope() {
        if (_paramScope == null) {
            ServiceCycle cycle = CycleUtil.getServiceCycle();
            _paramScope = cycle.getAttributeScope("param");
        }
        return _paramScope;
    }

    public String getScopeName() {
        return "binding";
    }

    public Iterator iterateAttributeNames() {
        InsertProcessor processor = InsertProcessor.getRenderingCurrent();
        if (processor != null) {
            return new BindingIterator(
                    processor.getInformalProperties().iterator());
        }
        return getParamScope().iterateAttributeNames();
    }

    public boolean hasAttribute(String name) {
        InsertProcessor processor = InsertProcessor.getRenderingCurrent();
        if (processor != null) {
            return getTargetAttribute(processor, name) != null;
        }
        return getParamScope().hasAttribute(name);
    }

    public Object getAttribute(String name) {
        InsertProcessor processor = InsertProcessor.getRenderingCurrent();
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
        return getParamScope().getAttribute(name);
    }

    // support class ------------------------------------------------

    private static class BindingIterator implements Iterator {

        private Iterator _it;

        protected BindingIterator(Iterator it) {
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
