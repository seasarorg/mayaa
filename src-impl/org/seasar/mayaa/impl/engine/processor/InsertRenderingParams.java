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
package org.seasar.mayaa.impl.engine.processor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Taro Kato (Gluegent, Inc.)
 */
public class InsertRenderingParams {

    private ThreadLocal _rendering;
    private ThreadLocal _params;

    private ThreadLocal renderingInstance() {
        if (_rendering == null) {
            synchronized(this) {
                if (_rendering == null) {
                    _rendering = new ThreadLocal();
                }
            }
        }
        return _rendering;
    }

    public boolean isRendering() {
        Boolean result = (Boolean) renderingInstance().get();
        if (result == null) {
            result = Boolean.FALSE;
            renderingInstance().set(result);
        }
        return result.booleanValue();
    }

    public void setRendering(boolean rendering) {
        renderingInstance().set(Boolean.valueOf(rendering));
    }

    public Map getParams() {
        if (_params == null) {
            synchronized (this) {
                if (_params == null) {
                    _params = new ThreadLocal();
                }
            }
        }
        Map result = (Map) _params.get();
        if (result == null) {
            result = new LinkedHashMap();
            _params.set(result);
        }
        return result;
    }

}