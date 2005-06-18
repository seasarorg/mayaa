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
 * Expression各メソッドで、式中のメソッドが存在しないときに発生する例外。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class MethodNotFoundException extends ExpressionException {
    
    private Object _target;
    private String _methodName;
    private Object[] _args;
    private boolean _staticCall;
    
    /**
     * @param target メソッドが見つからなかったターゲットオブジェクト、もしくはクラス型。
     * @param methodName 見つからなかったメソッド名。
     */
    public MethodNotFoundException(Object target, 
            String methodName, Object[] args, boolean staticCall) {
        _target = target;
        _methodName = methodName;
        if(_args != null) {
            _args = args;
        } else {
            _args = new Object[0];
        }
        _staticCall = staticCall;
    }
    
    /**
     * ターゲットオブジェクトの取得。
     * @return ターゲットオブジェクト、もしくはクラス型。
     */
   public Object getTarget() {
        return _target;
    }
    
   /**
    * 見つからなかったメソッドの取得。
    * @return メソッド名。
    */
    public String getMethodName() {
        return _methodName;
    }
    
    /**
     * 見つからなかったメソッドコールの引数。
     * @return 引数の配列。
     */
    public Object[] getArgs() {
        return _args;
    }
    
    /**
     * staticメソッドコールかどうか。
     * @return trueだとstaticなメソッドコール。
     */
    public boolean isStaticCall() {
        return _staticCall;
    }
    
}
