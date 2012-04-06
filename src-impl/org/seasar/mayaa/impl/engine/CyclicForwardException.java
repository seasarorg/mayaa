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
package org.seasar.mayaa.impl.engine;

import org.seasar.mayaa.impl.util.AbstractMessagedException;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class CyclicForwardException extends AbstractMessagedException {

    private static final long serialVersionUID = 7307438131920621929L;

    private String _pageName;

    public CyclicForwardException(String pageName) {
        _pageName = pageName;
    }

    public String getPageName() {
        return _pageName;
    }

    protected String[] getParamValues() {
        return new String[] { _pageName };
    }

}
