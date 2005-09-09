/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which 
 * accompanies this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package org.seasar.maya.impl.engine.specification;

import org.seasar.maya.impl.MayaException;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PrefixMappingNotFoundException extends MayaException {

	private static final long serialVersionUID = -9114023056056051237L;

	private String _prefix;
    
    public PrefixMappingNotFoundException(String prefix) {
    	_prefix = prefix;
    }
    
    public String getPrefix() {
        return _prefix;
    }

    protected String[] getMessageParams() {
        return new String[] { _prefix };
    }

}