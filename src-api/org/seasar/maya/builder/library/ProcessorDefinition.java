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
package org.seasar.maya.builder.library;

import java.util.Iterator;

import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.engine.specification.SpecificationNode;

/**
 * MLDのprocessorエレメントのモデルオブジェクト。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface ProcessorDefinition {
    
    /**
     * 所属ライブラリの情報モデル取得。
     * @return ライブラリ情報。
     */
    LibraryDefinition getLibraryDefinition();
    
    /**
     * プロセッサ名の取得。
     * @return プロセッサ名。
     */
    String getName();
    
    /**
     * class属性で指定した、TemplateProcessorの実装クラス完全修飾名。
     * @return 実装クラス名。
     */
    Class getProcessorClass();
    
    /**
     * プロセッサへのバインディング情報モデル（PropertyDefinition）オブジェクトのイテレート。
     * @return バインディング情報イテレーター。
     */
    Iterator iteratePropertyDefinition();        
    
    /**
     * 当該設定より、テンプレートプロセッサを生成する。 
     * @param injected インジェクションするスペックノード。
     * @return テンプレートプロセッサ。
     */
    TemplateProcessor createTemplateProcessor(SpecificationNode injected);
    
}
