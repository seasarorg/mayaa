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
package org.seasar.mayaa.impl.engine.specification.serialize;

import org.seasar.mayaa.MayaaContext;
import org.seasar.mayaa.impl.engine.specification.SpecificationImpl;

/**
 * @author Taro Kato (Gluegent, Inc.)
 */
public class SerializeThreadManager {

    protected SerializeThread[] _serializeThreads = new SerializeThread[10];

    private volatile boolean _terminated;
    
    public static SerializeThreadManager getInstance() {
    	MayaaContext currentContext = MayaaContext.getCurrentContext();
    	if (currentContext == null) {
    		throw new IllegalStateException();
    	}
    	return (SerializeThreadManager)currentContext.getGrowAttribute(
    			SerializeThreadManager.class.getName(),
    			new MayaaContext.Instantiator() {
		    		public Object newInstance() {
		    			return new SerializeThreadManager();
		    		}
    			});
    }
    
    protected SerializeThreadManager() {
        // no-op
    }

    public static boolean serializeReserve(
            SpecificationImpl spec, Object servletContext) {
        int fewIndex = -1;
        int min = Integer.MAX_VALUE;
        SerializeThreadManager instance = getInstance();
        synchronized(instance._serializeThreads) {
            if (instance._terminated) {
                return false;
            }
            for (int i = 0; i < instance._serializeThreads.length; i++) {
                if (instance._serializeThreads[i] == null) {
                	instance._serializeThreads[i] = new SerializeThread(i);
                    if (instance._serializeThreads[i].add(spec)) {
                    	instance._serializeThreads[i].start();
                        return true;
                    }
                }
                int waitCount = instance._serializeThreads[i].waitCount();
                if (waitCount < min) {
                    min = waitCount;
                    fewIndex = i;
                }
            }
            return instance._serializeThreads[fewIndex].add(spec);
        }
    }
    
    public static void destroy() {
    	SerializeThreadManager instance = getInstance();
    	instance._terminated = true;
        synchronized (instance._serializeThreads) {
            for (int i = 0; i < instance._serializeThreads.length; i++) {
                if (instance._serializeThreads[i] != null) {
                	instance._serializeThreads[i].terminate();
                }
            }
        }
        while (!isReleasedAll()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    static void threadDestroy(int index) {
    	SerializeThreadManager instance = getInstance();
        synchronized (instance._serializeThreads) {
        	instance._serializeThreads[index] = null;
        }
    }
    
    static boolean isReleasedAll() {
    	SerializeThreadManager instance = getInstance();
        synchronized (instance._serializeThreads) {
            for (int i = 0; i < instance._serializeThreads.length; i++) {
                if (instance._serializeThreads[i] != null) {
                    return false;
                }
            }
            return true;
        }
    }
}

