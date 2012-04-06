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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.builder.SpecificationBuilder;
import org.seasar.mayaa.cycle.scope.ApplicationScope;
import org.seasar.mayaa.engine.specification.NodeTreeWalker;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.engine.specification.serialize.NodeReferenceResolver;
import org.seasar.mayaa.engine.specification.serialize.NodeResolveListener;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.cycle.DefaultCycleLocalInstantiator;
import org.seasar.mayaa.impl.engine.EngineUtil;
import org.seasar.mayaa.impl.engine.specification.serialize.NodeSerializeController;
import org.seasar.mayaa.impl.engine.specification.serialize.SerializeThreadManager;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.source.NullSourceDescriptor;
import org.seasar.mayaa.impl.util.ObjectUtil;
import org.seasar.mayaa.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SpecificationImpl extends ParameterAwareImpl
        implements Specification, NodeReferenceResolver, CONST_IMPL {

    private static final long serialVersionUID = -7503898036935182468L;

    private static final Log LOG =
        LogFactory.getLog(SpecificationImpl.class);

    private Date _buildTimestamp;
    private Date _builtSourceTime;
    private SourceDescriptor _source;
    private volatile NodeTreeWalkerImpl _delegateNodeTreeWalker;
    private boolean _hasSource;
    private boolean _deprecated = true;
    private int _lastSequenceID;
    private transient boolean _specificationSerialize;

    protected boolean needCheckTimestamp() {
        return EngineUtil.getEngineSettingBoolean(CHECK_TIMESTAMP, true);
    }

    protected boolean isSourceExists() {
        return getSource().exists();
    }

    public boolean isSpecificationSerialize() {
        return _specificationSerialize && !IS_SECURE_WEB;
    }

    public void setSpecificationSerialize(boolean specificationSerialize) {
        _specificationSerialize = specificationSerialize;
    }

    protected SpecificationBuilder getBuilder() {
        return ProviderUtil.getSpecificationBuilder();
    }

    protected void setTimestamp(Date buildTimestamp) {
        _buildTimestamp = buildTimestamp;
    }

    protected NodeTreeWalker getNodeTreeWalker() {
        if (_delegateNodeTreeWalker == null) {
            synchronized (this) {
                if (_delegateNodeTreeWalker == null) {
                    _delegateNodeTreeWalker = new NodeTreeWalkerImpl();
                    _delegateNodeTreeWalker.setOwner(this);
                }
            }
        }
        return _delegateNodeTreeWalker;
    }

//  デバッグのときだけ有効にすること。finalize()をオーバーライドするとFinalizerなどから特別扱いされる。
//    protected void finalize() throws Throwable {
//        if (LOG.isTraceEnabled()) {
//            LOG.trace(toString() + " unloaded.");
//        }
//        super.finalize();
//    }

    public String toString() {
        String className = ObjectUtil.getSimpleClassName(getClass());
        return getSystemID() + "(" + className + "@"  + Integer.toHexString(hashCode()) + ")";
    }

    // Specification implements ------------------------------------

    // TODO isDeprecatedの高速化
    public boolean isDeprecated() {
        if (_deprecated == false) {
            _deprecated = _hasSource != isSourceExists();
            if (_deprecated == false) {
                if (_hasSource == false) {
                    return false;
                }
                _deprecated = (getTimestamp() == null);
                if (_deprecated == false) {
                    if (needCheckTimestamp() == false) {
                        return false;
                    }
                    Date sourceTime = getSource().getTimestamp();
                    // リバートしてもビルドする。
                    _deprecated =
                        sourceTime.equals(_builtSourceTime) == false;
                }
            }
        }
        return _deprecated;
    }

    public void build() {
        build(true);
    }

    public void build(boolean rebuild) {
        if (isDeprecated()) {
            setTimestamp(new Date());
            _hasSource = isSourceExists();
            if (_hasSource) {
                LOG.debug(getSystemID() + " build start.");
                Date sourceTime = getSource().getTimestamp();
                _lastSequenceID = 0;
                getBuilder().build(this);
                _builtSourceTime = sourceTime;
                _deprecated = false;
                if (isSpecificationSerialize()) {
                    SerializeThreadManager.serializeReserve(
                            this, CycleUtil.getServiceCycle().getApplicationScope().getUnderlyingContext());
                }
                return;
            }
            // rebuildの場合は存在したソースが無くなったことを意味するため、タイムスタンプを0にしない
            if (rebuild == false) {
                setTimestamp(new Date(0));
            }
            _builtSourceTime = new Date(0);
            _deprecated = false;
        }
    }

    public Date getTimestamp() {
        return _buildTimestamp;
    }

    public void setSource(SourceDescriptor source) {
        _source = source;
    }

    public SourceDescriptor getSource() {
        if (_source == null) {
            _source = new NullSourceDescriptor();
        }
        return _source;
    }

    // SequenceIDGenerator implements ------------------------------------

    public void resetSequenceID(int sequenceID) {
        _lastSequenceID = sequenceID;
    }

    public int nextSequenceID() {
        return _lastSequenceID++;
    }

    // NodeTreeWalker implements ------------------------------------

    public void clearChildNodes() {
        if (_delegateNodeTreeWalker != null) {
            _delegateNodeTreeWalker.clearChildNodes();
        }
    }

    public void setParentNode(NodeTreeWalker parentNode) {
        throw new IllegalStateException();
    }

    public NodeTreeWalker getParentNode() {
        return null;
    }

    public void addChildNode(NodeTreeWalker childNode) {
        getNodeTreeWalker().addChildNode(childNode);
    }

    public void insertChildNode(int index, NodeTreeWalker childNode) {
        getNodeTreeWalker().insertChildNode(index, childNode);
    }

    public Iterator iterateChildNode() {
        return getNodeTreeWalker().iterateChildNode();
    }

    public boolean removeChildNode(NodeTreeWalker node) {
        return getNodeTreeWalker().removeChildNode(node);
    }

    public NodeTreeWalker getChildNode(int index) {
        return getNodeTreeWalker().getChildNode(index);
    }

    public int getChildNodeSize() {
        return getNodeTreeWalker().getChildNodeSize();
    }

    public void kill() {
        // TODO deprecated のため削除
    }

    // NodeReferenceResolverFinder implements --------------------------------------

    public NodeReferenceResolver findNodeResolver() {
        return getNodeTreeWalker().findNodeResolver();
    }

    // PositionAware overrides ------------------------------------

    public String getSystemID() {
        if (getSource() == null) {
            return null;
        }
        return getSource().getSystemID();
    }

    public int getLineNumber() {
        return 0;
    }

    public boolean isOnTemplate() {
        return false;
    }

    // for serialize
    private static final String SERIALIZE_CONTROLLER_KEY =
        SpecificationImpl.class.getName() + "#serializeController";
    static {
        CycleUtil.registVariableFactory(
            SERIALIZE_CONTROLLER_KEY,
            new DefaultCycleLocalInstantiator() {
                public Object create(Object[] params) {
                    return new NodeSerializeController();
                }
                public void destroy(Object instance) {
                    if (instance instanceof NodeSerializeController) {
                        ((NodeSerializeController) instance).release();
                    }
                }
            });
    }


    protected static File getSerializedFile(String systemID) {
        ApplicationScope scope =
            CycleUtil.getServiceCycle().getApplicationScope();
        String cachePath = scope.getRealPath("WEB-INF/.mayaaSpecCache");
        File cacheDir = new File(cachePath);
        cacheDir.mkdirs();
        return new File(cacheDir,
                systemID.substring("/".length()).replace('/', '`') + ".ser");
    }

    public void serialize() {
        synchronized(this) {
            try {
                File outputFile = getSerializedFile(getSystemID());
                ObjectOutputStream stream =
                    new ObjectOutputStream(new FileOutputStream(outputFile));
                try {
                    nodeSerializer().init();
                    try {
                        stream.writeObject(this);
                    } finally {
                        nodeSerializer().release();
                    }
                } finally {
                    stream.close();
                }
            } catch (IOException e) {
                LOG.error("page serialize failed.", e);
            }
        }
    }

    protected void afterDeserialize() {
        // no-op
    }

    public Specification deserialize(String systemID) {
        synchronized(this) {
            File cacheFile = getSerializedFile(systemID);
            if (cacheFile.exists() == false) {
                return null;
            }
            Specification result;
            try {
                ObjectInputStream stream =
                    new ObjectInputStream(new FileInputStream(cacheFile));
                try {
                    nodeSerializer().init();
                    try {
                        result = (Specification) stream.readObject();
                        if (result instanceof SpecificationImpl) {
                            ((SpecificationImpl) result).afterDeserialize();
                        }
                    } finally {
                        nodeSerializer().release();
                    }
                } finally {
                    stream.close();
                }
                return result;
            } catch(Throwable e) {
                String message =
                    getSystemID() + " specification deserialize failed.";
                if (e.getMessage() != null) {
                    message += " " + e.getMessage();
                }
                LOG.info(message);
                cacheFile.delete();
                return null;
            }
        }
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (_delegateNodeTreeWalker != null) {
            _delegateNodeTreeWalker.setOwner(this);
        }
        for (Iterator it = iterateChildNode(); it.hasNext(); ) {
            NodeTreeWalker child = (NodeTreeWalker) it.next();
            child.setParentNode(this);
        }
        nodeSerializer().specLoaded(this);
    }

    private void writeObject(ObjectOutputStream out)
            throws IOException {
        out.defaultWriteObject();
    }

    public static NodeSerializeController nodeSerializer() {
        return (NodeSerializeController) CycleUtil.getGlobalVariable(
                SERIALIZE_CONTROLLER_KEY, null);
    }

    // NodeReferenceResolver implements ----------------------------

    public void registResolveNodeListener(
            String uniqueID, NodeResolveListener listener) {
        nodeSerializer().registResolveNodeListener(
                uniqueID, listener);
    }

    public void nodeLoaded(SpecificationNode item) {
        nodeSerializer().nodeLoaded(item);
    }

}
