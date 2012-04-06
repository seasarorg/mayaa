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
package org.seasar.mayaa.impl.cycle;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.cycle.scope.AttributeScope;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ScopeIterator implements Iterator {

    private ServiceCycle _cycle;
    private Iterator _it;
    private String _current;

    public ScopeIterator(ServiceCycle cycle, Iterator it) {
        if (cycle == null || it == null) {
            throw new IllegalArgumentException();
        }
        _cycle = cycle;
        _it = it;
    }

    public boolean hasNext() {
        return ServiceCycle.SCOPE_APPLICATION.equals(_current) == false;
    }

    public Object next() {
        AttributeScope scope = null;
        if (_current == null) {
            AttributeScope page = _cycle.getPageScope();
            if (page != null) {
                scope = page;
                _current = ServiceCycle.SCOPE_PAGE;
            } else if (_it.hasNext()) {
                scope = (AttributeScope) _it.next();
                _current = scope.getScopeName();
            }
        } else if (ServiceCycle.SCOPE_REQUEST.equals(_current)) {
            scope = _cycle.getSessionScope();
            _current = ServiceCycle.SCOPE_SESSION;
        } else if (ServiceCycle.SCOPE_SESSION.equals(_current)) {
            scope = _cycle.getApplicationScope();
            _current = ServiceCycle.SCOPE_APPLICATION;
        } else if (ServiceCycle.SCOPE_APPLICATION.equals(_current)) {
            throw new NoSuchElementException();
        } else {
            if (_it.hasNext()) {
                scope = (AttributeScope) _it.next();
                _current = scope.getScopeName();
            } else {
                scope = _cycle.getRequestScope();
                _current = ServiceCycle.SCOPE_REQUEST;
            }
        }
        return scope;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

}
