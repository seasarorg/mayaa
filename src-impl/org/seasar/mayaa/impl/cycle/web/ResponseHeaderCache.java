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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Taro Kato (Gluegent, Inc.)
 */
public class ResponseHeaderCache {

    private Map _cacheWriteHeaders = new LinkedHashMap();

    public void setHeader(String name, String value) {
        List list = getHeaders(name);
        if (list.size() > 0) {
            list.set(list.size()-1, value);
        }
    }

    public void addHeader(String name, String value) {
        List list = getHeaders(name);
        list.add(value);
    }

    public List getHeaders(String name) {
        if (containsHeader(name)) {
            return (List)_cacheWriteHeaders.get(name);
        }

        List list = new ArrayList();
        _cacheWriteHeaders.put(name, list);
        return list;
    }

    public Set getHeaderNames() {
        return _cacheWriteHeaders.keySet();
    }

    public boolean containsHeader(String name) {
        return _cacheWriteHeaders.containsKey(name);
    }

}
