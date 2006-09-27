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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.map.ReferenceMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.impl.FactoryFactoryImpl;
import org.seasar.mayaa.impl.provider.ProviderUtil;

/**
 * スタンドアロンmayaaコンテキスト。
 * アプリケーションコンテキスト毎の管理コンテナとなる。
 * これにより共通ライブラリとしてデプロイができる。
 * Webアプリケーションなどのクラスローダ毎にクラス情報が
 * 生成されないため、メモリ消費を抑えることができる。
 * @author Taro Kato (Gluegent, Inc)
 */
public class MayaaContext {
	private static final Log LOG = LogFactory.getLog(MayaaContext.class);

	private static ThreadLocal _currentContext = new ThreadLocal();
        
	protected static Map/*<appContext, MayaaContext>*/ _contextVariables =
		new ReferenceMap(ReferenceMap.SOFT, ReferenceMap.SOFT);  
	
	private Map _attributes = new HashMap();
	
	private Object _context;
	
	public static void setCurrentContext(MayaaContext context) {
		if (context == null) {
			_currentContext.set(null);
		} else {
			_currentContext.set(context);
		}
	}
	
	public static MayaaContext getCurrentContext() {
		MayaaContext result = (MayaaContext) _currentContext.get();
		if (result == null) {
			synchronized (_currentContext) {
				result = (MayaaContext) _currentContext.get();	// retry
				if (result == null) {
					result = new MayaaContext();
					_currentContext.set(result);
                }
            }
		}
		return result;
	}
	
	public static MayaaContext getContext(Object appContext) {
		return (MayaaContext) _contextVariables.get(appContext);
	}
	
	/**
	 * 匿名アプリケーション用の管理コンテナ生成
	 */
	public MayaaContext() {
		this(null);
	}
	
	/**
	 * 特定アプリケーション用の管理コンテナ生成
	 * @param appContext アプリケーション用コンテキスト
	 */
	public MayaaContext(Object appContext) {
		_context = appContext;
	}
	
	public void init() {
		setFactoryFactory(new FactoryFactoryImpl());
		FactoryFactory.setContext(_context);
        LOG.info("prepareLibraries start");
		ProviderUtil.getLibraryManager().prepareLibraries();
        LOG.info("prepareLibraries end");
		
		if (_context != null) {
			_contextVariables.put(_context, this);
		}
	}
	
	public void destroy() {
		ProviderUtil.getEngine().kill();
	}
	
	public Object getApplicationContext() {
		return _context;
	}
	
	public static interface Instantiator {
		Object newInstance();
	}
	
	public Object getGrowAttribute(String key, Instantiator instantiator) {
		Object result = _attributes.get(key);
		if (result == null) {
			synchronized(_attributes) {
				result = _attributes.get(key);	// retry
				if (result == null) {
					result = instantiator.newInstance();
					_attributes.put(key, result);
				}
			}
		}
		return result;
	}
	
	/**
	 * @return _factoryFactory
	 */
	public FactoryFactory getFactoryFactory() {
		return (FactoryFactory)_attributes.get(FactoryFactory.class.getName());
	}

	/**
	 * @param factory �ݒ肷�� _factoryFactory
	 */
	public void setFactoryFactory(FactoryFactory factory) {
		_attributes.put(FactoryFactory.class.getName(), factory);
	}

	public boolean containsKey(String key) {
		return _attributes.containsKey(key);
	}

	public Object[] getKeys() {
		return _attributes.keySet().toArray();
	}

	public Object get(String key) {
		return _attributes.get(key);
	}
	
	public void put(String key, Object value) {
		_attributes.put(key, value);
	}

	public Object remove(String key) {
		return _attributes.remove(key);
	}

}
