/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
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
	
	private static final String CONTEXT_KEY = FactoryFactory.class.getName()+".context";
	
    private Map _factories = new HashMap();

    private static void check() {
    	if (isInitialized() == false) {
    		throw new IllegalStateException();
    	}
    }
    
    /**
     * ファクトリファクトリインスタンスとコンテキストオブジェクトが
     * 初期化済みかを判定する。
     * 
     * @return 初期化済みの場合はtrueを返却する。
     */
    public static boolean isInitialized() {
    	MayaaContext context = MayaaContext.getCurrentContext();
    	if (context == null) {
    		return false;
    	}
        if (context.getFactoryFactory() == null
        		|| context.get(CONTEXT_KEY) == null) {
            return false;
        }
    	return true;
    }

    protected static FactoryFactory getInstance() {
    	MayaaContext context = MayaaContext.getCurrentContext();
    	if (context == null) {
    		throw new IllegalStateException();
    	}
    	return context.getFactoryFactory();
    }
    
    protected static Object getContext() {
    	MayaaContext context = MayaaContext.getCurrentContext();
    	if (context == null) {
    		throw new IllegalStateException();
    	}
    	return context.get(CONTEXT_KEY);
    }
    
    public static void setInstance(FactoryFactory factoryFactory) {
    	MayaaContext context = MayaaContext.getCurrentContext();
    	if (context == null) {
    		throw new IllegalStateException();
    	}
    	context.setFactoryFactory(factoryFactory);
    }
    
    public static void setContext(Object context) {
    	MayaaContext mayaaContext = MayaaContext.getCurrentContext();
    	if (mayaaContext == null) {
    		throw new IllegalStateException();
    	}
    	mayaaContext.put(CONTEXT_KEY, context);
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
        return getInstance().getBootstrapSource(root, systemID, getContext());
    }

    /**
     * ファクトリを取得する。
     * @param interfaceClass 取得するファクトリのインターフェイス。
     * @return 指定インターフェイスに対応したファクトリ。
     */
    public static UnifiedFactory getFactory(Class interfaceClass) {
        check();
        UnifiedFactory factory = (UnifiedFactory)getInstance()._factories.get(interfaceClass);
        if (factory == null) {
            factory = getInstance().getFactory(interfaceClass, getContext());
            if (factory == null) {
                return null;
            }
            getInstance()._factories.put(interfaceClass, factory);
        }
        return factory;
    }

    /**
     * アプリケーションスコープの取得。
     * @return アプリケーションスコープ。
     */
    public static ApplicationScope getApplicationScope() {
        check();
        return getInstance().getApplicationScope(getContext());
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
