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
package org.seasar.maya.provider;

import javax.servlet.jsp.PageContext;

/**
 * アプリケーションスコープでのモデル提供オブジェクト。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface ModelProvider {

    /**
     * ユーザー定義のモデルオブジェクト取得メソッド。
     * @param context カレントコンテキスト。 
     * @param modelName コンポーネント名。
     * @param modelClass コンポーネントクラス型。
     * @param modelScope スコープ。
     * @return モデルオブジェクト。
     */
    Object getModel(PageContext context, 
            String modelName, Class modelClass, int modelScope);
    
}
