/*
 * Copyright 2004-2007 the Seasar Foundation and the Others.
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
package org.seasar.mayaa.impl.builder;

import org.seasar.mayaa.impl.MayaaException;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ProcessorNotInjectedException extends MayaaException {

    private static final long serialVersionUID = -6422698028264082995L;

    private String _injectedName;

    public ProcessorNotInjectedException(String injectedName) {
        _injectedName = injectedName;
    }

    public String getInjectedName() {
        return _injectedName;
    }

    protected String[] getMessageParams() {
        return new String[] { _injectedName };
    }

}
