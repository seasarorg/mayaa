/*
 * Copyright 2004-2005 the Seasar Foundation and the Others.
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
package org.seasar.maya.engine.specification;

import java.util.Iterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface NodeTreeWalker {
    
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
     * 子ノードのイテレータを取得する。
     * @return 子ノード（<code>NodeTreeWalker</code>）を保持したイテレータ。
     */
    Iterator iterateChildNode();
    
    /**
     * ソース上の行位置を取得。
     * @return 位置情報
     */
    int getLineNumber();
    
    /**
     * ソースのSystemIDを取得。
     * @return ソースSystemID。
     */
    String getSystemID();

}
