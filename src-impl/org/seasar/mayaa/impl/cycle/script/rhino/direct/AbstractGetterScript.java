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
package org.seasar.mayaa.impl.cycle.script.rhino.direct;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.seasar.mayaa.PositionAware;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.cycle.scope.AttributeScope;
import org.seasar.mayaa.engine.specification.PrefixAwareName;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.cycle.script.AbstractTextCompiledScript;
import org.seasar.mayaa.impl.cycle.script.ReadOnlyScriptBlockException;
import org.seasar.mayaa.impl.cycle.script.rhino.OffsetLineRhinoException;
import org.seasar.mayaa.impl.cycle.script.rhino.RhinoUtil;
import org.seasar.mayaa.impl.engine.specification.PrefixAwareNameImpl;
import org.seasar.mayaa.impl.util.ObjectUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * スコープから変数を取得するだけの処理をするスクリプトの抽象クラス。
 *
 * @author Koji Suga (Gluegent Inc.)
 */
public abstract class AbstractGetterScript extends AbstractTextCompiledScript {

    private static final long serialVersionUID = 1L;

    private static final Log LOG = LogFactory.getLog(AbstractGetterScript.class);

    private static final String[] SERVICE_CYCLE_PROPERTY_NAMES =
        ObjectUtil.getPropertyNames(ServiceCycle.class);

    protected final String _sourceName;
    protected final int _lineNumber;
    protected final int _offsetLine;

    protected final String _scopeName;
    protected final String _attributeName;
    protected final String _propertyName;
    protected final String _beanPropertyName;

    public AbstractGetterScript(
            String text, PositionAware position, int offsetLine, String scopeName,
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

        _scopeName = scopeName;
        _attributeName = attributeName;
        _propertyName = propertyName;
        _beanPropertyName = (contains(beanPropertyNames, attributeName)) ?
                attributeName : null;
    }

    private boolean contains(String[] array, String test) {
        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                if (array[i].equals(test)) {
                    return true;
                }
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
     * @throws OffsetLineRhinoException 属性がnullで、プロパティを取得しようとしたとき。
     */
    public Object execute(Object[] args) {
        Context cx = RhinoUtil.enter();
        try {
            Object result = getAttribute();

            if (_propertyName != null) {
                if (result == null) {
                    String script = getText();
                    String message = StringUtil.getMessage(
                            AbstractGetterScript.class, 1, _propertyName, script);
                    int[] position = extractLineSourcePosition(script, _propertyName);
                    String lineSource = script.substring(position[0], position[1]);
                    throw new OffsetLineRhinoException(
                            message, _sourceName, _lineNumber, lineSource,
                            lineSource.indexOf(_propertyName), _offsetLine, null);
                }

                result = readProperty(result);
            }

            return RhinoUtil.convertResult(cx, getExpectedClass(), result);
        } finally {
            Context.exit();
        }
    }

    /**
     * attributeからpropertyを読み出して返します。
     *
     * @param attribute propertyを読み出す属性
     * @return propertyの値、またはnull
     */
    private Object readProperty(Object attribute) {
        Object property = Undefined.instance;
        if (attribute instanceof NativeObject) {
            // Rhinoで作成したオブジェクトの場合
            NativeObject nativeObject = (NativeObject) attribute;
            Object nativeProperty = nativeObject.get(_propertyName, nativeObject);
            if (nativeProperty != Scriptable.NOT_FOUND) {
                property = nativeProperty;
            }
        } else if (attribute instanceof AttributeScope) {
            // TODO __current__ とか __parent__ とかの場合
            // スコープの場合
            AttributeScope scope = (AttributeScope) attribute;
            property = scope.getAttribute(_propertyName);
        } else if (attribute instanceof Map) {
            property = ((Map) attribute).get(_propertyName);
        } else {
            // それ以外はBeanとしてのアクセス
            try {
                property = RhinoUtil.getSimpleProperty(attribute, _propertyName);
            } catch (RuntimeException ignore) {
                // undefined
                LOG.debug(StringUtil.getMessage(AbstractGetterScript.class, 2,
                        _propertyName, attribute.getClass().getName()));
            }
        }
        return property;
    }

    protected static int[] extractLineSourcePosition(String text, String propertyName) {
        if (text.indexOf('\n') == -1) {
            return new int[] { 0, text.length() };
        }
        int index = text.lastIndexOf(propertyName);
        int head = text.lastIndexOf('\n', index);
        int tail = text.indexOf('\n', index + propertyName.length());
        if (tail == -1) {
            tail = text.length();
        }
        return new int[] { head + 1, tail };
    }

    public String getAttributeName(int index) {
        return _attributeName;
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
            if (_scopeName == null) {
                for (int i = 0; i < SERVICE_CYCLE_PROPERTY_NAMES.length; i++) {
                    if (SERVICE_CYCLE_PROPERTY_NAMES[i].equals(_attributeName)) {
                        ServiceCycle cycle = CycleUtil.getServiceCycle();
                        return ObjectUtil.getProperty(cycle, _attributeName);
                    }
                }
            }
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
