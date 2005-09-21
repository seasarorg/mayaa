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
public interface Namespace extends NodeObject {

    /**
     * 親名前空間スコープの設定。 
     * @param parent 親の名前空間スコープ。
     */
    void setParentSpace(Namespace parent);

    /**
     * 親名前空間スコープの取得。
     * @return 親の名前空間スコープ。
     */
    Namespace getParentSpace();
    
    /**
     * 名前空間モデルの追加。
     * @param mapping 名前空間モデル。
     */
    void addPrefixMapping(PrefixMapping mapping);
    
    /**
     * このスコープにて、名前空間モデルを追加したかどうか。
     * @return このスコープに追加モデルがあるとtrue。
     */
    boolean addedMapping();
    
    /**
     * 名前空間モデルの取得。
     * @param prefix 取得したい名前空間のプレフィックス。
     * @param all 親スコープも検索する。
     * @return 名前空間モデル。
     */
    PrefixMapping getMappingFromPrefix(String prefix, boolean all);

    /**
     * 名前空間モデルの取得。
     * @param namespaceURI 取得したい名前空間のURI。
     * @param all 親スコープも検索する。
     * @return 名前空間モデル。
     */
    PrefixMapping getMappingFromURI(String namespaceURI, boolean all);
    
    /**
     * 適用される名前空間のイテレート。
     * @param all 親スコープも検索する。 
     * @return プレフィックスマッピング（<code>PrefixMapping</code>）のイテレータ。
     */
    Iterator iteratePrefixMapping(boolean all);

}
