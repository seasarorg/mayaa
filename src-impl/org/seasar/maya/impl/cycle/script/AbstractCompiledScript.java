/*
 * Copyright 2004-2005 the Seasar Foundation and the Others.
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
package org.seasar.maya.impl.cycle.script;

import org.seasar.maya.cycle.script.CompiledScript;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class AbstractCompiledScript 
        implements CompiledScript {

    private String _text;
    private SourceDescriptor _source;
    private String _encoding;
    private Class _expectedType = Object.class;
    
    public AbstractCompiledScript(String text) {
        if(text == null) {
            throw new IllegalArgumentException();
        }
        _text = text;
    }

    public AbstractCompiledScript(
            SourceDescriptor source, String encoding) {
        if(source == null) {
            throw new IllegalArgumentException();
        }
        _source = source;
        _encoding = encoding;
    }
    
    public void setExpectedType(Class expectedType) {
        if(expectedType == null) {
            throw new IllegalArgumentException();
        }
        _expectedType = expectedType;
    }

    public Class getExpectedType() {
        return _expectedType;
    }
    
    public boolean isLiteral() {
        return false;
    }

    protected boolean usingSource() {
        return _source != null;
    }
    
    protected String getText() {
        return _text;
    }
    
    protected SourceDescriptor getSource() {
        return _source;
    }

    protected String getEncoding() {
        if(StringUtil.isEmpty(_encoding)) {
            return System.getProperty("file.encoding", "UTF-8");
        }
        return _encoding;
    }

    public String toString() {
        if(usingSource()) {
            return _source.getSystemID();
        }
        return ScriptUtil.getBlockSignedText(_text);
    }
    
    
    
}
