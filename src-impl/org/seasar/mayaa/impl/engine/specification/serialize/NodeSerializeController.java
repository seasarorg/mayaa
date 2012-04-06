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
package org.seasar.mayaa.impl.engine.specification.serialize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.seasar.mayaa.engine.specification.NodeTreeWalker;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.engine.specification.serialize.NodeReferenceResolver;
import org.seasar.mayaa.engine.specification.serialize.NodeResolveListener;

/**
 * @author Taro Kato (Gluegent, Inc.)
 */
public class NodeSerializeController implements NodeReferenceResolver {

    private List _nodeListeners;
    private Map _nodes;

    public void init() {
        _nodeListeners = new ArrayList(20);
        _nodes = new HashMap(100);
    }

    public void release() {
        doNotify();
        for (Iterator it = _nodeListeners.iterator(); it.hasNext(); ) {
            NodeListener listener = (NodeListener)it.next();
            listener.release();
        }
        _nodeListeners.clear();
        _nodes.clear();
    }

    public NodeTreeWalker getNode(String uniqueID) {
        return (NodeTreeWalker) _nodes.get(uniqueID);
    }

    public void doNotify() {
        for (Iterator it = _nodeListeners.iterator(); it.hasNext(); ) {
            NodeListener listener = (NodeListener) it.next();
            NodeTreeWalker node = getNode(listener._id);
            listener._listener.notify(listener._id, node);
            if (node != null) {
                it.remove();
            }
        }
    }

    /**
     * 存在しない場合は登録する。
     * @param node 登録するノード
     * @return 既に登録済みの場合はfalse。
     */
    public boolean collectNode(NodeTreeWalker node) {
    	String uniqueID = null;
    	if (node instanceof SpecificationNode) {
    		uniqueID = makeKey((SpecificationNode)node);
    	} else if (node instanceof Specification){
    		uniqueID = makeKey((Specification)node);
    	} else {
    		throw new IllegalArgumentException();
    	}
    	if (_nodes.containsKey(uniqueID)) {
    		return false;
    	}
    	_nodes.put(uniqueID, node);
    	return true;
    }

    public void registResolveNodeListener(String uniqueID, NodeResolveListener listener) {
        _nodeListeners.add(new NodeListener(uniqueID, listener));
    }

    public void nodeLoaded(SpecificationNode item) {
        _nodes.put(makeKey(item), item);
    }

    public void specLoaded(Specification item) {
        _nodes.put(makeKey(item), item);
    }

    public static String makeKey(SpecificationNode item) {
        return item.getSystemID() + "\n"
                + item.getSequenceID() + "\n"
                + item.getQName();
    }

    public static String makeKey(Specification item) {
        return item.getSystemID();
    }

    public static class NodeListener {
        String _id;
        NodeResolveListener _listener;

        public NodeListener(String id, NodeResolveListener listener) {
            _id = id;
            _listener = listener;
        }

        public void release() {
            _listener.release();
            _listener = null;
        }
    }

}
