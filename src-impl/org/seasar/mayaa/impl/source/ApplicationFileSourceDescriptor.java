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
package org.seasar.mayaa.impl.source;

import org.seasar.mayaa.FactoryFactory;
import org.seasar.mayaa.cycle.scope.ApplicationScope;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * ApplicationScopeを基準とし、かつ物理的なファイルであることを期待する場合の
 * SourceDescriptor。
 *
 * @author Koji Suga (Gluegent Inc.)
 */
public class ApplicationFileSourceDescriptor
        extends FileSourceDescriptor  {

    private static final long serialVersionUID = -2775274363708858237L;

    private transient ApplicationScope _application;

    public ApplicationFileSourceDescriptor() {
        super.setRoot("");
    }

    // use while building ServiceProvider.
    public void setApplicationScope(ApplicationScope application) {
        if (application == null) {
            throw new IllegalArgumentException();
        }
        _application = application;
    }

    public ApplicationScope getApplicationScope() {
        if (_application == null) {
            _application = FactoryFactory.getApplicationScope();
        }
        return _application;
    }

    public void setSystemID(String systemID) {
        super.setSystemID(systemID);
    }

    protected String getRealPath() {
        String path = super.getRealPath();
        if (StringUtil.isEmpty(path)) {
            path = "/";
        }
        return getApplicationScope().getRealPath(path);
    }

    // for serialize

    private void readObject(java.io.ObjectInputStream in)
            throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        _application = CycleUtil.getServiceCycle().getApplicationScope();
    }

}
