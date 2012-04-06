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
package org.seasar.mayaa.engine.specification.serialize;

import org.seasar.mayaa.engine.specification.SpecificationNode;


/**
 * @author Taro Kato (Gluegent, Inc.)
 */
public interface NodeReferenceResolver {

    /**
     * デシリアライズ完了の際に、ノード参照が解決したことを
     * 通知してもらうためのリスナを登録する。
     * @param uniqueID 対象ノードのユニーク識別子。systemID＋sequenceID
     * @param listener リスナ
     */
    void registResolveNodeListener(
            String uniqueID, NodeResolveListener listener);

    /**
     * 復元したプロセッサをリゾルバに通知する。
     * @param item 保存するノード
     */
    void nodeLoaded(SpecificationNode item);

}

