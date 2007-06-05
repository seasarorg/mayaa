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

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.PropertyUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeObject;
import org.seasar.mayaa.PositionAware;
import org.seasar.mayaa.cycle.scope.AttributeScope;
import org.seasar.mayaa.engine.specification.PrefixAwareName;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.cycle.script.AbstractTextCompiledScript;
import org.seasar.mayaa.impl.cycle.script.ReadOnlyScriptBlockException;
import org.seasar.mayaa.impl.cycle.script.rhino.RhinoUtil;
import org.seasar.mayaa.impl.engine.specification.PrefixAwareNameImpl;
import org.seasar.mayaa.impl.util.ObjectUtil;

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
    protected final String _propertyName;
    protected final String _beanPropertyName;

    public AbstractGetterScript(
            String text, PositionAware position, int offsetLine,
            String attributeName, String propertyName, String[] beanPropertyNames) {
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
        _propertyName = propertyName;
        _beanPropertyName = (contains(beanPropertyNames, attributeName)) ?
                attributeName : null;
    }

    private boolean contains(String[] array, String test) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(test)) {
                return true;
            }
        }
        return false;
    }

    /**
     * スクリプトを実行した結果を取得します。
     * このメソッドではスクリプトをエミュレートし、スコープからの属性取得および
     * スコープのプロパティを取得して返します。
     *
     * @param args 引数。使用しない。
     * @return 変数の値を返します。見つからない場合はnullを返します。
     */
    public Object execute(Object[] args) {
        Context cx = RhinoUtil.enter();
        try {
            Object result = getAttribute();

            if (result != null && _propertyName != null) {
                if (result instanceof NativeObject) {
                    // Rhinoで作成したオブジェクトの場合
                    NativeObject no = (NativeObject) result;
                    result = no.get(_propertyName, no);
                } else if (result instanceof AttributeScope) {
// TODO __current__ とか __parent__ とかの場合
                    // スコープの場合
                    AttributeScope scope = (AttributeScope) result;
                    result = scope.getAttribute(_propertyName);
                } else {
                    // それ以外はBeanかMapとしてのアクセス
                    try {
                        result = PropertyUtils.getProperty(result, _propertyName);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException(e);
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            return RhinoUtil.convertResult(cx, getExpectedClass(), result);
        } finally {
            Context.exit();
        }
    }

    public String getAttributeName() {
        return _attributeName;
    }

    public String getPropertyName() {
        return _propertyName;
    }

    /**
     * {@link #execute(Object[])}で呼び出され、スコープから属性値を取得します。
     * 指定された属性名がスコープのプロパティを指している場合はプロパティの値を
     * 返します。
     *
     * @return 属性値
     */
    protected Object getAttribute() {
        AttributeScope scope = getScope();
        if (scope == null) {
            return null;
        }

        Object result = scope.getAttribute(_attributeName);
        if (result == null && _beanPropertyName != null) {
            result = ObjectUtil.getProperty(scope, _beanPropertyName);
        }
        return result;
    }

    /**
     * {@link #execute(Object[])}で呼び出されます。
     * GetterScriptの実装はこのメソッドでスコープを返してください。
     *
     * @return スコープ
     */
    protected abstract AttributeScope getScope();

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
