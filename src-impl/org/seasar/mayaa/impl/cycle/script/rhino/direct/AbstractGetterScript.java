/*
 * Copyright 2004-2007 the Seasar Foundation and the Others.
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
package org.seasar.mayaa.impl.cycle.script.rhino.direct;

import org.seasar.mayaa.PositionAware;
import org.seasar.mayaa.engine.specification.PrefixAwareName;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.cycle.script.AbstractTextCompiledScript;
import org.seasar.mayaa.impl.cycle.script.ReadOnlyScriptBlockException;
import org.seasar.mayaa.impl.cycle.script.rhino.RhinoUtil;
import org.seasar.mayaa.impl.engine.specification.PrefixAwareNameImpl;

/**
 * スコープから変数を取得するだけの処理をするスクリプトの抽象クラス。
 *
 * @author Koji Suga (Gluegent Inc.)
 */
public abstract class AbstractGetterScript extends AbstractTextCompiledScript {

    private static final long serialVersionUID = 1L;

    protected final String _sourceName;
    protected final int _lineNumber;
    protected final int _offsetLine;

    protected final String _attributeName;

    public AbstractGetterScript(
            String text, PositionAware position, int offsetLine, String attributeName) {
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

        _attributeName = attributeName;
    }

    /**
     * requestスコープから変数を取得します。
     *
     * @param args 引数。使用しない。
     * @return 変数の値を返します。見つからない場合はnullを返します。
     */
    public Object execute(Object[] args) {
        return RhinoUtil.convertResult(null, getExpectedClass(), getAttribute());
    }

    public String getAttributeName() {
        return _attributeName;
    }

    /**
     * {@link #execute(Object[])}で呼び出され、属性値を取得します。
     * GetterScriptの実装はこのメソッドで属性値を取得します。
     *
     * @return 属性値
     */
    protected abstract Object getAttribute();

    /**
     * 読み込み専用。
     *
     * @return true
     */
    public boolean isReadOnly() {
        return true;
    }

    /**
     * サポートしない。
     *
     * @param value 値
     * @throws ReadOnlyScriptBlockException
     */
    public void assignValue(Object value) {
        throw new ReadOnlyScriptBlockException(getScriptText());
    }

}
