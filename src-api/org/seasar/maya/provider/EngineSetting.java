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
package org.seasar.maya.provider;

/**
 * Engineのチューニング設定。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface EngineSetting {

    /**
     * カスタム設定項目の取得メソッド。
     * @param name 設定名。
     * @return 設定された項目。
     */
    String getParameter(String name);
    
    /**
     * Engineのオプション。
     * @return テンプレートや設定ファイルの保存日時を常にチェックする場合はtrue。
     */
    boolean isCheckTimestamp();

    /**
     * テンプレートのIgnorableWhitespaceを出力するかどうかの取得。
     * @return IgnorableWhitespaceの出力。trueで出力する。
     */
    boolean isOutputWhitespace();    
    
	/**
	 * Engineのオプション。デフォルトは「$」。
	 * @return テンプレート接尾辞の区切り文字列。
	 */
	String getSuffixSeparator();
    
	/**
	 * Engineのオプション。テンプレート上のmayaIDもしくはidで該当するノード
	 * が引けなかったときに、例外を投げるかどうかを取得する。
	 * @return trueでノードが引けなかったときの例外スロー。falseで無視。
	 */
	boolean isReportUnresolvedID();
	
}
