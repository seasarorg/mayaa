/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
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

import org.seasar.maya.engine.specification.Namespaceable;
import org.seasar.maya.engine.specification.NodeNamespace;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class NodeNamespaceImpl implements NodeNamespace {

    private Namespaceable _namespaceable;
    private String _prefix;
    private String _namespaceURI;
    
    /**
     * @param prefix 名前空間プレフィックス。
     * @param namespaceURI 名前空間URI。非null、非ゼロ長。
     */
    public NodeNamespaceImpl(String prefix, String namespaceURI) {
        if(StringUtil.isEmpty(namespaceURI)) {
            throw new IllegalArgumentException();
        }
        if(prefix == null) {
            prefix = ""; 
        }
        _prefix = prefix;
        _namespaceURI = namespaceURI;
    }

    /**
     * 所属するノードを設定する。
     * @param node 所属ノード。
     */
    public void setNamespaceable(Namespaceable namespaceable) {
        if(namespaceable == null) {
            throw new IllegalArgumentException();
        }
        _namespaceable = namespaceable;
    }

    public Namespaceable getNamespaceable() {
        if(_namespaceable == null) {
            throw new IllegalStateException();
        }
        return _namespaceable;
    }

    public String getPrefix() {
        return _prefix;
    }
    
    public String getNamespaceURI() {
        return _namespaceURI;
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("xmlns");
        if(StringUtil.hasValue(_prefix)) {
            buffer.append(":").append(_prefix);
        }
        buffer.append("=").append(_namespaceURI);
        return buffer.toString();
    }
    
    public boolean equals(Object test) {
        if(test == null || (test instanceof NodeNamespaceImpl) == false) {
            return false;
        }
        NodeNamespaceImpl ns = (NodeNamespaceImpl)test;
        return _prefix.equals(ns.getPrefix()) && _namespaceURI.equals(ns.getNamespaceURI());
    }
    
    public int hashCode() {
        return toString().hashCode();
    }
    
}
