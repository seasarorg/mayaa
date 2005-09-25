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
package org.seasar.maya.impl.cycle.script.rhino;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrapFactory;
import org.mozilla.javascript.WrappedException;
import org.seasar.maya.impl.cycle.script.AbstractTextCompiledScript;
import org.seasar.maya.impl.cycle.script.ReadOnlyScriptBlockException;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TextCompiledScriptImpl extends AbstractTextCompiledScript {

    private static final long serialVersionUID = 4793923040332838492L;
    
    private String _sourceName;
    int _lineNumber;
    private WrapFactory _wrap;
    private Script _rhinoScript;
    private boolean _parsed;
    private boolean _elStyle;
    private String _scriptText;
    private String _elStyleName;
    
    public TextCompiledScriptImpl(String text, WrapFactory wrap, 
            String sourceName, int lineNumber) {
        super(text);
        _wrap = wrap;
        _sourceName = sourceName;
        _lineNumber = lineNumber;
    }
    
    protected void compileFromText(Context cx, String text) {
        if(_rhinoScript == null && StringUtil.hasValue(_scriptText)) {
            _rhinoScript = cx.compileString(
                    text, _sourceName, _lineNumber, null);
        }
    }
    
    protected void processText() {
        if(_parsed == false) {
            
            // TODO 実装。
            
            _scriptText = getText();
            _elStyleName = null;
            _elStyle = false;
            _parsed = true;
        }
    }
    
    public Object execute(Object[] args) {
        
    	// TODO 式様式のメソッドコール。
    	
    	Context cx = RhinoUtil.enter(_wrap);
        Object ret = null;
        try {
            processText();
            compileFromText(cx, _scriptText);
            Scriptable scope = RhinoUtil.getScope();
            Object jsRet;
            if(_rhinoScript != null) {
                jsRet = _rhinoScript.exec(cx, scope);
            } else {
                jsRet = scope.get(_elStyleName, scope);
            }
            ret = RhinoUtil.convertResult(cx, getExpectedType(), jsRet);
        } catch(WrappedException e) {
            RhinoUtil.removeWrappedException(e);
        } finally {
            Context.exit();
        }
        return ret;
    }

    public boolean isReadOnly() {
        processText();
        return _elStyle;
    }

    public void assignValue(Object value) {
        if(isReadOnly()) {
            throw new ReadOnlyScriptBlockException(toString());
        }
        Context cx = RhinoUtil.enter(_wrap);
        try {
            Scriptable scope = RhinoUtil.getScope();
            compileFromText(cx, _scriptText);
            if(_rhinoScript != null) {
                Scriptable host =  (Scriptable)_rhinoScript.exec(cx, scope);
                host.put(_elStyleName, scope, value);
            } else {
                scope.put(_elStyleName, scope, value);
            }
        } catch(WrappedException e) {
            RhinoUtil.removeWrappedException(e);
        } finally {
            Context.exit();
        }
    }

}
