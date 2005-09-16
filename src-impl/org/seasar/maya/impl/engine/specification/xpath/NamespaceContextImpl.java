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
package org.seasar.maya.impl.engine.specification.xpath;

import org.jaxen.NamespaceContext;
import org.seasar.maya.engine.specification.Namespace;
import org.seasar.maya.engine.specification.PrefixMapping;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class NamespaceContextImpl implements NamespaceContext {

    private Namespace _namespace;
    
    public NamespaceContextImpl(Namespace namespaceable) {
        if(namespaceable == null) {
            throw new IllegalArgumentException();
        }
        _namespace = namespaceable;
    }
    
    public String translateNamespacePrefixToUri(String prefix) {
        PrefixMapping mapping = _namespace.getMappingFromPrefix(prefix, true);
        if(mapping != null) {
            return mapping.getNamespaceURI();
        }
        return null;
    }
    
}
