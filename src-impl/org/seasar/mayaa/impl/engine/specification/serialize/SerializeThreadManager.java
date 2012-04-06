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
package org.seasar.mayaa.impl.engine.specification.serialize;

import org.seasar.mayaa.impl.engine.specification.SpecificationImpl;

/**
 * @author Taro Kato (Gluegent, Inc.)
 */
public class SerializeThreadManager {

    static SerializeThread[] _serializeThreads = new SerializeThread[10];

    private static volatile boolean _terminated;

    private SerializeThreadManager() {
        throw new UnsupportedOperationException();
    }

    public static boolean serializeReserve(
            SpecificationImpl spec, Object servletContext) {
        int fewIndex = -1;
        int min = Integer.MAX_VALUE;
        synchronized(_serializeThreads) {
            if (_terminated) {
                return false;
            }
            for (int i = 0; i < _serializeThreads.length; i++) {
                if (_serializeThreads[i] == null) {
                    _serializeThreads[i] = new SerializeThread(i, servletContext);
                    if (_serializeThreads[i].add(spec)) {
                        _serializeThreads[i].start();
                        return true;
                    }
                }
                int waitCount = _serializeThreads[i].waitCount();
                if (waitCount < min) {
                    min = waitCount;
                    fewIndex = i;
                }
            }
            return _serializeThreads[fewIndex].add(spec);
        }
    }

    public static void destroy() {
        _terminated = true;
        synchronized (_serializeThreads) {
            for (int i = 0; i < _serializeThreads.length; i++) {
                if (_serializeThreads[i] != null) {
                    _serializeThreads[i].terminate();
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
        synchronized (_serializeThreads) {
            _serializeThreads[index] = null;
        }
    }

    static boolean isReleasedAll() {
        synchronized (_serializeThreads) {
            for (int i = 0; i < _serializeThreads.length; i++) {
                if (_serializeThreads[i] != null) {
                    return false;
                }
            }
            return true;
        }
    }
}

