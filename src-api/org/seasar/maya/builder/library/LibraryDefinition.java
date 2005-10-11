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

import org.seasar.maya.ParameterAware;
import org.seasar.maya.builder.library.converter.PropertyConverter;


/**
 * MLDのlibraryエレメントのモデルオブジェクト。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface LibraryDefinition extends ParameterAware {

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
     * 登録コンバータの取得。
     * @param converterName コンバータ登録名。
     * @return 指定名のコンバータ、もしくはnull。
     */
    PropertyConverter getPropertyConverter(String converterName);
    
    /**
     * 登録コンバータのイテレータ。
     * @return コンバータイテレータ。
     */
    Iterator iteratePropertyConverters();
    
    /**
     * 登録コンバータの取得。
     * @param propertyClass コンバート対象型。
     * @return 指定型に対応したコンバータ、もしくはnull。
     */
    PropertyConverter getPropertyConverter(Class propertyClass);
    
    /**
     * 登録プロパティセットのイテレータ。
     * @return プロパティセットイテレータ。
     */
    Iterator iteratePropertySets();
    
    /**
     * 登録プロパティセットの取得。
     * @param name プロパティセット名。
     * @return 指定名のプロパティセット、もしくはnull。
     */
    PropertySet getPropertySet(String name);
    
    /**
     * 子のprocessorノードのイテレータ。
     * @return ノードイテレータ。
     */
    Iterator iterateProcessorDefinitions();
    
    /**
     * 指定QNameで該当するProcessorDefinitionの取得。
     * @param name 取得したいProcessorDefinitionの名前。
     * @return 該当ProcessorDefinitionもしくはnull。
     */
    ProcessorDefinition getProcessorDefinition(String name);
    
}
