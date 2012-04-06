/*
 * Copyright 2004-2012 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.mayaa.impl.engine.specification;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.seasar.mayaa.engine.specification.NodeTreeWalker;
import org.seasar.mayaa.engine.specification.serialize.NodeReferenceResolver;
import org.seasar.mayaa.impl.util.collection.NullIterator;

/**
 * @author Taro Kato (Gluegent, Inc.)
 */
public class NodeTreeWalkerImpl implements NodeTreeWalker {

    private static final long serialVersionUID = 2482332186727952663L;

    private transient NodeTreeWalker _owner;
    private transient NodeTreeWalker _parent;
    private List/*<NodeTreeWalker>*/ _childNodes;
    private String _systemID;
    private int _lineNumber;
    private boolean _onTemplate;

    public void setOwner(NodeTreeWalker owner) {
        _owner = owner;
    }

    // NodeTreeWalker implemetns ------------------------------------

    public void setParentNode(NodeTreeWalker parentNode) {
        if (parentNode == null) {
            throw new IllegalArgumentException();
        }
        _parent = parentNode;
    }

    public NodeTreeWalker getParentNode() {
        return _parent;
    }

    public void addChildNode(NodeTreeWalker childNode) {
        int index;
        if (_childNodes == null) {
            index = 0;
        } else {
            index = _childNodes.size();
        }
        insertChildNode(index, childNode);
    }

    public void insertChildNode(int index, NodeTreeWalker childNode) {
        if (childNode == null) {
            throw new IllegalArgumentException();
        }
        synchronized (this) {
            if (_childNodes == null) {
                _childNodes = new ArrayList();
            }
        }
        synchronized (_childNodes) {
            if (index < 0 || index > _childNodes.size()) {
                throw new IndexOutOfBoundsException();
            }
            _childNodes.add(index, childNode);
            childNode.setParentNode(_owner);
        }
    }

    public boolean removeChildNode(NodeTreeWalker childNode) {
        if (childNode != null && _childNodes != null) {
            synchronized (_childNodes) {
                return _childNodes.remove(childNode);
            }
        }
        return false;
    }

    public int getChildNodeSize() {
        if (_childNodes == null) {
            return 0;
        }
        return _childNodes.size();
    }

    public NodeTreeWalker getChildNode(int index) {
        if (_childNodes == null || index < 0 || index >= _childNodes.size()) {
            throw new IndexOutOfBoundsException();
        }
        return (NodeTreeWalker) _childNodes.get(index);
    }

    public Iterator iterateChildNode() {
        if (_childNodes == null) {
            return NullIterator.getInstance();
        }
        return _childNodes.iterator();
    }

    public void clearChildNodes() {
        if (_childNodes != null) {
            synchronized (_childNodes) {
                _childNodes.clear();
                _childNodes = null;
            }
        }
    }

    public void kill() {
        // TODO deprecated のため削除
    }

    // PositionAware implements -------------------------------------

    public void setSystemID(String systemID) {
        if (systemID == null) {
            throw new IllegalArgumentException();
        }
        _systemID = systemID;
    }

    public String getSystemID() {
        return _systemID;
    }

    public void setLineNumber(int lineNumber) {
        _lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return _lineNumber;
    }

    public void setOnTemplate(boolean onTemplate) {
        _onTemplate = onTemplate;
    }

    public boolean isOnTemplate() {
        return _onTemplate;
    }

    public NodeReferenceResolver findNodeResolver() {
        return SpecificationImpl.nodeSerializer();
    }

}

