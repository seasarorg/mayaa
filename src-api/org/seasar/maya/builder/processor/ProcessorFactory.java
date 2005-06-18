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
package org.seasar.maya.builder.processor;

import org.seasar.maya.engine.Template;
import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.engine.specification.SpecificationNode;

/**
 * テンプレート中のHTMLタグの情報から、TemplateProcessorを提供するファクトリ。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface ProcessorFactory {

    /**
     * カスタマイズされたプロセッサ生成を行う際のオプショナルなファクトリAPI。
     * @param template テンプレート。
     * @param injected インジェクトするノード情報。
     * @return 処理を委譲するTemplateProcessor。
     */
    TemplateProcessor createProcessor(Template template, SpecificationNode injected);
    
}
