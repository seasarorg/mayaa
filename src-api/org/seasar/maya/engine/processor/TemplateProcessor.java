/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
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

import org.seasar.maya.engine.specification.SpecificationNode;

/**
 * テンプレート中のHTMLタグを処理する委譲クラス。このTemplateProcessorのツリーすなわち
 * Templateとなる。このTemplateProcessorもリクエストに対してステートレスでなければならない。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface TemplateProcessor extends ProcessorTreeWalker {

    /**
     * 開きタグの出力。テンプレートテキストやWhiteSpaceの場合も、このメソッドで出力する。
     * @return 子プロセッサを処理する場合にはEVAL_BODY_INCLUDE、
     * 子プロセッサの処理をスキップする場合にはSKIP_BODYを返す。
     */
    ProcessStatus doStartProcess();

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
     * プロセッサ動作にて状態遷移を示すステータス。
     */
    public class ProcessStatus implements Serializable {

        private static final long serialVersionUID = 473586899180314059L;

        protected ProcessStatus() {
        }
        
    }

    /**
     * リターンフラグ。doStartProcess()がこの値を返すと、プロセッサボディを出力しない。
     */
    public static final ProcessStatus SKIP_BODY = new ProcessStatus();
    
    /**
     * リターンフラグ。doStartProcess()がこの値を返すと、
     * プロセッサボディをバッファリング無しで出力する。
     */
    public static final ProcessStatus EVAL_BODY_INCLUDE = new ProcessStatus();
    
    /**
     * リターンフラグ。doEndProcess()がこの値を返すと、以降の出力をただちに中止する。
     */
    public static final ProcessStatus SKIP_PAGE = new ProcessStatus();
    
    /**
     * リターンフラグ。doEndProcess()がこの値を返すと、以降のプロセッサ出力を続ける。
     */
    public static final ProcessStatus EVAL_PAGE = new ProcessStatus();

}