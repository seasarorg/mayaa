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

/**
 * PageContextのチューニング設定。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface PageContextSetting {

    /**
     * 内部PageContext生成オプション。指定しない場合、サーブレットコンテナにまかせる。
     * @return エラー発生時にフォワードするページURL。
     */
    String getErrorPageURL();

    /**
     * 内部PageContext生成オプション。デフォルトでは「8192」。
     * @return 書き出しバッファのサイズ。
     */
    int getBufferSize();
    
    /**
     * 内部PageContext生成オプション。デフォルトでは「false」
     * @return 自動セッション生成の有無。
     */
    boolean isNeedSession();

    /**
     * 内部PageContext生成オプション。デフォルト「false」。
     * @return 自動フラッシュするかどうか。
     */
    boolean isAutoFlush();    

}
