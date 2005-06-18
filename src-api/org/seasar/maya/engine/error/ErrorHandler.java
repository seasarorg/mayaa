/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License"); you may
 * not use this file except in compliance with the License which accompanies
 * this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.seasar.maya.engine.error;

import javax.servlet.jsp.PageContext;

import org.seasar.maya.provider.Parameterizable;

/**
 * エラー処理のためのハンドラ。リクエストの最後、エラー画面の出力などを行う。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface ErrorHandler extends Parameterizable {

    /**
     * エラー処理メソッド。
     * @param context カレントコンテキスト。
     * @param t 処理対象のThrowable。
     */
    void doErrorHandle(PageContext context, Throwable t) throws Throwable;

}
