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
package org.seasar.mayaa.impl.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class AbstractMessagedException
        extends RuntimeException {

    private static final long serialVersionUID = -287199613481216863L;
    private static final Log LOG =
        LogFactory.getLog(AbstractMessagedException.class);

    protected static final String[] ZERO_PARAM = new String[0];

    public AbstractMessagedException() {
        //NoOperation
    }

    public AbstractMessagedException(Throwable cause) {
        super(cause);
    }

    protected int getMessageID() {
        return 0;
    }

    protected abstract String[] getParamValues();

    public String getMessage() {
        String[] params = ZERO_PARAM;
        try {
            params = getParamValues();
            if (params == null) {
                params = ZERO_PARAM;
            }
        } catch (Throwable t) {
            if (LOG.isErrorEnabled()) {
                LOG.error(t.getMessage(), t);
            }
        }
        String message = StringUtil.getMessage(
                getClass(), getMessageID(), params);
        if (StringUtil.isEmpty(message)) {
            Throwable cause = getCause();
            if (cause != null) {
                return cause.getMessage();
            }
        }
        return message;
    }

}
