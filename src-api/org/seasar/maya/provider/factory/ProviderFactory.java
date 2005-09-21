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
package org.seasar.maya.provider.factory;

import java.io.Serializable;

import org.seasar.maya.provider.ServiceProvider;

/**
 * アプリケーションサービスプロバイダのファクトリクラス
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class ProviderFactory implements Serializable {

	private static ProviderFactory _factory;
	private static Object _context;
	
	/**
	 * ファクトリのカスタマイズ用。 
	 * @param factory カスタマイズしたファクトリ。
	 */
	public static void setDefaultFactory(ProviderFactory factory) {
	    if(factory == null) {
	        throw new IllegalArgumentException();
	    }
		_factory = factory;
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
	 
	/**
	 * サービスプロバイダの取得。
	 * @return サービスプロバイダ。
	 */
	public static ServiceProvider getServiceProvider() {
	    if(_factory == null || _context == null) {
	        throw new IllegalStateException();
	    }
		return _factory.getServiceProvider(_context);
	}
	
	/**
	 * サービスプロバイダの取得。生成もしくはキャッシュしているプロバイダを取り出す。
	 * @param context コンテキストオブジェクト。
	 * @return サービスプロバイダ。
	 */
	protected abstract ServiceProvider getServiceProvider(Object context);
	
}
