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

import org.seasar.maya.cycle.factory.CycleFactory;
import org.seasar.maya.provider.factory.ProviderFactory;
import org.seasar.maya.source.factory.SourceFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class FactoryFactory 
		implements ParameterAware, Serializable {

    private static FactoryFactory _instance; 
    private static Object _context;
    private static ProviderFactory _providerFactory;
    private static CycleFactory _cycleFactory;
    private static SourceFactory _sourceFactory;
    
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
     * サービスプロバイダのファクトリを取得する。
     * @return サービスプロバイダファクトリ。
     */
    public static ProviderFactory getProviderFactory() {
        check();
        if(_providerFactory == null) {
            _providerFactory = _instance.createProviderFactory(_context);
        }
        return _providerFactory;
    }
    
    /**
     * サービスサイクルのファクトリを取得する。
     * @return サービスサイクルファクトリ。
     */
    public static CycleFactory getCycleFactory() {
        check();
        if(_cycleFactory == null) {
            _cycleFactory = _instance.createCycleFactory(_context);
        }
        return _cycleFactory;
    }
    
    /**
     * ソース定義のファクトリを取得する。
     * @return ソース定義ファクトリ。
     */
    public static SourceFactory getSourceFactory() {
        check();
        if(_sourceFactory == null) {
            _sourceFactory = _instance.createSourceFactory(_context);
        }
        return _sourceFactory;
    }
    
    /**
     * パラメータを設定する。
     * @param name パラメータ名。
     * @param value パラメータ値。
     */
    public static void setParameterToInstance(String name, String value) {
    	check();
    	_instance.setParameter(name, value);
    }
    
    /**
     * サービスプロバイダのファクトリを生成する。
     * @param context コンテキストオブジェクト。
     * @return サービスプロバイダのファクトリ。
     */
    protected abstract ProviderFactory createProviderFactory(Object context);
    
    /**
     * サービスサイクルのファクトリを生成する。
     * @param context コンテキストオブジェクト。
     * @return サービスサイクルのファクトリ。
     */
    protected abstract CycleFactory createCycleFactory(Object context);
    
    /**
     * ソース定義のファクトリを生成する。
     * @param context コンテキストオブジェクト。
     * @return ソース定義のファクトリ。
     */
    protected abstract SourceFactory createSourceFactory(Object context);
    
}
