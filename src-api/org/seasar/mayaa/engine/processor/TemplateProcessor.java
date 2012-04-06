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

import org.seasar.mayaa.builder.library.ProcessorDefinition;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.specification.SpecificationNode;

/**
 * テンプレート中のHTMLタグを処理するクラス。
 * リクエストに対してステートレスなように実装しなければならない。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface TemplateProcessor extends ProcessorTreeWalker {

    /**
     * インスタンス生成処理の最後、プロパティをセットした後で呼ばれる。
     */
    void initialize();

    /**
     * テンプレート内でユニークなID値を取得する。
     * @return テンプレート中でユニークなID。
     */
    String getUniqueID();

    /**
     * 開きタグの出力。テンプレートテキストやWhiteSpaceの場合も、
     * このメソッドで出力する。
     * @param topLevelPage 描画トップレベルのページ。
     * @return 子プロセッサを処理する場合にはEVAL_BODY_INCLUDE、
     * 子プロセッサの処理をスキップする場合にはSKIP_BODYを返す。
     */
    ProcessStatus doStartProcess(Page topLevelPage);

    /**
     * 閉じタグの出力。
     * @return ページのこのタグ以降を処理する場合にはEVAL_PAGE、
     * 以降の処理をスキップする場合にはSKIP_PAGE。
     */
    ProcessStatus doEndProcess();

    /**
     * テンプレート上の該当するノード情報を設定する。
     * @param node テンプレートノード。
     */
    void setOriginalNode(SpecificationNode node);

    /**
     * テンプレート上の該当するノード情報の取得。
     * @return テンプレートノード。
     */
    SpecificationNode getOriginalNode();

    /**
     * インジェクションされたノード情報の設定。
     * @param node インジェクトされたノード。
     */
    void setInjectedNode(SpecificationNode node);

    /**
     * インジェクションされたノード情報の取得。
     * @return インジェクトされたノード。
     */
    SpecificationNode getInjectedNode();

    /**
     * このプロセッサの定義への参照を設定する。
     * @param definition プロセッサ定義。
     */
    void setProcessorDefinition(ProcessorDefinition definition);

    /**
     * このプロセッサの定義を取得する。
     * @return プロセッサ定義。
     */
    ProcessorDefinition getProcessorDefinition();

    /**
     * 本プロセッサのインスタンスを破棄対象とし、
     * 本プロセッサが保持していた他のオブジェクトへの参照リンクを切る。
     * @deprecated
     */
    void kill();

    /**
     * ページに対して予め通知を受けるように登録していた場合に、
     * 呼び出される。
     *
     * @param topLevelPage 描画トップレベルのページ。
     */
    void notifyBeginRender(Page topLevelPage);

}
