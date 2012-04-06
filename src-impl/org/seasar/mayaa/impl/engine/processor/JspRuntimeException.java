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

import org.seasar.mayaa.impl.MayaaException;

/**
 * @author AtsumiHayashi (Gluegent,Inc.)
 */
public class JspRuntimeException extends MayaaException {

    private static final long serialVersionUID = 3228547289753702951L;

    private String _originalName;
    private int _originalLineNumber;
    private String _injectedName;
    private int _injectedLineNumber;

    public JspRuntimeException(
            String originalName, int originalLineNumber, String injectedName,
            int injectedLineNumber, Throwable cause) {
        super(cause);
        _originalName = originalName;
        _originalLineNumber = originalLineNumber;
        _injectedName = injectedName;
        _injectedLineNumber = injectedLineNumber;
    }

    /**
     * @see org.seasar.mayaa.impl.MayaaException#getMessageParams()
     */
    protected String[] getMessageParams() {
        return new String[] {
                _originalName, Integer.toString(_originalLineNumber),
                _injectedName, Integer.toString(_injectedLineNumber),
                getCause().getMessage()};
    }

}
