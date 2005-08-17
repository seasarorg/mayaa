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

import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.util.MessageUtil;
import org.seasar.maya.impl.util.StringUtil;

/**
 * メッセージ設定機能付き実行時例外。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class MayaException extends RuntimeException {

	private static final long serialVersionUID = -9103534239273385474L;

    private SpecificationNode _node;
    private int _messageID;
    
	public MayaException() {
    }

    public MayaException(Throwable cause) {
        super(cause);
    }
    
    protected void setMessageID(int messageID) {
        _messageID = messageID;
    }
    
    public String getMessage() {
        String message = MessageUtil.getMessage(getClass(), _messageID);
        if(StringUtil.isEmpty(message)) {
            Throwable cause = getCause();
            if(cause != null) {
                return cause.getMessage();
            }
        }
        return message;
    }
    
    public void setCurrentNode(SpecificationNode node) {
        _node = node;
    }
    
    public SpecificationNode getCurrentNode() {
        return _node;
    }
    
}
