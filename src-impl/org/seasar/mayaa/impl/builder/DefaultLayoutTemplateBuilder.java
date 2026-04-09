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
package org.seasar.mayaa.impl.builder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.impl.management.DiagnosticEventBuffer;
import org.seasar.mayaa.engine.Template;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.impl.engine.TemplateImpl;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.source.SourceUtil;
import org.seasar.mayaa.impl.util.ObjectUtil;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.source.SourceDescriptor;

/**
 * 対応するMayaaファイルにm:extends属性が存在しない場合、デフォルトのレイアウト
 * 設定をします。
 * ページ名をパラメータ "defaultLayoutPageName" で "/foo/bar.html" の形式で
 * 指定します。
 * また、パラメータ "generateMayaaNode" に true を指定すると、Mayaaファイルの
 * 無いテンプレートでもレイアウトが適用されるようにします。
 *
 * @author Koji Suga (Gluegent Inc.)
 */
public class DefaultLayoutTemplateBuilder extends TemplateBuilderImpl {

    private static final Log LOG = LogFactory.getLog(DefaultLayoutTemplateBuilder.class);

    private static final long serialVersionUID = 6472968108941224353L;

    private String _defaultLayoutPageName;
    private boolean _generateMayaaNode;

    protected void afterBuild(Specification specification) {
        setupExtends((Template) specification);

        super.afterBuild(specification);
    }

