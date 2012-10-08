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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Date;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.WrappedException;
import org.seasar.mayaa.impl.cycle.script.AbstractSourceCompiledScript;
import org.seasar.mayaa.impl.engine.EngineUtil;
import org.seasar.mayaa.impl.util.IOUtil;
import org.seasar.mayaa.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SourceCompiledScriptImpl
extends AbstractSourceCompiledScript {

    private static final long serialVersionUID = 970613841877330176L;

    private transient Script _rhinoScript;
    private Date _compiledTimestamp;

    public SourceCompiledScriptImpl(SourceDescriptor source, String encoding) {
        super(source, encoding);
        _compiledTimestamp = new Date();
    }

    protected boolean needCheckTimestamp() {
        return EngineUtil.getEngineSettingBoolean(CHECK_TIMESTAMP, true);
    }

    /**
     * スクリプトが未コンパイルか、前回コンパイル時刻よりもタイムスタンプが
     * 新しければコンパイルする。
     *
     * @param cx コンパイルのためのコンテキスト
     * @param source コンパイルするソース
     */
    protected void compileFromSource(Context cx, SourceDescriptor source) {
        if (source == null) {
            throw new IllegalArgumentException();
        }

        if (_rhinoScript == null
                || (needCheckTimestamp() && source.getTimestamp().after(_compiledTimestamp))) {
            if (source.exists() == false) {
                throw new RuntimeException(
                        new FileNotFoundException(source.getSystemID()));
            }

            InputStream stream = null;
            Reader reader = null;
            try {
                stream = source.getInputStream();
                reader = new InputStreamReader(stream, getEncoding());
                _rhinoScript = cx.compileReader(
                        reader, source.getSystemID(), 1, null);
                _compiledTimestamp = source.getTimestamp();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (reader != null) {
                    IOUtil.close(reader);
                } else {
                    IOUtil.close(stream);
                }
            }
        }
    }

    public Object execute(Object[] args) {
        Context cx = RhinoUtil.enter();
        Object ret = null;
        try {
            compileFromSource(cx, getSource());
            Object jsRet = _rhinoScript.exec(cx, RhinoUtil.getScope());
            ret = RhinoUtil.convertResult(cx, getExpectedClass(), jsRet);
        } catch (WrappedException e) {
            RhinoUtil.removeWrappedException(e);
        } finally {
            Context.exit();
        }
        return ret;
    }

}
