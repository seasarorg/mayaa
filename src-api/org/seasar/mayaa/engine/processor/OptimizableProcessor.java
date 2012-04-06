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
package org.seasar.mayaa.engine.processor;

import org.seasar.mayaa.builder.SequenceIDGenerator;

/**
 * @author Taro Kato (Gluegent, Inc.)
 */
public interface OptimizableProcessor {

    /**
     * 子プロセッサの作成完了時点で呼び出され、
     * 自身を静的パートと動的パートに分割する。
     * 最低でも自身を示す１要素が返却される。
     * @param sequenceIDGenerator 最適化によって新たなノードが必要な際に使用するsequenceIDジェネレータ
     * @return 分割プロセッサー配列
     */
    ProcessorTreeWalker[] divide(SequenceIDGenerator sequenceIDGenerator);

}
