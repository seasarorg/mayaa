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
package org.seasar.mayaa.impl.cycle.script.rhino;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.IdScriptableObject;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrappedException;
import org.seasar.mayaa.PositionAware;
import org.seasar.mayaa.cycle.scope.AttributeScope;
import org.seasar.mayaa.engine.specification.PrefixAwareName;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.cycle.script.AbstractTextCompiledScript;
import org.seasar.mayaa.impl.cycle.script.ReadOnlyScriptBlockException;
import org.seasar.mayaa.impl.engine.RenderingBrake;
import org.seasar.mayaa.impl.engine.specification.PrefixAwareNameImpl;
import org.seasar.mayaa.impl.util.ObjectUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TextCompiledScriptImpl extends AbstractTextCompiledScript {

    private static final long serialVersionUID = 4793923040332838492L;

    private final String _sourceName;
    private final int _lineNumber;
    private final int _offsetLine;
    private transient Script _rhinoScript;
    // for el style --------------
    private transient Script _elRhinoScript;
    private String _elScriptText;
    private String _elStyleName;
    private boolean _elStyle;

    public TextCompiledScriptImpl(
            String text, PositionAware position, int offsetLine) {
        super(text);
        String sourceName = position.getSystemID();
        if (position instanceof PrefixAwareName) {
            PrefixAwareName prefixAwareName = (PrefixAwareName) position;
            QName qName = prefixAwareName.getQName();
            if (CONST_IMPL.URI_MAYAA == qName.getNamespaceURI()) {
                sourceName += "#" + qName.getLocalName();
            } else {
                sourceName += "#"
                    + PrefixAwareNameImpl.forPrefixAwareNameString(qName,
                        prefixAwareName.getPrefix());
            }
        }
        _sourceName = sourceName;
        _lineNumber = position.getLineNumber();
        _offsetLine = offsetLine;
        processText(text);
    }

    protected boolean maybeELStyle(String text) {
        boolean start = true;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (start) {
                if (Character.isJavaIdentifierStart(c)) {
                    start = false;
                    continue;
                }
            } else {
                if (Character.isJavaIdentifierPart(c)) {
                    continue;
                }
            }
            if (c == '.') {
                start = true;
                continue;
            }
            return false;
        }
        return true;
    }

    protected void processText(String text) {
        if (text == null) {
            throw new IllegalArgumentException();
        }
        text = text.trim();
        String script = text;
        String name = null;
        if (maybeELStyle(text)) {
            int pos = text.lastIndexOf('.');
            if (pos != -1 && pos != (text.length() - 1)) {
                script = text.substring(0, pos);
                name = text.substring(pos + 1);
            }
        }
        _elScriptText = script;
        _elStyleName = name;
        _elStyle = (name != null);
    }

    protected Object normalExecute(Context cx, Scriptable scope) {
        if (cx == null || scope == null) {
            throw new IllegalArgumentException();
        }
        if (_rhinoScript == null) {
            _rhinoScript = cx.compileString(
                    getText(), _sourceName, _lineNumber + _offsetLine, null);
        }
        return _rhinoScript.exec(cx, scope);
    }

    protected Object getELStyleHost(Context cx, Scriptable scope) {
        if (cx == null || scope == null) {
            throw new IllegalArgumentException();
        }
        if (StringUtil.isEmpty(_elScriptText)) {
            return null;
        }
        if (_elRhinoScript == null) {
            _elRhinoScript = cx.compileString(
                    _elScriptText, _sourceName, _lineNumber + _offsetLine, null);
        }
        return _elRhinoScript.exec(cx, scope);
    }

    protected Object functionExecute(
            Context cx, Scriptable scope, Object host, Object[] args) {
        if (cx == null || scope == null || host == null || args == null) {
            throw new IllegalArgumentException();
        }
        if (host instanceof Scriptable) {
            Object func = ((Scriptable) host).get(_elStyleName, scope);
            return ((Function) func).call(cx, scope, (Scriptable) host, args);
        }
        Class[] argClasses = getMethodArgClasses();
        return ObjectUtil.invoke(host, _elStyleName, args, argClasses);
    }

    public Object execute(Object[] args) {
        Context cx = RhinoUtil.enter();
        Object ret = null;
        try {
            Scriptable scope = RhinoUtil.getScope();
            Object jsRet;
            if (_elStyle && args != null) {
                Object host = getELStyleHost(cx, scope);
                jsRet = functionExecute(cx, scope, host, args);
            } else {
                jsRet = normalExecute(cx, scope);
            }
            ret = RhinoUtil.convertResult(cx, getExpectedClass(), jsRet);
        } catch (RhinoException e) {
            if (e instanceof WrappedException) {
                WrappedException we = (WrappedException) e;
                Throwable wrapped = we.getWrappedException();
                if (wrapped instanceof RenderingBrake) {
                    RhinoUtil.removeWrappedException(we);
                }
            }

            // エラーとなったソース情報が微妙なので微調整。
            // 行番号はスクリプト次第でずれてしまう。
            int offsetLine;
            String message;
            String sourceName;
            if (e instanceof JavaScriptException
                    && ((JavaScriptException)e).getValue() instanceof IdScriptableObject) {
                offsetLine = -1;
                IdScriptableObject scriptable =
                    (IdScriptableObject) ((JavaScriptException) e).getValue();
                Object messageProperty = scriptable.get("message", scriptable);
                if (messageProperty != Scriptable.NOT_FOUND) {
                    message = messageProperty.toString();
                } else {
                    message = scriptable.toString();
                }
            } else {
                offsetLine = e.lineNumber() - _lineNumber + 1; // one "\n" is added
                message = e.details() + " in script=\n" + getText();
            }
            if (e.lineSource() == null && message != null) {
                String[] lines = message.split("\n");
                offsetLine = (lines.length > offsetLine) ? offsetLine : _offsetLine;
                if (offsetLine >= 0 && lines.length > offsetLine) {
                    e.initLineSource(lines[offsetLine]);
                    sourceName = _sourceName;
                } else {
                    sourceName = e.sourceName();
                }
            } else {
                sourceName = e.sourceName();
            }
            throw new OffsetLineRhinoException(
                    message,
                    sourceName, e.lineNumber(), e.lineSource(),
                    e.columnNumber(), offsetLine, e.getCause());
        } finally {
            Context.exit();
        }
        return ret;
    }

    public boolean isReadOnly() {
        return _elStyle == false;
    }

    public void assignValue(Object value) {
        if (isReadOnly()) {
            throw new ReadOnlyScriptBlockException(getScriptText());
        }
        Context cx = RhinoUtil.enter();
        try {
            Scriptable scope = RhinoUtil.getScope();
            if (_elStyle) {
                Object host =  getELStyleHost(cx, scope);
                if (host == null) {
                    scope.put(_elStyleName, scope, value);
                } else if (host instanceof Scriptable) {
                    ((Scriptable) host).put(_elStyleName, scope, value);
                } else if (host instanceof AttributeScope) {
                    ((AttributeScope) host).setAttribute(_elStyleName, value);
                } else {
                    ObjectUtil.setProperty(host, _elStyleName, value);
                }
            } else {
                throw new IllegalStateException();
            }
        } catch (WrappedException e) {
            RhinoUtil.removeWrappedException(e);
        } finally {
            Context.exit();
        }
    }

}