    /**
     * TemplateBuilderImplの{@link #afterBuild(Specification)}を実行する前に、
     * 必要ならデフォルトレイアウトの適用設定をします。
     * <p>
     * キャッシュ済みの Page インスタンスはイミュータブルとして扱い、直接ミューテートは行いません。
     * デフォルトレイアウトのページ名は Template 側（{@link TemplateImpl#setDynamicSuperPagePath}）
     * に保持し、描画時に {@code RenderUtil} が参照します。
     *
     * @param template 処理対象のテンプレート
     */
    protected void setupExtends(Template template) {
        if (StringUtil.isEmpty(_defaultLayoutPageName)) {
            return;
        }
        if (_defaultLayoutPageName.equals(template.getSystemID())) {
            return;
        }

        Page page = template.getPage();
        SpecificationNode mayaaNode = SpecificationUtil.getMayaaNode(page);

        boolean shouldApplyDefault;
        if (mayaaNode == null) {
            // .mayaaファイルなし — generateMayaaNode=true の場合のみデフォルトレイアウトを適用
            shouldApplyDefault = _generateMayaaNode;
        } else {
            // .mayaaファイルあり — m:extends が未定義の場合にデフォルトレイアウトを適用
            shouldApplyDefault = StringUtil.isEmpty(
                    SpecificationUtil.getAttributeValue(mayaaNode, QM_EXTENDS));
        }

        if (shouldApplyDefault && template instanceof TemplateImpl) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("default layout - set extends: " + page.getPageName()
                        + " m:extends=\"" + _defaultLayoutPageName + "\"");
            }
            ((TemplateImpl) template).setDynamicSuperPagePath(_defaultLayoutPageName);
        }
    }

    /**
     * PageのMayaaノードを取得します（.mayaaファイルに定義されたノードのみ返します）。
     *
     * @param page ビルド対象のページ
     * @return .mayaaファイルに定義されたMayaaノード。無ければnullを返します。
     */
    protected SpecificationNode getMayaaNode(Page page) {
        return SpecificationUtil.getMayaaNode(page);
    }

    /**
     * ページ名に対応したMayaaファイルの代わりを作成します。
     *
     * @deprecated Mayaa 2.0 で廃止。Page をミューテートする方式は廃止されました。
     *             デフォルトレイアウトは {@link TemplateImpl#setDynamicSuperPagePath} で
     *             Template 側に設定してください。
     *             詳細は UPGRADING.md の「setupExtends のシグネチャ変更」を参照してください。
     * @throws UnsupportedOperationException 常にスローされます
     */
    @Deprecated
    protected SpecificationNode createMayaaNode(
            Page page, Specification specification) {
        throw new UnsupportedOperationException(
            "DefaultLayoutTemplateBuilder.createMayaaNode() is removed in Mayaa 2.0. "
            + "Override setupExtends() and call TemplateImpl.setDynamicSuperPagePath() instead. "
            + "See UPGRADING.md for migration details.");
    }

    /**
     * m:mayaa要素にm:extends属性を追加します。
     *
     * @deprecated Mayaa 2.0 で廃止。Page の属性を直接ミューテートする方式は廃止されました。
     *             デフォルトレイアウトは {@link TemplateImpl#setDynamicSuperPagePath} で
     *             Template 側に設定してください。
     *             詳細は UPGRADING.md の「setupExtends のシグネチャ変更」を参照してください。
     * @throws UnsupportedOperationException 常にスローされます
     */
    @Deprecated
    protected void addExtends(Page page, SpecificationNode mayaaNode) {
        throw new UnsupportedOperationException(
            "DefaultLayoutTemplateBuilder.addExtends() is removed in Mayaa 2.0. "
            + "Override setupExtends() and call TemplateImpl.setDynamicSuperPagePath() instead. "
            + "See UPGRADING.md for migration details.");
    }

    // Parameterizable implements ------------------------------------

    public void setParameter(String name, String value) {
        if ("generateMayaaNode".equals(name)) {
            setGenerateMayaaNode(ObjectUtil.booleanValue(value, false));
        } else if ("defaultLayoutPageName".equals(name)) {
            setDefaultLayoutPageName(value);
        }
        super.setParameter(name, value);
    }

    /**
     * @return the applyAllTemplates
     */
    public boolean isGenerateMayaaNode() {
        return _generateMayaaNode;
    }

    /**
     * @param applyAllTemplates the applyAllTemplates to set
     */
    public void setGenerateMayaaNode(boolean applyAllTemplates) {
        _generateMayaaNode = applyAllTemplates;
    }

    /**
     * @return the defaultLayoutPageName
     */
    public String getDefaultLayoutPageName() {
        return _defaultLayoutPageName;
    }

    /**
     * @param defaultLayoutPageName the defaultLayoutPageName to set
     */
    public void setDefaultLayoutPageName(String defaultLayoutPageName) {
        if (validatePageName(defaultLayoutPageName)) {
            _defaultLayoutPageName = defaultLayoutPageName;
        }
    }

    /**
     * デフォルトレイアウトとするページ名が正しいことを検証します。
     * <ul>
     * <li>"/"始まりであること</li>
     * <li>"/../"などを含まないこと</li>
     * <li>ファイルが存在すること</li>
     * </ul>
     *
     * @param pageName 検証するページ名
     * @return 正しければtrue
     */
    protected boolean validatePageName(String pageName) {
        if (StringUtil.isEmpty(pageName)) {
            return false;
        }

        if (pageName.charAt(0) != '/' || pageName.indexOf("/../") != -1) {
            LOG.warn(StringUtil.getMessage(
                    DefaultLayoutTemplateBuilder.class, 1, pageName));
            DiagnosticEventBuffer.recordWarn(
                    DiagnosticEventBuffer.Phase.BUILD,
                    "invalidPageName",
                    DefaultLayoutTemplateBuilder.class.getName(),
                    StringUtil.getMessage(DefaultLayoutTemplateBuilder.class, 1, pageName),
                    pageName);
            return false;
        }

        SourceDescriptor source = SourceUtil.getSourceDescriptor(pageName);
        if (source == null || source.exists() == false) {
            LOG.warn(StringUtil.getMessage(
                    DefaultLayoutTemplateBuilder.class, 2, pageName));
            DiagnosticEventBuffer.recordWarn(
                    DiagnosticEventBuffer.Phase.BUILD,
                    "invalidPageName",
                    DefaultLayoutTemplateBuilder.class.getName(),
                    StringUtil.getMessage(DefaultLayoutTemplateBuilder.class, 2, pageName),
                    pageName);
            return false;
        }

        return true;
    }

}
