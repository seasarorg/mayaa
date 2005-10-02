/*
 * Copyright 2004-2005 the Seasar Foundation and the Others.
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
package org.seasar.maya.builder.library;

import java.util.Iterator;

import org.seasar.maya.provider.Parameterizable;


/**
 * MLDのlibraryエレメントのモデルオブジェクト。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface LibraryDefinition extends Parameterizable {

	/**
	 * 例外やログのメッセージ用途として、ファイルのSystemIDを取得する。
	 * @return ファイルSystemID。
	 */
	String getSystemID();
    
    /**
     * 当該ライブラリの名前空間URIを取得する。
     * @return 名前空間URI。
     */
    String getNamespaceURI();
    
    /**
     * 当該ライブラリに追加アサインされた名前空間URIをイテレートする。
     * @return 追加アサインされた名前空間URIのイテレータ。
     */
    Iterator iterateAssignedURI();
    
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
