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
package org.seasar.mayaa.builder.library;

import org.seasar.mayaa.ParameterAware;
import org.seasar.mayaa.engine.processor.TemplateProcessor;
import org.seasar.mayaa.engine.specification.SpecificationNode;

/**
 * MLDのpropertyノードのモデルオブジェクト。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface PropertyDefinition extends ParameterAware {

    /**
     * 所属するプロパティセットの取得。
     * @return プロパティセット。
     */
    PropertySet getPropertySet();

    /**
     * MLDのname属性であるプロパティ名。
     * テンプレートや設定XML上の属性と、テンプレートプロセッサのプロパティを
     * バインディングする名前となる。
     * @return バインディング名。
     */
    String getName();

    /**
     * テンプレートプロセッサ実装のプロパティ名。
     * 設定されていなければname属性の値。
     * @return プロパティ名。
     */
    String getImplName();

    /**
     * MLDにrequired属性で記述された必須フラグ。デフォルトはfalse。
     * @return 必須フラグ。
     */
    boolean isRequired();

    /**
     * MLDにexpectedClass属性で記述された属性型。
     * デフォルトはjava.lang.Object。
     * @return 属性型。
     */
    Class getExpectedClass();

    /**
     * MLDのdefault属性値。カスタマイズで渡すプロパティのデフォルト値。
     * @return カスタマイズデフォルト値。
     */
    String getDefaultValue();

    /**
     * MLDのfinal属性値。MLDに定義した値はユーザーアプリで上書きされない。
     * @return ファイナル値。
     */
    String getFinalValue();

    /**
     * MLD指定のプロパティ値コンバータ名。
     * @return コンバータ名。
     */
    String getPropertyConverterName();

    /**
     * プロパティオブジェクトを生成する。
     * @param processorDef プロセッサ定義。
     * @param processor プロセッサインスタンス。
     * @param original テンプレート上のオリジナルノード。
     * @param injected インジェクションするノード。
     * @return プロパティオブジェクト。
     */
    Object createProcessorProperty(ProcessorDefinition processorDef,
            TemplateProcessor processor,
            SpecificationNode original, SpecificationNode injected);

}
