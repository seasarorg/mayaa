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
package org.seasar.mayaa.impl.management;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.seasar.mayaa.FactoryFactory;
import org.seasar.mayaa.cycle.scope.ApplicationScope;
import org.seasar.mayaa.engine.Engine;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.Version;
import org.seasar.mayaa.impl.engine.EngineImpl;
import org.seasar.mayaa.impl.util.ObjectUtil;
import org.seasar.mayaa.management.MayaaEngineMXBean;

public class EngineRegistory {
    
    public static void registerEngine(final Engine engine) {
        MayaaEngineMXBean mbean = new MayaaEngineMXBean(){
            @Override
            public String getVersion() {
                return Version.MAYAA_VERSION;
            }

            @Override
            public boolean isDebugEnabled() {
                ApplicationScope scope = FactoryFactory.getApplicationScope();
                if (scope != null) {
                    return ObjectUtil.booleanValue(scope.getAttribute(CONST_IMPL.DEBUG), false);
                }
                return false;
            }

            @Override
            public void setDebugEnabled(boolean debugEnabled) {
                ApplicationScope scope = FactoryFactory.getApplicationScope();
                if (scope != null) {
                    scope.setAttribute(CONST_IMPL.DEBUG, Boolean.valueOf(debugEnabled));
                }
            }

            @Override
            public boolean isDumpEnabled() {
                if (engine instanceof EngineImpl) {
                    String debug = ((EngineImpl) engine).getParameter(EngineImpl.DUMP_ENABLED);
                    if (debug != null && "true".equals(debug)) {
                        return true;
                    }
                    return false;
                } else {
                    throw new UnsupportedOperationException("Dump mode is not supported");
                }
            }

            @Override
            public void setDumpEnabled(boolean dumpEnabled) {
                if (engine instanceof EngineImpl) {
                    String value = dumpEnabled ? "true": "false";
                    ((EngineImpl) engine).setParameter(EngineImpl.DUMP_ENABLED, value);
                } else {
                    throw new UnsupportedOperationException("Dump mode is not supported");
                }
            }
        };
        JMXUtil.register(mbean, makeObjectName());
    }

    protected static ObjectName makeObjectName() {
        try {
            String name = String.format(MayaaEngineMXBean.JMX_OBJECT_NAME_FORMAT);
            ObjectName objectName = new ObjectName(name);
            return objectName;
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException("ObjectName is invalid");
        }
    }

}