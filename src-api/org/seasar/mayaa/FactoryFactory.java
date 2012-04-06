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
package org.seasar.mayaa;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.seasar.mayaa.cycle.scope.ApplicationScope;
import org.seasar.mayaa.source.SourceDescriptor;

/**
 * ファクトリのファクトリオブジェクト。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class FactoryFactory implements Serializable {

    private static final long serialVersionUID = 4985431947480350680L;

    private static FactoryFactory _instance;
    private static Object _context;
    private static Map _factories = new HashMap();

    /**
     * ファクトリの初期化。
     * @param instance ファクトリのインスタンス。
     */
    public static void setInstance(FactoryFactory instance) {
        if (instance == null) {
            throw new IllegalArgumentException();
        }
        _instance = instance;
    }

    /**
     * コンテキストオブジェクト設定。
     * @param context カレントアプリケーションのコンテキストオブジェクト。
     */
    public static void setContext(Object context) {
        if (context == null) {
            throw new IllegalArgumentException();
        }
        _context = context;
    }

    private static void check() {
        if (_instance == null || _context == null) {
            if (_instance == null) {
                throw new IllegalStateException("instance is null");
            }
            if (_context == null) {
                throw new IllegalStateException("context is null");
            }
        }
    }

    /**
     * ファクトリファクトリインスタンスとコンテキストオブジェクトが
     * 初期化済みかを判定する。
     *
     * @return 初期化済みの場合はtrueを返却する。
     */
    public static boolean isInitialized() {
        return _instance != null && _context != null;
    }

    /**
     * ブートストラップ時に用いる、/WEB-INFフォルダを読むソース。
     * @param root ルートパス。
     * @param systemID ソースのSystemID。
     * @return /WEB-INF相対のソース。
     */
    public static SourceDescriptor getBootstrapSource(
            String root, String systemID) {
        check();
        return _instance.getBootstrapSource(root, systemID, _context);
    }

    /**
     * ファクトリを取得する。
     * @param interfaceClass 取得するファクトリのインターフェイス。
     * @return 指定インターフェイスに対応したファクトリ。
     */
    public static UnifiedFactory getFactory(Class interfaceClass) {
        check();
        UnifiedFactory factory = (UnifiedFactory) _factories.get(interfaceClass);
        if (factory == null) {
            factory = _instance.getFactory(interfaceClass, _context);
            if (factory == null) {
                return null;
            }
            _factories.put(interfaceClass, factory);
        }
        return factory;
    }

    /**
     * アプリケーションスコープの取得。
     * @return アプリケーションスコープ。
     */
    public static ApplicationScope getApplicationScope() {
        check();
        return _instance.getApplicationScope(_context);
    }

    /**
     * インスタンスやキャッシュを解放する。
     */
    public static synchronized void release() {
        _instance = null;
        _context = null;
        _factories.clear();
    }

    /**
     * ファクトリを生成する。
     * @param interfaceClass ファクトリのinterfaceのClassオブジェクト
     * @param context コンテキストオブジェクト。
     * @return ファクトリ。
     */
    protected abstract UnifiedFactory getFactory(
            Class interfaceClass, Object context);

    /**
     * ブートストラップ用のソースディスクリプタを取得する。
     * @param root ルートパス。
     * @param systemID システムID。
     * @param context コンテキストオブジェクト。
     * @return ブートストラップ用のソース。
     */
    protected abstract SourceDescriptor getBootstrapSource(
            String root, String systemID, Object context);

    /**
     * アプリケーションスコープの取得。
     * @param context コンテキストオブジェクト。
     * @return アプリケーションスコープ。
     */
    protected abstract ApplicationScope getApplicationScope(Object context);

}
