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
package org.seasar.maya.engine;

import java.io.Serializable;

import org.seasar.maya.cycle.script.CompiledScript;
import org.seasar.maya.engine.processor.TemplateProcessor.ProcessStatus;
import org.seasar.maya.engine.specification.Specification;

/**
 * テンプレートのレンダリングエントリーポイント。アプリケーションを構成する
 * 各ページのモデルであり、リクエストに対してはステートレスなオブジェクトである。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface Page extends Specification, Serializable {

    /**
     * 継承元ページの取得。
     * @return 継承元ページもしくはnull。
     */
    Page getSuperPage();

    /**
     * 継承元テンプレート接尾辞の取得。
     * @return テンプレート接尾辞。
     */
    String getSuperSuffix();
    
    /**
     * 継承元テンプレート拡張子の取得。
     * @return テンプレート拡張子。
     */
    String getSuperExtension();
    
    /**
     * ページの名前を取得する。/context/hello.htmlであれば、
     * 「/context/hello」を返す。
     * @return ページ名。
     */
    String getPageName();

    /**
     * テンプレート接尾辞を決定するコンパイル済みスクリプト。
     * @return コンパイル済みスクリプト。
     */
    CompiledScript getSuffixScript();
    
    /**
     * テンプレート接尾辞より適切なTemplateオブジェクトをロードして返す。
     * @return レンダリングするテンプレート。
     */
    Template getTemplate(String suffix, String extension);
    
    /**
     * テンプレートレンダリングを行う。
     * @param requestedSuffix リクエストされたテンプレート接尾辞。
     * @param extension テンプレート拡張子。
     * @return プロセッサ処理ステータス。
     */
    ProcessStatus doPageRender(String requestedSuffix, String extension);
    
}
