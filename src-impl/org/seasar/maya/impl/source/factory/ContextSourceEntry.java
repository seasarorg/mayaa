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
package org.seasar.maya.impl.source.factory;

import org.seasar.maya.cycle.Application;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.source.ServletSourceDescriptor;
import org.seasar.maya.impl.util.CycleUtil;
import org.seasar.maya.source.SourceDescriptor;
import org.seasar.maya.source.factory.DescriptorEntry;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ContextSourceEntry implements DescriptorEntry, CONST_IMPL {

    private Application _application;
    
    public String getProtocol() {
        return PROTOCOL_CONTEXT;
    }

    public SourceDescriptor createSourceDescriptor(String systemID) {
        if(_application == null) {
            _application = CycleUtil.getApplication();
        }
        return new ServletSourceDescriptor(
                _application, PROTOCOL_CONTEXT, "/", systemID);
    }
    
    public void putParameter(String name, String value) {
        throw new UnsupportedOperationException();
    }

}
