/*
 * Copyright 2004-2026 the Seasar Foundation and the Others.
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
package org.seasar.mayaa.functional.layout.extends_from_template;

import java.util.Iterator;

import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.Template;
import org.seasar.mayaa.engine.specification.NodeTreeWalker;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.impl.builder.DefaultLayoutTemplateBuilder;
import org.seasar.mayaa.impl.engine.TemplateImpl;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * HTML テンプレートのノードに書かれた {@code m:extends} 属性を読み取って
 * ページの mayaaNode に設定するカスタムテンプレートビルダー。
 *
 * <p>{@link DefaultLayoutTemplateBuilder#setupExtends} をオーバーライドし、
 * テンプレートの第1階層ノードを走査して {@code m:extends} 属性が見つかった場合に
 * mayaaNode へ転写します。HTML に {@code m:extends} がない場合は
 * スーパークラスの動作（{@code defaultLayoutPageName}）にフォールバックします。</p>
 *
 * <p>HTML ファイルに以下のように書くことで対象レイアウトを指定します：</p>
 * <pre>{@code
 * <html xmlns:m="http://mayaa.seasar.org" m:extends="/layout.html">
 * }</pre>
 */
public class HtmlExtendsTemplateBuilder extends DefaultLayoutTemplateBuilder {

    private static final long serialVersionUID = 1L;

    @Override
    protected void setupExtends(Template template) {
        // Page の .mayaa に m:extends が既に定義されている場合は何もしない（Page 定義優先）
        Page page = template.getPage();
        SpecificationNode mayaaNode = getMayaaNode(page);
        if (mayaaNode != null && !StringUtil.isEmpty(
                SpecificationUtil.getAttributeValue(mayaaNode, QM_EXTENDS))) {
            return;
        }

        String extendsValue = findExtendsFromHtml(template);
        if (!StringUtil.isEmpty(extendsValue)) {
            // HTML から読み取った m:extends を Template 側に保持する（Page はミューテートしない）
            if (template instanceof TemplateImpl) {
                ((TemplateImpl) template).setDynamicSuperPagePath(extendsValue);
            }
        } else {
            // HTML に m:extends がなければデフォルト動作（defaultLayoutPageName を使う）
            super.setupExtends(template);
        }
    }

    /**
     * テンプレートの第1階層ノードを走査して {@code m:extends}（Mayaa 名前空間）
     * 属性の値を返します。
     *
     * @param template 検索対象のテンプレート
     * @return 属性値。見つからなければ {@code null}
     */
    private String findExtendsFromHtml(Template template) {
        for (Iterator<NodeTreeWalker> itr = template.iterateChildNode(); itr.hasNext(); ) {
            SpecificationNode node = (SpecificationNode) itr.next();
            String value = SpecificationUtil.getAttributeValue(node, QM_EXTENDS);
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
