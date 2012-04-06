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

import java.util.Iterator;

import org.seasar.mayaa.engine.processor.TemplateProcessor;
import org.seasar.mayaa.engine.specification.SpecificationNode;

/**
 * MLDのprocessorエレメントのモデルオブジェクト。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface ProcessorDefinition extends PropertySet {

    /**
     * class属性で指定した、TemplateProcessorの実装クラス完全修飾名。
     * @return 実装クラス名。
     */
    Class getProcessorClass();

    /**
     * 登録プロパティセットのイテレータ。
     * @return プロパティセットイテレータ。
     */
    Iterator iteratePropertySets();

    /**
     * 当該設定より、テンプレートプロセッサを生成する。
     * @param original テンプレート上のオリジナルノード。
     * @param injected インジェクションするスペックノード。
     * @return テンプレートプロセッサ。
     */
    TemplateProcessor createTemplateProcessor(
            SpecificationNode original, SpecificationNode injected);

}
