/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which 
 * accompanies this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package org.seasar.maya.impl.el;

/**
 * Expression#setValue(...)において、プロパティがリードオンリーの場合に発生する例外。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PropertyNotWritableException extends ExpressionException {
    
	private static final long serialVersionUID = -4086709979130925980L;

	private Object _target;
    private Object _property;
    
    /**
     * @param target ターゲットオブジェクト。
     * @param property リードオンリーだったプロパティ名。
     */
    public PropertyNotWritableException(Object target, Object property) {
        _target = target;
        _property = property;
    }
    
    /**
     * ターゲットオブジェクトの取得。
     * @return ターゲットオブジェクト。
     */
    public Object getTarget() {
        return _target;
    }
    
    /**
     * リードオンリーだったプロパティの取得。
     * @return プロパティ。文字列もしくはインデックス値。
     */
    public Object getProperty() {
        return _property;
    }

}
