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
package org.seasar.mayaa.impl.source;

import java.util.Iterator;

import org.seasar.mayaa.impl.util.collection.NullIterator;
import org.seasar.mayaa.source.SourceDescriptor;
import org.seasar.mayaa.source.SourceHolder;

/**
 * @author Taro Kato (Gluegent, Inc.)
 */
public abstract class SourceDescriptorProvideSourceHolder implements SourceHolder {

    protected abstract ChangeableRootSourceDescriptor getSourceDescriptor();

    private String _root = "";

    public String getRoot() {
        return _root;
    }

    public void setRoot(String root) {
        _root = root;
    }

    public SourceDescriptor getSourceDescriptor(String systemID) {
    	ChangeableRootSourceDescriptor sourceDescriptor = getSourceDescriptor();
        sourceDescriptor.setRoot(getRoot());
        sourceDescriptor.setSystemID(systemID);

        return sourceDescriptor;
    }

    public Iterator iterator(String[] filters) {
        FileSourceDescriptor root =
            (FileSourceDescriptor) getSourceDescriptor("");
        if (root.exists() == false) {
            return NullIterator.getInstance();
        }
        return new SystemIDFileSearchIterator(root.getFile(), filters);
    }
}
