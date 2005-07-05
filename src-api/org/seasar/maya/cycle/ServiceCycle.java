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
package org.seasar.maya.cycle;

import java.io.Serializable;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface ServiceCycle extends Serializable {

    String SCOPE_IMPLICIT = "implicit";
    String SCOPE_PAGE = "page";
    String SCOPE_REQUEST = "request";
    String SCOPE_SESSION = "session";
    String SCOPE_COOKIE = "cookie";
    String SCOPE_APPLICATION = "application";
    
    Request getRequest();
    
    Response getResponse();
    
    Object getAttribute(String name, String scope);
    
    void setAttribute(String name, Object attribute, String scope);
    
}
