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

import org.seasar.maya.builder.specification.InjectionResolver;
import org.seasar.maya.impl.util.XmlUtil;
import org.seasar.maya.provider.Parameterizable;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class InjectionResolverTagHandler extends AbstractParameterizableTagHandler {
    
    private TemplateBuilderTagHandler _parent;
    private InjectionResolver _resolver;
    
    public InjectionResolverTagHandler(TemplateBuilderTagHandler parent) {
        super("resolver");
        if(parent == null) {
            throw new IllegalArgumentException();
        }
        _parent = parent;
    }
    
    public void start(Attributes attributes) {
        _resolver = (InjectionResolver)XmlUtil.getObjectValue(
                attributes, "class", null, InjectionResolver.class);
        _parent.getTemplateBuilder().addInjectionResolver(_resolver);
    }
    
    public void end(String body) {
        _resolver = null;
    }
    
    public Parameterizable getParameterizable() {
        if(_resolver == null) {
            throw new IllegalStateException();
        }
        return _resolver;
    }
    
}
