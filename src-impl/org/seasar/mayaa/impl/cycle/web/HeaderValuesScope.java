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
package org.seasar.mayaa.impl.cycle.web;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.seasar.mayaa.impl.cycle.scope.AbstractReadOnlyAttributeScope;
import org.seasar.mayaa.impl.util.collection.EnumerationIterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc)
 */
public class HeaderValuesScope
        extends AbstractReadOnlyAttributeScope {

    private static final long serialVersionUID = -7755074034945921281L;

    private transient HttpServletRequest _request;

    public HeaderValuesScope(HttpServletRequest request) {
        if (request == null) {
            throw new IllegalArgumentException();
        }
        _request = request;
    }

    public String getScopeName() {
        return "headerValues";
    }

    public Iterator<String> iterateAttributeNames() {
        Enumeration<String> e = _request.getHeaderNames();
        return EnumerationIterator.getInstance(e);
    }

    @Override
    public boolean hasAttribute(String name) {
        for (Iterator<String> it = iterateAttributeNames(); it.hasNext();) {
            String headerName = it.next();
            if (headerName.equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object getAttribute(String name) {
        if (hasAttribute(name)) {
            Enumeration<?> headers = _request.getHeaders(name);
            List<Object> headerList = new ArrayList<>();
            while (headers.hasMoreElements()) {
                headerList.add(headers.nextElement());
            }
            return headerList.toArray(new String[headerList.size()]);
        }
        return null;
    }

}
