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
package org.seasar.maya.engine.processor;

/**
 * 描画前に、ツリー構造やデータ構造の復元処理を行うプロセッサが実装する。
 * JSF等のミドルウェア対応機能。
 * @author Masataka Kurihara (Gluegent, Inc)
 */
public interface DecodeTreeWalker extends ProcessorTreeWalker {

	/**
	 * 開きタグにおけるデコードイベント。
     * @param topLevelPage 描画トップレベルのページ。
	 * @param parentDecode 直近のデコード。
	 */
	void doStartDecode(DecodeTreeWalker parentDecode);
	
	/**
	 * 閉じタグにおけるデコードイベント。
     * @param parentDecode 直近のデコード。
	 */
	void doEndDecode(DecodeTreeWalker parentDecode);
	
}
