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
package org.seasar.mayaa.impl.cycle.script;

import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class AbstractSourceCompiledScript
        extends AbstractCompiledScript implements CONST_IMPL {

    private static final long serialVersionUID = -585846992294307594L;

    private SourceDescriptor _source;
    private String _encoding;

    public AbstractSourceCompiledScript(
            SourceDescriptor source, String encoding) {
        if (source == null) {
            throw new IllegalArgumentException();
        }
        _source = source;
        _encoding = encoding;
    }

    protected SourceDescriptor getSource() {
        return _source;
    }

    protected String getEncoding() {
        if (StringUtil.isEmpty(_encoding)) {
            return System.getProperty("file.encoding", SCRIPT_DEFAULT_CHARSET);
        }
        return _encoding;
    }

    public boolean isReadOnly() {
        return true;
    }

    public void assignValue(Object value) {
        throw new ReadOnlyScriptBlockException(toString());
    }

    public String getScriptText() {
        return _source.getSystemID();
    }

}
