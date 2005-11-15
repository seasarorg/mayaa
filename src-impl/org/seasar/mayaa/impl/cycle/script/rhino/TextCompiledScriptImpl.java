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
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrapFactory;
import org.mozilla.javascript.WrappedException;
import org.seasar.maya.PositionAware;
import org.seasar.maya.cycle.scope.AttributeScope;
import org.seasar.maya.impl.cycle.script.AbstractTextCompiledScript;
import org.seasar.maya.impl.cycle.script.ReadOnlyScriptBlockException;
import org.seasar.maya.impl.util.ObjectUtil;
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
    // for el style --------------
    private Script _elRhinoScript;
    private String _elScriptText;
    private String _elStyleName;
    private boolean _elStyle;
    
    public TextCompiledScriptImpl(String text, WrapFactory wrap, 
            PositionAware position) {
        super(text);
        _wrap = wrap;
        _sourceName = position.getSystemID();
        _lineNumber = position.getLineNumber();
        processText(text);
    }

    protected boolean maybeELStyle(String text) {
        boolean start = true;
        for(int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if(start) {
                if(Character.isJavaIdentifierStart(c)) {
                    start = false;
                    continue;
                }
            } else {
                if(Character.isJavaIdentifierPart(c)) {
                    continue;
                }
            }
            if(c == '.') {
                start = true;
                continue;
            }
            return false;
        }
        return true;
    }
    
    protected void processText(String text) {
        if(text == null) {
            throw new IllegalArgumentException();
        }
        text = text.trim();
        String script = text;
        String name = null;
        if(maybeELStyle(text)) {
            int pos = text.lastIndexOf('.');
            if(pos != -1 && pos != (text.length() - 1)) {
                script = text.substring(0, pos);
                name = text.substring(pos + 1);
            }
        }
        _elScriptText = script;
        _elStyleName = name;
        _elStyle = name != null;
    }

    protected Object normalExecute(Context cx, Scriptable scope) {
        if(cx == null || scope == null) {
            throw new IllegalArgumentException();
        }
        if(_rhinoScript == null) {
            _rhinoScript = cx.compileString(
                    getText(), _sourceName, _lineNumber, null);
        }
        return _rhinoScript.exec(cx, scope);
    }
    
    protected Object getELStyleHost(Context cx, Scriptable scope) {
        if(cx == null || scope == null) {
            throw new IllegalArgumentException();
        }
        if(StringUtil.isEmpty(_elScriptText)) {
            return null;
        }
        if(_elRhinoScript == null) {
            _elRhinoScript = cx.compileString(
                    _elScriptText, _sourceName, _lineNumber, null);
        }
        return _elRhinoScript.exec(cx, scope);
    }
    
    protected Object functionExecute(
            Context cx, Scriptable scope, Object host, Object[] args) {
        if(cx == null || scope == null || host == null || args == null) {
            throw new IllegalArgumentException();
        }
        if(host instanceof Scriptable) {
            Object func = ((Scriptable)host).get(_elStyleName, scope);
            return ((Function)func).call(cx, scope, (Scriptable)host, args);
        }
        Class[] argClasses = getMethodArgClasses();
        return ObjectUtil.invoke(host, _elStyleName, args, argClasses);
    }
    
    public Object execute(Object[] args) {
    	Context cx = RhinoUtil.enter(_wrap);
        Object ret = null;
        try {
            Scriptable scope = RhinoUtil.getScope();
            Object jsRet;
            if(_elStyle && args != null) {
                Object host = getELStyleHost(cx, scope);
                jsRet = functionExecute(cx, scope, host, args);
            } else {
                jsRet = normalExecute(cx, scope);
            }
            ret = RhinoUtil.convertResult(cx, getExpectedClass(), jsRet);
        } catch(WrappedException e) {
            RhinoUtil.removeWrappedException(e);
        } finally {
            Context.exit();
        }
        return ret;
    }

    public boolean isReadOnly() {
        return _elStyle == false;
    }

    public void assignValue(Object value) {
        if(isReadOnly()) {
            throw new ReadOnlyScriptBlockException(getScriptText());
        }
        Context cx = RhinoUtil.enter(_wrap);
        try {
            Scriptable scope = RhinoUtil.getScope();
            if(_elStyle) {
                Object host =  getELStyleHost(cx, scope);
                if(host == null) {
                    scope.put(_elStyleName, scope, value);
                } else if(host instanceof Scriptable) {
                    ((Scriptable)host).put(_elStyleName, scope, value);
                } else if(host instanceof AttributeScope) {
                    ((AttributeScope)host).setAttribute(_elStyleName, value);
                } else {
                    ObjectUtil.setProperty(host, _elStyleName, value);
                }
            } else {
                throw new IllegalStateException();
            }
        } catch(WrappedException e) {
            RhinoUtil.removeWrappedException(e);
        } finally {
            Context.exit();
        }
    }

}
