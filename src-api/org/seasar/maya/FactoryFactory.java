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
package org.seasar.maya;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.seasar.maya.source.SourceDescriptor;

/**
 * ファクトリのファクトリオブジェクト。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class FactoryFactory implements Serializable {

    private static FactoryFactory _instance; 
    private static Object _context;
    private static Map _factories = new HashMap();
    
    /**
     * ファクトリの初期化。 
     * @param instance ファクトリのインスタンス。
     */
    public static void setInstance(FactoryFactory instance) {
        if(instance == null) {
            throw new IllegalArgumentException();
        }
        _instance = instance;
    }
    
    /**
     * コンテキストオブジェクト設定。
     * @param context カレントアプリケーションのコンテキストオブジェクト。
     */
    public static void setContext(Object context) {
        if(context == null) {
            throw new IllegalArgumentException();
        }
        _context = context;
    }
    
    private static void check() {
        if(_instance == null || _context == null) {
            throw new IllegalStateException();
        }
    }

    /**
     * ブートストラップ時に用いる、/WEB-INFフォルダを読むソース。
     * @param systemID ソースのSystemID。
     * @return /WEB-INF相対のソース。
     */
    public static SourceDescriptor getBootstrapSource(
            String systemID) {
        check();
        return _instance.getBootstrapSource(systemID, _context);
    }
    
    /**
     * ファクトリを取得する。
     * @param 取得するファクトリのインターフェイス。
     * @return 指定インターフェイスに対応したファクトリ。
     */
    public static UnifiedFactory getFactory(Class interfaceClass) {
        check();
        UnifiedFactory factory = (UnifiedFactory)_factories.get(interfaceClass);
        if(factory == null) {
            factory = _instance.getFactory(interfaceClass, _context);
            if(factory == null) {
            	throw new IllegalStateException();
            }
            _factories.put(interfaceClass, factory);
        }
        return factory;
    }
    
    /**
     * ファクトリを生成する。
     * @param context コンテキストオブジェクト。
     * @return ファクトリ。
     */
    protected abstract UnifiedFactory getFactory(
    		Class interfaceClass, Object context);
    
    protected abstract SourceDescriptor getBootstrapSource(
            String systemID, Object context);
    
}
