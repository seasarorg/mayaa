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

import java.util.Iterator;

import org.xml.sax.Locator;

/**
 * 	設定XMLの構成物。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface SpecificationNode extends QNameable {
    
	/**
	 * ノード属性の追加。
	 * @param qName 属性名。
	 * @param value 属性値。
	 */
    void addAttribute(QName qName, String value);
    
    /**
     * 属性の取得。
     * @param qName 取得する属性のQName。
     * @return 属性オブジェクト。
     */
    NodeAttribute getAttribute(QName qName);
    
    /**
     * 属性のイテレート。
     * @return 属性（<code>NodeAttribute</code>）のイテレータ。
     */
    Iterator iterateAttribute();
    
    /**
     * 親ノードの設定をセットする。
     * @param parent 親ノード。
     */
    void setParentNode(SpecificationNode parent);

    /**
     * 親ノードを取得する。
     * @return 親ノード。
     */
    SpecificationNode getParentNode();
    
    /**
     * 子ノードの設定をセットする。
     * @param child 子ノード。
     */
    void addChildNode(SpecificationNode child);

    /**
     * 子ノードのイテレータを取得する。
     * @return 子ノード（<code>SpecificationNode</code>）を保持したイテレータ。
     */
    Iterator iterateChildNode();
    
    /**
     * ソース上の位置を取得。
     * @return 位置情報
     */
    Locator getLocator();
    
    /**
     * 自分のコピーを生成して返す。ただし、親ノードは設定されていない。
     * @return 自分のコピー。
     */
    SpecificationNode copyTo();

    /**
     * フィルタ付きで自分のコピーを生成して返す。ただし、親ノードは設定されていない。
     * @param filter コピー時フィルタ。
     * @return 自分のコピー。
     */
    SpecificationNode copyTo(CopyToFilter filter);

}
