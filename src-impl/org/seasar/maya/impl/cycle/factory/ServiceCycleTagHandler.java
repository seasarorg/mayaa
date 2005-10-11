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
package org.seasar.maya.impl.cycle.factory;

import org.seasar.maya.ParameterAware;
import org.seasar.maya.cycle.factory.CycleFactory;
import org.seasar.maya.impl.provider.factory.AbstractParameterizableTagHandler;
import org.seasar.maya.impl.util.XMLUtil;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ServiceCycleTagHandler
        extends AbstractParameterizableTagHandler {

    private CycleFactory _cycleFactory;
    
    public ServiceCycleTagHandler() {
        super("serviceCycle");
    }

    protected void start(
    		Attributes attributes, String systemID, int lineNumber) {
        _cycleFactory = (CycleFactory)XMLUtil.getObjectValue(
                attributes, "class", null, CycleFactory.class);
    }

    public CycleFactory getCycleFactory() {
        return _cycleFactory;
    }
    
    public ParameterAware getParameterizable() {
        return getCycleFactory(); 
    }
    
}
