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

import org.seasar.maya.builder.library.scanner.LibraryScanner;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.provider.Parameterizable;

/**
 * MLDモデルオブジェクトのルート。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface LibraryManager extends Parameterizable {
    
    /**
     * ルートのライブラリスキャナの取得。
     * @return ライブラリスキャナ。
     * @deprecated
     */
    LibraryScanner getLibraryScanner();
    
    /**
     * ライブラリの追加を行う。
     * @param library ライブラリ。
     * @deprecated
     */
    void addLibraryDefinition(LibraryDefinition library);    

    /**
     * ライブラリ定義ソーススキャナの追加。
     * @param scanner ライブラリ定義ソーススキャナ。
     */
    void addLibraryDefinitionSourceScanner(LibraryDefinitionSourceScanner scanner);
    
    /**
     * ライブラリ定義ビルダの追加。
     * @param builder ライブラリ定義ビルダ。
     */
    void addLibraryDefinitionBuilder(LibraryDefinitionBuilder builder);    
    
    /**
     * 全MLD設定（=<code>LibraryDefinition</code>）のイテレータ取得。
     * @return MLD設定のイテレータ。
     */
    Iterator iterateLibraryDefinition();
    
    /**
     * 指定URIで該当するMLD設定のイテレータ取得。
     * @param namespaceURI 取得したいMLDの名前空間URI。
     * @return MLD設定のイテレータ。
     */
    Iterator iterateLibraryDefinition(String namespaceURI);
    
    /**
     * QNameで該当するプロセッサ定義（=<code>ProcessorDefinition</code>）を検索する。
     * 一番はじめに見つかったものを返す。
     * @param qName 取得したいプロセッサ定義の指定QName。
     * @return 指定QNameのプロセッサ定義。見つからない場合はnull。
     */
    ProcessorDefinition getProcessorDefinition(QName qName);
    
}
