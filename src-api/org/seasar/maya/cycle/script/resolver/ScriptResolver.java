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
package org.seasar.maya.cycle.script.resolver;

import org.seasar.maya.provider.Parameterizable;

/**
 * スクリプト評価リゾルバ。アプリケーションスコープにて共有されるので、
 * スレッドセーフに実装することが求められる。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface ScriptResolver extends Parameterizable {

    /**
     * 値取得のためのリゾルバメソッド。
     * @param name 取得する値の名前文字列。
     * @return 評価結果もしくは、UNDEFINED。
     */
    Object getVariable(String name);
    
    /**
     * 値設定のためのリゾルバメソッド。
     * @param name 設定する値の名前文字列。
     * @param value 設定する値。
     * @return 値設定を行った際に、true。処理をおこなわないとfalseを返す。
     */
    boolean setVariable(String name, Object value);
    
}
