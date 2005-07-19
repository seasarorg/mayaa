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
package org.seasar.maya.impl.provider.factory;

import org.seasar.maya.impl.util.XmlUtil;
import org.seasar.maya.provider.Parameterizable;
import org.seasar.maya.source.factory.SourceEntry;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SourceEntryTagHandler extends AbstractParameterizableTagHandler {
    
    private SourceTagHandler _parent;
    private SourceEntry _sourceEntry;
    
    public SourceEntryTagHandler(SourceTagHandler parent) {
        if(parent == null) {
            throw new IllegalArgumentException();
        }
        _parent = parent;
    }

    protected void start(Attributes attributes) {
        String name = XmlUtil.getStringValue(attributes, "name", null);
        _sourceEntry = (SourceEntry)XmlUtil.getObjectValue(
                attributes, "class", null, SourceEntry.class);
        _parent.getSourceFactory().putSourceEntry(name, _sourceEntry);
    }

    protected void end(String body) {
        _sourceEntry = null;
    }
    
    public Parameterizable getParameterizable() {
        if(_sourceEntry == null) {
            throw new IllegalStateException();
        }
        return _sourceEntry;
    }

}
