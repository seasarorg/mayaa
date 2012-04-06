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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.cycle.web.MockHttpServletRequest;
import org.seasar.mayaa.impl.cycle.web.MockHttpServletResponse;
import org.seasar.mayaa.impl.engine.specification.SpecificationImpl;

/**
 * @author Taro Kato (Gluegent, Inc.)
 */
public class SerializeThread extends Thread {
    private static final int RECYCLE_LIVE_COUNT = 600;   // x 100 msec => 1 min
    private int _index;
    private volatile int _liveCount;
    private List _queue = new ArrayList();
    private Object _requestContext;
    private Object _responseContext;
    private boolean _terminated;

    SerializeThread(int index, Object servletContext) {
        setName("serializeThread-" + index);
        _index = index;
        _liveCount = RECYCLE_LIVE_COUNT;
        // TODO contextPathをセット
        _requestContext = new MockHttpServletRequest(
                (ServletContext) servletContext, "/");
        _responseContext = new MockHttpServletResponse();
    }

    public int waitCount() {
       return _queue.size();
    }

    public boolean add(SpecificationImpl specification) {
        if (_liveCount > 0 && _terminated == false) {
            synchronized(_queue) {
                _queue.add(specification);
                return true;
            }
        }
        return false;
    }

    public void run() {
        CycleUtil.initialize(_requestContext, _responseContext);
        SpecificationImpl specification;
        try {
            while (_liveCount > 0 && _terminated == false) {
                Thread.sleep(100);
                specification = null;
                synchronized(_queue) {
                    if (_queue.size() > 0) {
                        specification =
                            (SpecificationImpl) _queue.remove(0);
                    }
                }
                if (specification != null) {
                    _liveCount = RECYCLE_LIVE_COUNT;    // enlargement
                    synchronized(specification) {
                        try {
                            specification.serialize();
                        } finally {
                            specification = null;
                        }
                    }
                }
                if (--_liveCount <= 0) {
                    break;
                }
            }
        } catch(InterruptedException e) {
            // no-op
        } finally {
            CycleUtil.cycleFinalize();
            SerializeThreadManager.threadDestroy(_index);
        }
    }

    public void terminate() {
        _terminated = true;
    }

}

