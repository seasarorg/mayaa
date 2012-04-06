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
package org.seasar.mayaa.impl.engine.processor;

import java.util.LinkedHashMap;
import java.util.Map;

import org.seasar.mayaa.engine.Page;

/**
 * @author Taro Kato (Gluegent, Inc.)
 */
public class InsertRenderingParams {

    private boolean _rendering;
    private LinkedHashMap _params;
    private Page _stackComponent;
    private Page _currentComponent;

    public boolean isRendering() {
        return _rendering;
    }

    public void setRendering(boolean rendering) {
        _rendering = rendering;
    }

    public Map getParams() {
        if (_params == null) {
            _params = new LinkedHashMap();
        }
        return _params;
    }

    public void setStackComponent(Page component) {
        _stackComponent = component;
    }

    public Page getStackComponent() {
        return _stackComponent;
    }

    public void setCurrentComponent(Page component) {
        _currentComponent = component;
    }

    public Page getCurrentComponent() {
        return _currentComponent;
    }

    public void clear() {
        _stackComponent = null;
        _currentComponent = null;
    }

}