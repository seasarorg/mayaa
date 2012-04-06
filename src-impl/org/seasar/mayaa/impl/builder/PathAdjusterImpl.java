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

import org.seasar.mayaa.builder.PathAdjuster;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.URI;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * "./"で始まるパスをコンテキストパスを含む絶対パスに置換します。
 * 対象とするのは、デフォルトでは以下のタグの属性。(名前空間はHTML/XHTML限定)
 * <ul>
 * <li>&lt;a href="..."&gt;</li>
 * <li>&lt;applet code="..."&gt;</li>
 * <li>&lt;applet codebase="..."&gt;</li>
 * <li>&lt;area href="..."&gt;</li>
 * <li>&lt;base href="..."&gt;</li>
 * <li>&lt;blockquote cite="..."&gt;</li>
 * <li>&lt;del cite="..."&gt;</li>
 * <li>&lt;embed src="..."&gt;</li>
 * <li>&lt;form action="..."&gt;</li>
 * <li>&lt;frame longdesc="..."&gt;</li>
 * <li>&lt;frame src="..."&gt;</li>
 * <li>&lt;iframe src="..."&gt;</li>
 * <li>&lt;img src="..."&gt;</li>
 * <li>&lt;img usemap="..."&gt;</li>
 * <li>&lt;input src="..."&gt;</li>
 * <li>&lt;input usemap="..."&gt;</li>
 * <li>&lt;ins cite="..."&gt;</li>
 * <li>&lt;link href="..."&gt;</li>
 * <li>&lt;object codebase="..."&gt;</li>
 * <li>&lt;object data="..."&gt;</li>
 * <li>&lt;object usemap="..."&gt;</li>
 * <li>&lt;q cite="..."&gt;</li>
 * <li>&lt;script src="..."&gt;</li>
 * </ul>
 * パラメータ
 * <dl>
 * <dt>enabled</dt><dd>trueならばパス置換が有効になります。デフォルトはtrueです。</dd>
 * <dt>force</dt><dd>trueならば"./"で始まっていない相対パスも対象とします。デフォルトはfalseです。</dd>
 * </dl>
 *
 * @author Koji Suga (Gluegent Inc.)
 */
public class PathAdjusterImpl
        extends ParameterAwareImpl
        implements PathAdjuster, CONST_IMPL {

    private static final long serialVersionUID = -6683061623840171581L;

    private String[][] _adjustTarget;

    private boolean _enabled = true;

    /** @since 1.1.13 */
    private boolean _force = false;

    /**
     * デフォルトのコンストラクタ。
     * 処理対象のタグ属性はクラスコメントを参照。
     */
    public PathAdjusterImpl() {
        this(new String[][] {
            { "a", "href" },
            { "img", "src" },
            { "link", "href" },
            { "script", "src" },
            { "form", "action" },
            { "input", "src" },
            { "area", "href" },
            { "iframe", "src" },
            { "frame", "src" },
            { "frame", "longdesc" },
            { "img", "usemap" },
            { "input", "usemap" },
            { "embed", "src" },
            { "applet", "code" },
            { "applet", "codebase" },
            { "object", "data" },
            { "object", "codebase" },
            { "object", "usemap" },
            { "base", "href" },
            { "blockquote", "cite" },
            { "del", "cite" },
            { "ins", "cite" },
            { "q", "cite" }
        });
    }

    /**
     * 処理対象のタグ属性を引数に取るコンストラクタ。
     * 形式は[[タグ名, 属性名], [タグ名, 属性名], ...]のString配列。
     *
     * @param adjustTarget 処理対象のタグ属性
     */
    public PathAdjusterImpl(String[][] adjustTarget) {
        _adjustTarget = adjustTarget;
    }

    public void setParameter(String name, String value) {
        if ("enabled".equals(name)) {
            _enabled = Boolean.valueOf(value).booleanValue();
        } else if ("force".equals(name)) {
            _force = Boolean.valueOf(value).booleanValue();
        }
        super.setParameter(name, value);
    }

    public boolean isTargetNode(QName nodeName) {
        if (_enabled == false) {
            return false;
        }

        URI uri = nodeName.getNamespaceURI();
        if (URI_HTML.equals(uri) || URI_XHTML.equals(uri)) {
            String local = nodeName.getLocalName().toLowerCase();
            for (int i = 0; i < _adjustTarget.length; i++) {
                if (_adjustTarget[i][0].equals(local)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isTargetAttribute(QName nodeName, QName attributeName) {
        if (_enabled == false) {
            return false;
        }

        String nodeLocal = nodeName.getLocalName().toLowerCase();
        String attributeLocal = attributeName.getLocalName().toLowerCase();
        for (int i = 0; i < _adjustTarget.length; i++) {
            if (_adjustTarget[i][0].equals(nodeLocal)
                    && _adjustTarget[i][1].equals(attributeLocal)) {
                return true;
            }
        }
        return false;
    }

    /**
     * "./"で始まるパスを解決して返します。
     * パラメータでforceをtrueにセットされた場合、"./" で始まっていない相対パスも
     * 調整の対象とします。
     *
     * @param base 対象タグのあるページのパス
     * @param path 解決対象のパス
     * @return 解決後のパス
     */
    public String adjustRelativePath(String base, String path) {
        if (path == null) {
            return path;
        }
        String trimed = path.trim();
        if (trimed.length() == 0) {
            return path;
        }
        if (_force) {
            return StringUtil.adjustRelativePath(base, forceRelativePath(trimed));
        }
        return StringUtil.adjustRelativePath(base, trimed);
    }

    /**
     * "./" で始まっていない相対パスの場合、"./" を付けたパスにして返します。
     * "/" で始まるパス、または ":" を含むパスの場合はそのまま返します。
     * "#" で始まるパスもそのまま返します。
     *
     * @param path 調整するパス
     * @return 相対パスならば"./"を先頭に付けたもの、それ以外はpathのまま
     */
    protected String forceRelativePath(String path) {
        if (path.charAt(0) == '/' || path.charAt(0) == '#' ||
                path.indexOf(':') != -1 || path.startsWith("./")) {
            return path;
        }
        return "./" + path;
    }

}
