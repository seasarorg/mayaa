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

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.util.CycleUtil;
import org.seasar.maya.impl.util.StringUtil;

/**
 * メッセージ設定機能付き実行時例外。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class MayaException extends RuntimeException {

	private static final long serialVersionUID = -9103534239273385474L;

    private SpecificationNode _originalNode;
    private SpecificationNode _injectedNode;
    
	public MayaException() {
		ServiceCycle cycle = CycleUtil.getServiceCycle();
		_originalNode = cycle.getOriginalNode().copyTo();
		_injectedNode = cycle.getInjectedNode().copyTo();
    }

    public MayaException(Throwable cause) {
        super(cause);
    }
    
    protected int getMessageID() {
        return 0;
    }
    
    protected Object[] getMessageParams() {
        return new Object[] { _originalNode, _injectedNode };
    }
    
    public String getMessage() {
        String message = StringUtil.getMessage(
                getClass(), getMessageID(), getMessageParams());
        if(StringUtil.isEmpty(message)) {
            Throwable cause = getCause();
            if(cause != null) {
                return cause.getMessage();
            }
        }
        return message;
    }
    
    public SpecificationNode getOriginalNode() {
    	return _originalNode;
    }
    
    public SpecificationNode getInjectedNode() {
    	return _injectedNode;
    }
    
}
