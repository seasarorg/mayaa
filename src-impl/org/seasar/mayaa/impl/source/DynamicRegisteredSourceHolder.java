/*
 * Copyright 2004-2022 the Seasar Foundation and the Others.
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
package org.seasar.mayaa.impl.source;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mitsutaka Watanabe
 */
public class DynamicRegisteredSourceHolder
        extends SourceDescriptorProvideSourceHolder {

    protected ChangeableRootSourceDescriptor getSourceDescriptor() {
        return new InMemorySourceDescriptor(this);
    }

    public void setRoot(String root) {
        // NOP
    }

    public String getContents(String systemId) {
        return contentRepository.get(systemId);
    }

    public Date getTimestamp(String systemId) {
        return contentTimestamp.get(systemId);
    }

    private static Map<String, String> contentRepository = new HashMap<>();
    private static Map<String, Date> contentTimestamp = new HashMap<>();
    
    public static void registerContents(String systemId, String content) {
        contentRepository.put(systemId, content);
        contentTimestamp.put(systemId, new Date());
    }

    public static void unregisterContents(String systemId) {
        contentRepository.remove(systemId);
        contentTimestamp.remove(systemId);
    }

    public static void unregisterAll() {
        contentRepository.clear();
        contentTimestamp.clear();
    }
}
