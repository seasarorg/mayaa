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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaAdapter;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrapFactory;
import org.mozilla.javascript.WrappedException;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.scope.AttributeScope;
import org.seasar.maya.impl.cycle.CycleUtil;
import org.seasar.maya.impl.cycle.script.AbstractCompiledScript;
import org.seasar.maya.impl.cycle.script.ReadOnlyScriptBlockException;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CompiledScriptImpl extends AbstractCompiledScript {

    private static final long serialVersionUID = 4793923040332838492L;
    
    private String _sourceName;
    int _lineno;
    private WrapFactory _wrap;
    private Script _execScript;
    private boolean _assignParsed;
    private Script _assignScript;
    private String _assignName;
    
    public CompiledScriptImpl(String text, String sourceName, int lineno) {
        super(text);
        _sourceName = sourceName;
        _lineno = lineno;
    }
    
    public CompiledScriptImpl(SourceDescriptor source, String encoding) {
        super(source, encoding);
        _sourceName = source.getSystemID();
        _lineno = 1;
    }
    
    public void setWrapFactory(WrapFactory wrap) {
        if(wrap == null) {
            throw new IllegalArgumentException();
        }
        _wrap = wrap;
    }
    
    protected WrapFactory getWrapFactory() {
        return _wrap;
    }
    
    protected Scriptable getScope() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        AttributeScope attrs = cycle.getPageScope();
        if(attrs instanceof Scriptable) {
            return (Scriptable)attrs;
        }
        throw new IllegalStateException();
    }
    
    protected Script compile(Context cx) {
        if(usingSource()) {
            SourceDescriptor source = getSource();
            if(source.exists()) {
                InputStream stream = source.getInputStream();
                try {
                    Reader reader = 
                        new InputStreamReader(stream, getEncoding());
                    return cx.compileReader(reader, _sourceName, _lineno, null);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            throw new RuntimeException(
                    new FileNotFoundException(source.getSystemID()));
        }
        return cx.compileString(getText(), _sourceName, _lineno, null);
    }

    protected Object convertResult(Context cx, Object jsRet) {
        Object ret = null;
        Class expectedType = getExpectedType();
        if(expectedType.equals(Boolean.TYPE)) {
            // workaround to ECMA1.3 
            ret = JavaAdapter.convertResult(jsRet, Object.class);
        } else if(expectedType != Void.class) {
            ret = JavaAdapter.convertResult(jsRet, expectedType);
        }
        if(expectedType == Void.class) {
            ret = null;
        }
        return ret;
    }

    protected void removeWrappedException(WrappedException e) {
        Throwable t = e.getWrappedException();
        if(t instanceof RuntimeException) {
            throw (RuntimeException)t;
        }
        throw new RuntimeException(t);
    }
    
    public Object execute() {
        Object ret = null;
        Context cx = Context.enter();
        WrapFactory wrap = getWrapFactory();
        if(wrap != null) {
            cx.setWrapFactory(wrap);
        }
        try {
            if(_execScript == null) {
                _execScript = compile(cx);
            }
            Object jsRet = _execScript.exec(cx, getScope());
            ret = convertResult(cx, jsRet);
        } catch(WrappedException e) {
            removeWrappedException(e);
        } finally {
            Context.exit();
        }
        return ret;
    }

    protected void parseForAssigne() {
        if(_assignParsed == false) {

            // TODO ŽÀ‘•
            
            _assignParsed = true;
        }
    }
    
    public boolean isReadOnly() {
        parseForAssigne();
        return _assignName == null;
    }

    public void assignValue(Object value) {
        if(isReadOnly()) {
            throw new ReadOnlyScriptBlockException(toString());
        }
        Context cx = Context.enter();
        WrapFactory wrap = getWrapFactory();
        if(wrap != null) {
            cx.setWrapFactory(wrap);
        }
        try {
            Scriptable scope = getScope();
            if(_assignScript != null) {
                Scriptable host =  (Scriptable)_assignScript.exec(cx, scope);
                host.put(_assignName, scope, value);
            } else {
                scope.put(_assignName, scope, value);
            }
        } catch(WrappedException e) {
            removeWrappedException(e);
        } finally {
            Context.exit();
        }
    }

}
