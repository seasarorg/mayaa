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
import org.seasar.mayaa.engine.Template;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.engine.specification.SpecificationNode;
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

    private static final String SYSTEM_ID_SUFFIX = ".mayaa/(auto)";

    private String _defaultLayoutPageName;
    private boolean _generateMayaaNode;

    protected void afterBuild(Specification specification) {
        setupExtends((Template) specification);

        super.afterBuild(specification);
    }

    /**
     * TemplateBuilderImplの{@link #afterBuild(Specification)}を実行する前に、
     * 必要ならm:extendsを自動生成します。
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
        SpecificationNode mayaaNode = getMayaaNode(page);

        if (_generateMayaaNode && mayaaNode == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("default layout - generate mayaa: " + page.getPageName());
            }
            mayaaNode = createMayaaNode(page, template);
            if (template.getParentNode() != null) {
                mayaaNode.setParentNode(template.getParentNode());
                template.setParentNode(mayaaNode);
            }
            page.addChildNode(mayaaNode);
        }
        if (mayaaNode != null) {
            addExtends(page, mayaaNode);
        }
    }

    /**
     * PageのMayaaノードを取得します。
     * createMayaaNodeで自動生成したノードだった場合、そのノードは削除して
     * 再度Mayaaノードを取得します。
     *
     * @param page ビルド対象のページ
     * @return 自動生成でないMayaaノード。無ければnullを返します。
     */
    protected SpecificationNode getMayaaNode(Page page) {
        SpecificationNode mayaaNode = SpecificationUtil.getMayaaNode(page);
        if (mayaaNode != null && mayaaNode.getSystemID().endsWith(SYSTEM_ID_SUFFIX)) {
            page.removeChildNode(mayaaNode);
            mayaaNode = SpecificationUtil.getMayaaNode(page);
        }
        return mayaaNode;
    }

    /**
     * ページ名に対応したMayaaファイルの代わりを作成します。
     *
     * @param page Mayaaノードを作成するページ
     * @param specification sequenceIDを取得するためのspec
     * @return Mayaaノード
     */
    protected SpecificationNode createMayaaNode(
            Page page, Specification specification) {
        String systemID = page.getPageName() + SYSTEM_ID_SUFFIX;

        return SpecificationUtil.createSpecificationNode(
                    QM_MAYAA, systemID,
                    0, false, specification.nextSequenceID());
    }

    /**
     * m:mayaa要素にm:extends属性を追加します。
     *
     * @param page 処理中のページ
     * @param mayaaNode 属性を追加するm:mayaa要素
     */
    protected void addExtends(Page page, SpecificationNode mayaaNode) {
        String extendsValue = SpecificationUtil.getAttributeValue(
                mayaaNode, QM_EXTENDS);
        if (StringUtil.isEmpty(extendsValue)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("default layout - set extends: " + page.getPageName()
                        + " m:extends=\"" + _defaultLayoutPageName + "\"");
            }
            mayaaNode.addAttribute(QM_EXTENDS, _defaultLayoutPageName);
        }
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
            return false;
        }

        SourceDescriptor source = SourceUtil.getSourceDescriptor(pageName);
        if (source == null || source.exists() == false) {
            LOG.warn(StringUtil.getMessage(
                    DefaultLayoutTemplateBuilder.class, 2, pageName));
            return false;
        }

        return true;
    }

}
