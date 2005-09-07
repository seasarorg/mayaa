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
import org.seasar.maya.cycle.AttributeScope;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.impl.cycle.script.AbstractCompiledScript;
import org.seasar.maya.impl.util.CycleUtil;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CompiledScriptImpl extends AbstractCompiledScript {

    private static final long serialVersionUID = 4793923040332838492L;
    
    private Script _script;
    private String _sourceName;
    int _lineno;
    private WrapFactory _wrap;
    
    public CompiledScriptImpl(String text,
            String blockSign, String sourceName, int lineno) {
        super(text, blockSign);
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
                    Reader reader = new InputStreamReader(stream, getEncoding());
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

    protected Object convertToPrimitive(Object obj) {
    	return obj;
    }
    
    public Object execute() {
        Object ret = null;
        Class expectedType = getExpectedType();
        Context cx = Context.enter();
        if(_wrap != null) {
            cx.setWrapFactory(_wrap);
        }
        try {
            if(_script == null) {
                _script = compile(cx);
            }
            Object value = _script.exec(cx, getScope());
            if(expectedType.equals(Boolean.TYPE)) {
                // workaround to ECMA1.3 
                ret = JavaAdapter.convertResult(value, Object.class);
            } else if(expectedType != Void.class) {
                ret = JavaAdapter.convertResult(value, expectedType);
            }
        } catch(WrappedException e) {
            Throwable t = e.getWrappedException();
            if(t instanceof RuntimeException) {
                throw (RuntimeException)t;
            }
            throw new RuntimeException(t);
        } finally {
            Context.exit();
        }
        if(expectedType == Void.class || ret == null) {
            return null;
        }
        return ret;
    }
    
}
