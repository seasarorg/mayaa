/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which 
 * accompanies this distribution, and is available at
 * 
 *     http://homepage3.nifty.com/seasar/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language governing 
 * permissions and limitations under the License.
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
    private Class _expectedType;
    
    public AbstractCompiledScript(String text, Class expectedType) {
        if(StringUtil.isEmpty(text) || expectedType == null) {
            throw new IllegalArgumentException();
        }
        _text = text;
        _expectedType = expectedType;
    }

    public AbstractCompiledScript(
            SourceDescriptor source, String encoding, Class expectedType) {
        if(source == null || expectedType == null) {
            throw new IllegalArgumentException();
        }
        if(StringUtil.isEmpty(encoding)) {
            
        }
        _source = source;
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
    
}
