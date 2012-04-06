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

import org.seasar.mayaa.PositionAware;
import org.seasar.mayaa.engine.specification.serialize.NodeReferenceResolverFinder;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface NodeTreeWalker extends PositionAware, NodeReferenceResolverFinder {

    /**
     * 親ノードの設定をセットする。
     * @param parentNode 親ノード。
     */
    void setParentNode(NodeTreeWalker parentNode);

    /**
     * 親ノードを取得する。
     * @return 親ノード。
     */
    NodeTreeWalker getParentNode();

    /**
     * 子ノードの設定をセットする。
     * @param childNode 子ノード。
     */
    void addChildNode(NodeTreeWalker childNode);

    /**
     * 子ノードの設定を指定位置に挿入する。
     * @param index
     * @param childNode
     */
    void insertChildNode(int index, NodeTreeWalker childNode);

    /**
     * 子ノードを削除する。
     * @param childNode 子ノード。
     * @return 削除に成功したらtrue、そうでなければfalse。
     */
    boolean removeChildNode(NodeTreeWalker childNode);

    /**
     * 子ノードの数を返す。
     * @return 子ノードの数
     */
    int getChildNodeSize();

    /**
     * 指定インデックスの子ノードを返す。
     * @param index インデックス値。
     * @return ノード。
     */
    NodeTreeWalker getChildNode(int index);

    /**
     * 子ノードのイテレータを取得する。
     * @return 子ノード（<code>NodeTreeWalker</code>）を保持したイテレータ。
     */
    Iterator iterateChildNode();

    /**
     * インスタンスを破棄対象とし、親ノードや子ノードなど
     * インスタンスが保持していた他のオブジェクトへの参照リンクを切る。
     * @deprecated
     */
    void kill();

    /**
     * 子ノードをクリアする
     */
    void clearChildNodes();
}
