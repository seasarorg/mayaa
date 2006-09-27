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
package org.seasar.mayaa.impl.source;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.seasar.mayaa.MayaaContext;
import org.seasar.mayaa.source.SourceHolder;

/**
 * ソースディスクリプタを保持するオブジェクトを管理する
 * @author Taro Kato (Gluegent, Inc.)
 */
public class SourceHolderFactory {

    private List _sourceHolders = new ArrayList();
    
    private static SourceHolderFactory getInstance() {
    	MayaaContext currentContext = MayaaContext.getCurrentContext();
    	return (SourceHolderFactory) currentContext.getGrowAttribute(
    			SourceHolderFactory.class.getName(),
    			new MayaaContext.Instantiator() {
    		public Object newInstance() {
    			return new SourceHolderFactory();
    		}
    	});
    }

    protected SourceHolderFactory() {
        // for context root
        SourceHolder contentRoot = new WebContextFolderSourceHolder();
        contentRoot.setRoot("/");
        append(contentRoot);
    }

    public void append(SourceHolder sourceHolder) {
        synchronized(_sourceHolders) {
            if (sourceHolder == null) {
                throw new IllegalArgumentException();
            }
            _sourceHolders.add(sourceHolder);
        }
    }

    public static void appendSourceHolder(SourceHolder sourceHolder) {
    	getInstance().append(sourceHolder);
    }

    public static Iterator iterator() {
    	SourceHolderFactory instance = getInstance();
        synchronized(instance._sourceHolders) {
            return new ArrayList(instance._sourceHolders).iterator();
        }
    }

}
