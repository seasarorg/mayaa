/*
 * Copyright 2004-2012 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.mayaa.engine.specification;

import java.util.Iterator;

/**
 * 設定XMLの構成物。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface SpecificationNode
        extends NodeTreeWalker, Namespace, PrefixAwareName {

	/**
	 * このノードのID
	 * @return ID
	 */
	String getId();

	/**
     * ビルド時に、スペック内でユニークに採番される値を設定する。
     * @param sequenceID スペック内のユニーク値。
     */
    void setSequenceID(int sequenceID);

    /**
     * スペック内でユニークに設定された値を取得する。
     * @return スペック内のユニーク値。
     */
    int getSequenceID();

    /**
     * ノード属性の追加。
     * @param qName 属性名。
     * @param value 属性値。
     */
    void addAttribute(QName qName, String value);

    /**
     * ノード属性の追加。
     * @param qName 属性名。
     * @param originalName 属性名(qNameにする前のオリジナル)
     * @param value 属性値。
     */
    void addAttribute(QName qName, String originalName, String value);

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
     * 属性の削除。
     * @param qName 削除する属性のQName。
     * @return 設定していたオブジェクト。存在しなかった場合はヌルを返す。
     */
    NodeAttribute removeAttribute(QName qName);

    /**
     * 属性を全て削除する。
     */
    void clearAttributes();

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
