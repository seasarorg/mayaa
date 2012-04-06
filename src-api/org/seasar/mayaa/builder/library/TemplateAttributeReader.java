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
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.SpecificationNode;

/**
 * テンプレートの属性値を取得する。
 *
 * @author Koji Suga (Gluegent, Inc.)
 */
public interface TemplateAttributeReader extends ParameterAware {

    /**
     * テンプレートから指定したQNameの属性を読み、その値を返す。
     * 存在しなければnullを返す。
     *
     * @param qName 対象とするカスタムタグのQName
     * @param attributeName 取得する属性のLocalName
     * @param original テンプレートのノード
     * @return qNameに対応する属性値
     */
    String getValue(QName qName, String attributeName, SpecificationNode original);

    /**
     * 無視する属性を追加する。
     *
     * @param qName 対象とするプロセッサタグのQName
     * @param attributeName 対象とする属性のLocalName
     */
    void addIgnoreAttribute(String qName, String attributeName);

    /**
     * プロセッサタグの属性にテンプレート上の異なる名前の属性をセットする指定を
     * 追加する。
     *
     * @param qName 対象とするプロセッサタグのQName
     * @param attributeName 対象とする属性のLocalName
     * @param templateAttributeName テンプレート上の属性名
     */
    void addAliasAttribute(
            String qName, String attributeName, String templateAttributeName);

}
