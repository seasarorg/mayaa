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
package org.seasar.mayaa.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.engine.specification.NodeTreeWalker;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.util.AbstractMessagedException;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class MayaaException
        extends AbstractMessagedException {

    private static final long serialVersionUID = -233090042202542209L;
    private static final Log LOG =
        LogFactory.getLog(MayaaException.class);

    private String _originalSystemID;
    private int _originalLineNumber = -1;
    private String _injectedSystemID;
    private int _injectedLineNumber = -1;

    public MayaaException() {
        this(null);
    }

    public MayaaException(Throwable cause) {
        super(cause);
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        if (cycle != null) {
            NodeTreeWalker original = cycle.getOriginalNode();
            if (original != null) {
                _originalSystemID = original.getSystemID();
                _originalLineNumber = original.getLineNumber();
            }
            NodeTreeWalker injected = cycle.getInjectedNode();
            if (injected != null) {
                _injectedSystemID = injected.getSystemID();
                _injectedLineNumber = injected.getLineNumber();
            }
        }
    }

    protected abstract String[] getMessageParams();

    private static final int RESERVED_PARAM_COUNT = 4;

    protected String[] getParamValues() {
        String[] params = ZERO_PARAM;
        try {
            params = getMessageParams();
            if (params == null) {
                params = ZERO_PARAM;
            }
        } catch (Throwable t) {
            if (LOG.isErrorEnabled()) {
                LOG.error(t.getMessage(), t);
            }
        }
        int paramLength = params.length;
        String[] newParams = new String[paramLength + RESERVED_PARAM_COUNT];
        newParams[0] = _originalSystemID;
        newParams[1] = Integer.toString(_originalLineNumber);
        newParams[2] = _injectedSystemID;
        newParams[3] = Integer.toString(_injectedLineNumber);
        System.arraycopy(params, 0, newParams, RESERVED_PARAM_COUNT, paramLength);
        return newParams;
    }

    public String getOriginalSystemID() {
        return _originalSystemID;
    }

    public int getOriginalLineNumber() {
        return _originalLineNumber;
    }

    public String getInjectedSystemID() {
        return _injectedSystemID;
    }

    public int getInjectedLineNumber() {
        return _injectedLineNumber;
    }

}
