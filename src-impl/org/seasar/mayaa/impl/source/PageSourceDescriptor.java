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

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.seasar.mayaa.impl.IllegalParameterValueException;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PageSourceDescriptor extends CompositeSourceDescriptor {

    private static final long serialVersionUID = -6821253020265849514L;

    private LinkedHashSet _folders = new LinkedHashSet();
    private LinkedHashSet _absolutePaths = new LinkedHashSet();

    public void setSystemID(String systemID) {
        super.setSystemID(systemID);
        ApplicationSourceDescriptor app1 = new ApplicationSourceDescriptor();
        app1.setSystemID(systemID);
        addSourceDescriptor(app1);

        for (Iterator it = _absolutePaths.iterator(); it.hasNext();) {
            FileSourceDescriptor file = new FileSourceDescriptor();
            file.setRoot((String) it.next());
            file.setSystemID(systemID);
            addSourceDescriptor(file);
        }

        for (Iterator it = _folders.iterator(); it.hasNext();) {
            ApplicationSourceDescriptor app2 = new ApplicationSourceDescriptor();
            app2.setRoot((String) it.next());
            app2.setSystemID(systemID);
            addSourceDescriptor(app2);
        }

        ClassLoaderSourceDescriptor loader = new ClassLoaderSourceDescriptor();
        loader.setRoot(ClassLoaderSourceDescriptor.META_INF);
        loader.setSystemID(systemID);
        addSourceDescriptor(loader);
    }

    public void setParameter(String name, String value) {
        if ("folder".equals(name)) {
            if (StringUtil.isEmpty(value)) {
                throw new IllegalParameterValueException(getClass(), name);
            }
            _folders.add(value);
        } else if ("absolutePath".equals(name)) {
            String path = StringUtil.preparePath(value);
            if (StringUtil.isEmpty(path)
                    || new File(path).isDirectory() == false) {
                throw new IllegalParameterValueException(getClass(), name);
            }
            _absolutePaths.add(path);
        }
        super.setParameter(name, value);
    }

}
