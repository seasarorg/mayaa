/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
 *
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which
 * accompanies this distribution, and is available at
 *
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.seasar.maya.engine.processor;

import java.io.Serializable;

import javax.servlet.jsp.PageContext;

/**
 * テンプレート中のHTMLタグを処理する委譲クラス。このTemplateProcessorのツリーすなわち
 * Templateとなる。このTemplateProcessorもリクエストに対してステートレスでなければならない。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface TemplateProcessor extends Serializable {

    /**
     * ノードの初期化を行う。このメソッドは、TemplateBuilder#buildの中で呼ばれる。
     * @param parent 親TemplateProcessor
     * @param index 親TemplateNode内での子としてのインデックス値。
     */
    void setParentProcessor(TemplateProcessor parent, int index);

    /**
     * 子TemplateProcessorを追加する。このメソッドは、TemplateBuilder#buildの中で呼ばれる。
     * @param child 子TemplateProcessor
     */
    void addChildProcessor(TemplateProcessor child);

    /**
     * 親TemplateProcessor内での子としてのインデックス値
     * @return インデックス値
     */
    int getIndex();

    /**
     * 親TemplateProcessorを取得する。
     * @return 親TemplateProcessor
     */
    TemplateProcessor getParentProcessor();

    /**
     * 子TemplateNodeの数を取得する。
     * @return 子TemplateNodeの数
     */
    int getChildProcessorSize();

    /**
     * 指定インデックスの子TemplateNodeを取得する。
     * @param index 指定index。
     * @return 指定indexの子TemplateProcessor。
     */
    TemplateProcessor getChildProcessor(int index);

    /**
     * 開きタグの出力。テンプレートテキストやWhiteSpaceの場合も、このメソッドで出力する。
     * @param context プロセス中のステートフルな情報を保持するコンテキスト。
     * @return javax.servlet.jsp.tagext.Tag#doStartTag()の返値と同じ仕様。
     */
    int doStartProcess(PageContext context);

    /**
     * 閉じタグの出力。
     * @param context プロセス中のステートフルな情報を保持するコンテキスト。
     * @return javax.servlet.jsp.tagext.Tag#doEndTag()の返値と同じ仕様。
     */
    int doEndProcess(PageContext context);

}