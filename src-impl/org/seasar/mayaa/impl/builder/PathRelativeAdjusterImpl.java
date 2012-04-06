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

import org.seasar.mayaa.impl.util.StringUtil;

/**
 * "./"で始まるパスをレンダリング時のパスからの相対パスになるようスクリプトに置換します。
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
 * ※このスクリプトはJavaScriptであるため、スクリプトエンジンを置き換えた場合は動作しません。
 *
 * @author Koji Suga (Gluegent Inc.)
 */
public class PathRelativeAdjusterImpl extends PathAdjusterImpl {

    private static final long serialVersionUID = -1408738254857321372L;

    /**
     * @see PathAdjusterImpl#PathAdjusterImpl()
     */
    public PathRelativeAdjusterImpl() {
        super();
    }

    /**
     * @see PathAdjusterImpl#PathAdjusterImpl(String[][])
     */
    public PathRelativeAdjusterImpl(String[][] adjustTarget) {
        super(adjustTarget);
    }

    /**
     * "./"で始まるパスを解決して返します。
     * 解決した結果はJavaScriptになり、レンダリング時に実行されます。
     *
     * @param base 対象タグのあるページのパス
     * @param path 解決対象のパス
     * @return 解決後のパス
     */
    public String adjustRelativePath(String base, String path) {
        if (StringUtil.isRelativePath(path) == false) {
            return path;
        }
        // リンク先をリクエストされたパスからの相対にする
        String contextRelativePath = StringUtil.adjustRelativePath(base, path);
        StringBuffer sb = new StringBuffer(200);
        sb.append("${Packages.org.seasar.mayaa.impl.util.StringUtil.adjustContextRelativePath(");
        sb.append("request.getContextPath() + request.getRequestedPath()");
        sb.append(", '");
        sb.append(contextRelativePath);
        sb.append("')}");
        return sb.toString();
    }

}
