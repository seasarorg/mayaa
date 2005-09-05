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


/**
 * MLDのlibraryエレメントのモデルオブジェクト。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface LibraryDefinition {
    
    /**
     * 当該ライブラリの名前空間URIをイテレートする。
     * @return 名前空間URIのイテレータ。
     */
    Iterator iterateNamespaceURI();
    
    /**
     * 子のprocessorノードのイテレータ。
     * @return ノードイテレータ。
     */
    Iterator iterateProcessorDefinition();
    
    /**
     * 指定QNameで該当するProcessorDefinitionの取得。
     * @param localName 取得したいProcessorDefinitionの名前。
     * @return 該当ProcessorDefinitionもしくはnull。
     */
    ProcessorDefinition getProcessorDefinition(String localName);
    
}
