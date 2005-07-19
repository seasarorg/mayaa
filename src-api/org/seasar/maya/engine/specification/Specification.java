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
package org.seasar.maya.engine.specification;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.seasar.maya.source.SourceDescriptor;

/**
 * スペック情報にアクセスするためのインターフェイス
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface Specification extends SpecificationNode {

    /**
     * 子ノードにおいて名前空間プレフィックス省略時に適用する、エンジンデフォルトの名前空間。
     * @return デフォルト名前空間。
     */
    NodeNamespace getDefaultNamespace();
    
    /**
     * 最終ビルド時を取得する。
     * @return ビルド時。未ビルドの場合nullを返す。
     */
    Date getTimestamp();

    /**
     * 設定XMLのソース設定。
     * @param source 設定XMLソース。
     */
    void setSource(SourceDescriptor source);
    
	/**
	 * 設定XMLのソースを取得する。
	 * @return 設定XMLソース。
	 */
    SourceDescriptor getSource();
    
	/**
	 * ビルド例外発生時に、例外補足ブロックにおいて中途半端なビルド結果を殺す。
	 */
	void kill();

	/**
	 * 親スペックの取得。
	 * @return 親スペックもしくはnull。
	 */
    Specification getParentSpecification();
    
    /**
     * 親スペックへの登録キー取得。
     * @return 登録キー。
     */
    String getKey();
    
    /**
     * 子スペックのイテレート。
     * @return 子スペック(<code>Specification</code>)のイテレータ。
     */
    Iterator iterateChildSpecification();

    /**
     * XPath式を評価して、boolean値を返す。
     * @param xpathExpr 評価XPath式文字列。
	 * @param namespaces 式文字列中の名前空間を、プレフィックス=URIの表記で登録したマップ
	 * もしくはnull。
     * @return 評価結果。
     */
    boolean booleanValueOf(String xpathExpr, Map namespaces);
    
    /**
     * XPath式を評価して、Number値を返す。
     * @param xpathExpr 評価XPath式文字列。
	 * @param namespaces 式文字列中の名前空間を、プレフィックス=URIの表記で登録したマップ
	 * もしくはnull。
     * @return 評価結果。
     */
    Number numberValueOf(String xpathExpr, Map namespaces);

    /**
     * XPath式を評価して、文字列値を返す。
     * @param xpathExpr 評価XPath式文字列。
	 * @param namespaces 式文字列中の名前空間を、プレフィックス=URIの表記で登録したマップ
	 * もしくはnull。
     * @return 評価結果。
     */
    String stringValueOf(String xpathExpr, Map namespaces);

    /**
     * XPath式を評価して、マッチするノードセットを返す。
     * @param xpathExpr 評価XPath式文字列。
	 * @param namespaces 式文字列中の名前空間を、プレフィックス=URIの表記で登録したマップ
	 * もしくはnull。
     * @param cascade カスケードスイッチ。trueで親Specificationもカスケードして検索する。
     * @return 評価結果の<code>SpecificationNode</code>を保持するイテレータ。
     */
    Iterator selectChildNodes(String xpathExpr, Map namespaces, boolean cascade);
    
}
