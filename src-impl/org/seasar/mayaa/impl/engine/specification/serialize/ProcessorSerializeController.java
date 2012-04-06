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

import org.seasar.mayaa.engine.processor.ProcessorTreeWalker;
import org.seasar.mayaa.engine.specification.serialize.ProcessorReferenceResolver;
import org.seasar.mayaa.engine.specification.serialize.ProcessorResolveListener;

/**
 * @author Taro Kato (Gluegent, Inc.)
 */
public class ProcessorSerializeController implements ProcessorReferenceResolver {

    private List _processorListeners = new ArrayList();
    private Map _processors = new HashMap();

    public void init() {
        _processorListeners = new ArrayList();
        _processors = new HashMap(20);
    }

    public void release() {
        doNotify();
        for (Iterator it = _processorListeners.iterator(); it.hasNext(); ) {
            ProcessorListener listener = (ProcessorListener)it.next();
            listener.release();
        }
        _processorListeners.clear();
        _processors.clear();
    }

    public void processorLoaded(String uniqueID, ProcessorTreeWalker item) {
        _processors.put(uniqueID, item);
    }

    public ProcessorTreeWalker getProcessor(String uniqueID) {
        return (ProcessorTreeWalker) _processors.get(uniqueID);
    }

    public void registResolveProcessorListener(String uniqueID, ProcessorResolveListener listener) {
        _processorListeners.add(new ProcessorListener(uniqueID, listener));
    }

    public void doNotify() {
        for (Iterator it = _processorListeners.iterator(); it.hasNext(); ) {
            ProcessorListener listener = (ProcessorListener) it.next();
            listener._listener.notify(listener._id, getProcessor(listener._id));
        }
    }

    public static class ProcessorListener {
        String _id;
        ProcessorResolveListener _listener;

        public ProcessorListener(String id, ProcessorResolveListener listener) {
            _id = id;
            _listener = listener;
        }

        public void release() {
            _listener.release();
            _listener = null;
        }
    }

}

