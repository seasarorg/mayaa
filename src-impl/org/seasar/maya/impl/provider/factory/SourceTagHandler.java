/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
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

import org.seasar.maya.impl.source.factory.SourceFactoryImpl;
import org.seasar.maya.provider.Parameterizable;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SourceTagHandler extends AbstractParameterizableTagHandler {
    
    private ServiceTagHandler _parent;
    private SourceFactoryImpl _sourceFactory;
    
    public SourceTagHandler(ServiceTagHandler parent) {
        if(parent == null) {
            throw new IllegalArgumentException();
        }
        _parent = parent;
        putHandler("entry", new SourceEntryTagHandler(this));
    }
    
    protected void start(Attributes attributes) {
        _sourceFactory = new SourceFactoryImpl();
        _parent.getServiceProvider().setSourceFactory(_sourceFactory);
    }
    
    protected void end(String body) {
        _sourceFactory = null;
    }

    public SourceFactoryImpl getSourceFactory() {
        if(_sourceFactory == null) {
            throw new IllegalStateException();
        }
        return _sourceFactory;
    }
    
    public Parameterizable getParameterizable() {
        return getSourceFactory();
    }

}
