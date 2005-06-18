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
package org.seasar.maya.provider.factory;

import java.io.Serializable;

import javax.servlet.ServletContext;

import org.seasar.maya.provider.ModelProvider;
import org.seasar.maya.provider.ServiceProvider;

/**
 * アプリケーションサービスプロバイダのファクトリクラス
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class ServiceProviderFactory implements Serializable {

	private static ServiceProviderFactory _factory;
	private static ServletContext _servletContext;
	
	/**
	 * ファクトリのカスタマイズ用。 
	 * @param factory カスタマイズしたファクトリ。
	 */
	public static void setDefaultFactory(ServiceProviderFactory factory) {
	    if(factory == null) {
	        throw new IllegalArgumentException();
	    }
		_factory = factory;
	}
	
	/**
	 * サーブレットコンテキストの設定。
	 * @param servletContext カレントのサーブレットコンテキスト。
	 */
	public static void setServletContext(ServletContext servletContext) {
	    if(servletContext == null) {
	        throw new IllegalArgumentException();
	    }
	    _servletContext = servletContext;
	}
	 
	/**
	 * サービスプロバイダの取得。
	 * @return サービスプロバイダ。
	 */
	public static ServiceProvider getServiceProvider() {
	    if(_factory == null || _servletContext == null) {
	        throw new IllegalStateException();
	    }
		return _factory.getServiceProvider(_servletContext);
	}

	/**
	 * モデル供給用のファクトリオブジェクトの取得。
	 * @return モデル供給ファクトリオブジェクト。
	 */
	public static ModelProvider getModelProvider() {
	    if(_factory == null || _servletContext == null) {
	        throw new IllegalStateException();
	    }
		return _factory.getModelProvider(_servletContext);
	}
	
	/**
	 * サービスプロバイダの取得。生成もしくはServletContext中にサーブレット名を
	 * キーとして保存しているプロバイダを取り出す。
	 * @param servletContext サーブレットコンテキスト。
	 * @return エンジンカスタマイズ用コンテキスト。
	 */
	protected abstract ServiceProvider getServiceProvider(ServletContext servletContext);
	
	/**
	 * モデル供給用のファクトリオブジェクトの取得。生成もしくはServletContext中にサーブレット名を
	 * キーとして保存しているプロバイダを取り出す。
	 * @param servletContext サーブレットコンテキスト。
	 * @return モデル供給ファクトリオブジェクト。
	 */
	protected abstract ModelProvider getModelProvider(ServletContext servletContext);
	
}
