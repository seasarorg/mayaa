/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
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
 * Expression各メソッドで、式中のアクセスプロパティが存在しないときに発生する例外。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PropertyNotFoundException extends ExpressionException {
    
	private static final long serialVersionUID = -5888901658424559272L;

	private Object _target;
    private Object _property;
    
    /**
     * @param target プロパティが見つからなかったターゲットオブジェクト。
     * @param property 見つからなかったプロパティ。
     */
    public PropertyNotFoundException(Object target, Object property) {
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
    * 見つからなかったプロパティの取得。
    * @return プロパティ。文字列もしくはインデックス値。
    */
    public Object getProperty() {
        return _property;
    }
    
}
