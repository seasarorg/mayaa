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

import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.XmlUtil;
import org.seasar.maya.impl.util.xml.TagHandler;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ProviderFactory;
import org.seasar.maya.source.SourceDescriptor;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SpecificationTagHandler extends TagHandler
		implements CONST_IMPL {
    
    private EngineTagHandler _parent;
    
    public SpecificationTagHandler(EngineTagHandler parent) {
        super("specification");
        if(parent == null) {
            throw new IllegalArgumentException();
        }
        _parent = parent;
    }
    
    protected void start(Attributes attributes) {
        String path = XmlUtil.getStringValue(attributes, "path", null);
        if(StringUtil.hasValue(path)) {
            ServiceProvider provider = ProviderFactory.getServiceProvider();
            SourceDescriptor source = provider.getPageSourceDescriptor(path);
            _parent.getEngine().setSource(source);
        }
    }
    
}
