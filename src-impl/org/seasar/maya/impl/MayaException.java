/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 *
 * Licensed under the Seasar Software License, v1.1 (aka "the License"); you may
 * not use this file except in compliance with the License which accompanies
 * this distribution, and is available at
 *
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.seasar.maya.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.util.CycleUtil;
import org.seasar.maya.impl.util.StringUtil;

/**
 * メッセージ設定機能付き実行時例外。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class MayaException extends RuntimeException {

	private static final long serialVersionUID = -9103534239273385474L;
	private static final Log LOG = LogFactory.getLog(MayaException.class);
    
    protected static final String[] ZERO_PARAM = new String[0];
    
    private String _originalSystemID;
    private int _originalLineNumber = -1;
    private String _injectedSystemID;
    private int _injectedLineNumber = -1;
    
	public MayaException() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        if(cycle != null) {
            SpecificationNode original = cycle.getOriginalNode();
            if(original != null) {
                _originalSystemID = original.getSystemID();
                _originalLineNumber = original.getLineNumber();
            }
            SpecificationNode injected = cycle.getInjectedNode();
            if(injected != null) {
                _injectedSystemID = injected.getSystemID();
                _injectedLineNumber = injected.getLineNumber();
            }
        }
    }
    
    protected int getMessageID() {
        return 0;
    }
    
    protected abstract String[] getMessageParams();
    
    public String getMessage() {
        String[] params = ZERO_PARAM;
        try {
            params = getMessageParams();
            if(params == null) {
                params = ZERO_PARAM;
            }
        } catch(Throwable t) {
            if(LOG.isErrorEnabled()) {
                LOG.error(t.getMessage(), t);
            }
        }
        int paramLength = params.length; 
        String[] newParams = new String[paramLength + 4];
        System.arraycopy(params, 0, newParams, 0, paramLength);
        newParams[paramLength] = _originalSystemID;
        newParams[paramLength + 1] = Integer.toString(_originalLineNumber);
        newParams[paramLength + 2] = _injectedSystemID;
        newParams[paramLength + 3] = Integer.toString(_injectedLineNumber);
        String message = StringUtil.getMessage(
                getClass(), getMessageID(), newParams);
        if(StringUtil.isEmpty(message)) {
            Throwable cause = getCause();
            if(cause != null) {
                return cause.getMessage();
            }
        }
        return message;
    }
    
    public String getClassName() {
        return getClass().getName();
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
