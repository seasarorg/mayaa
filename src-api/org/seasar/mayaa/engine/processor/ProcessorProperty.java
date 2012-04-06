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

import java.io.Serializable;

import org.seasar.mayaa.cycle.script.CompiledScript;
import org.seasar.mayaa.engine.specification.PrefixAwareName;

/**
 * プロセッサに設定するプロパティのランタイムオブジェクト。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface ProcessorProperty extends Serializable {

    /**
     * プロセッサのプロパティ名。
     * @return プロパティ名。
     */
    PrefixAwareName getName();

    /**
     * プロパティ値のコンパイル済みスクリプトオブジェクトを取得する。
     * @return プロパティ値。
     */
    CompiledScript getValue();

}
