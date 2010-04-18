/*
 * Copyright 2004-2010 the Seasar Foundation and the Others.
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

import org.seasar.mayaa.source.SourceDescriptor;
import org.seasar.mayaa.source.SourceHolder;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PageSourceDescriptor extends CompositeSourceDescriptor
    implements SourceDescriptorObserver {

    private static final long serialVersionUID = -6821253020265849514L;

    public boolean collectSourceDescriptor(
            SourceDescriptorObserver listener) throws Exception {
        for (Iterator it = SourceHolderFactory.iterator();
                it.hasNext();) {
            SourceHolder holder = (SourceHolder) it.next();
            if (listener.nextSourceDescriptor(
                    holder.getSourceDescriptor("")) == false) {
                return false;
            }
        }
        ClassLoaderSourceDescriptor loader =
            new ClassLoaderSourceDescriptor();
        loader.setRoot(ClassLoaderSourceDescriptor.META_INF);
        if (listener.nextSourceDescriptor(loader) == false) {
            return false;
        }
        return true;
    }

    public boolean nextSourceDescriptor(
            SourceDescriptor sourceDescriptor) {
        sourceDescriptor.setSystemID(getSystemID());
        addSourceDescriptor(sourceDescriptor);
        return true;
    }

    public void setSystemID(String systemID) {
        super.setSystemID(systemID);
        try {
            collectSourceDescriptor(this);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setParameter(String name, String value) {
        super.setParameter(name, value);
    }

}
