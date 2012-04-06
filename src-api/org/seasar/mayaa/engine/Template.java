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
package org.seasar.mayaa.engine;

import java.io.Serializable;

import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.engine.processor.ProcessorTreeWalker;
import org.seasar.mayaa.engine.specification.Specification;

/**
 * テンプレートオブジェクト。HTMLをパースした結果の、
 * TemplateProcessorのツリー構造を内包。
 * リクエストに対して、ステートレスである。 シリアライズ可能。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface Template
        extends Specification, ProcessorTreeWalker, Serializable {

    /**
     * テンプレートを初期化する。
     * @param page ページ。
     * @param suffix テンプレートの接尾子。
     * @param extension ページの拡張子。
     */
    void initialize(Page page, String suffix, String extension);

    /**
     * ページへの参照を取得する。
     * @return ページ。
     */
    Page getPage();

    /**
     * テンプレートの接尾子を返す。hello_ja.htmlであれば、「ja」を返す。
     * hello.htmlでは空白文字列。
     * @return テンプレートの接尾子。
     */
    String getSuffix();

    /**
     * ページの拡張子を返す。/context/hello.htmlだと、「html」。ドットを含まない。
     * @return ページの拡張子。
     */
    String getExtension();

    /**
     * テンプレートをレンダリングする。
     * @param topLevelPage  描画トップレベルのページ。
     * @return テンプレートプロセッサのプロセスステートフラグ。
     */
    ProcessStatus doTemplateRender(Page topLevelPage);

}
