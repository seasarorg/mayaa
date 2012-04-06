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

import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;

import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class DelaySourceDescriptor extends ParameterAwareImpl
        implements SourceDescriptor {

    private static final long serialVersionUID = 1596798824321986307L;

    private SourceDescriptor _source;

    public boolean exists() {
        if (_source == null) {
            _source = SourceUtil.getSourceDescriptor(getSystemID());
            for (Iterator it = iterateParameterNames(); it.hasNext();) {
                String name = (String) it.next();
                String value = getParameter(name);
                _source.setParameter(name, value);
            }
        }
        return _source.exists();
    }

    public InputStream getInputStream() {
        if (exists()) {
            return _source.getInputStream();
        }
        return null;
    }

    public Date getTimestamp() {
        if (exists()) {
            return _source.getTimestamp();
        }
        return new Date(0);
    }

}
