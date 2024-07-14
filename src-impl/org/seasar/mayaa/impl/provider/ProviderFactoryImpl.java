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
package org.seasar.mayaa.impl.provider;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.FactoryFactory;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.MarshallUtil;
import org.seasar.mayaa.impl.NeedCompatibilityException;
import org.seasar.mayaa.impl.NonSerializableParameterAwareImpl;
import org.seasar.mayaa.impl.NeedCompatibilityException.CompatibilityType;
import org.seasar.mayaa.impl.provider.factory.ServiceProviderHandler;
import org.seasar.mayaa.impl.source.ApplicationSourceDescriptor;
import org.seasar.mayaa.impl.util.IOUtil;
import org.seasar.mayaa.impl.util.XMLUtil;
import org.seasar.mayaa.provider.ProviderFactory;
import org.seasar.mayaa.provider.ServiceProvider;
import org.seasar.mayaa.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ProviderFactoryImpl extends NonSerializableParameterAwareImpl implements ProviderFactory {
    private static final Log LOG = LogFactory.getLog(ProviderFactoryImpl.class.getName());

    private Object _context;
    private Class<?> _serviceClass;
    private ServiceProvider _provider;

    public boolean isServiceProviderInitialized() {
        return _provider != null;
    }

    protected ServiceProvider marshallServiceProvider(
            SourceDescriptor source, ServiceProvider beforeProvider) {
        if (source.exists()) {
            ServiceProviderHandler handler = new ServiceProviderHandler(beforeProvider);
            InputStream stream = source.getInputStream();
            try {
                XMLUtil.parse(handler, stream, CONST_IMPL.PUBLIC_PROVIDER10,
                        source.getSystemID(), true, true, false);
                return handler.getServiceProvider();
            } finally {
                IOUtil.close(stream);
            }
        }
        return beforeProvider;
    }

    protected ServiceProvider getServiceProvider(Object context) {
        try {
            return getServiceProvider(context, true);
        } catch (NeedCompatibilityException e) {
            assert(e.getCompatibilityType() == CompatibilityType.LoadFactoryDefinitionForwardWay);
            return getServiceProvider(context, false);
        }
    }

    class Pair {
        SourceDescriptor source;
        String location;
        Pair(SourceDescriptor source, String location) {
            this.source = source;
            this.location = location;
        }
    }

    /**
     * org.seasar.mayaa.provider.ServiceProviderファイルに定義されている内容で{@code ServiceProvider}を生成する。
     * <p>
     * v1.2 まではビルトイン、ロード中のMETA-INF内、WEB-INF内の順にそれぞれで配置された記述ファイルごとにインスタンスを生成していたが、
     * より後から生成されたもので破棄されていた（設定が継承されるわけではない）。
     * v1.2.1からはWEB-INF、ロード中のMETA-INF内、ビルトインの順で最初に見つかった記述ファイルのみを対象に生成する。
     * <p>
     * ただし、各エレメントの`class`属性で指定されたクラスにインタフェースクラスを1つ引数にとるコンストラクタが定義されている場合は、
     * 先に生成されたインスタンスを引き渡す仕様との互換性のために元の順序で生成を行う（オブジェクトベースの継承の挙動となっている）
     * 
     * @param context 設定する{@code UnderlyingContext}オブジェクト
     * @param loadBackwardWay WEB-INF、ロード中のMETA-INF内、ビルトインの順で読み込む場合はtrue
     * @return 生成された{@code ServiceProvider}
     * @throws IllegalStateException ファイルに記述された内容不正などで必要なオブジェクトが生成されなかった時
     */
    private ServiceProvider getServiceProvider(Object context, boolean loadBackwardWay) throws IllegalStateException {
        final String systemID = "org.seasar.mayaa.provider.ServiceProvider";

        List<Pair> sources = new ArrayList<>();
        // Collect source files
        // Mayaa Built-in source file
        SourceDescriptor source = MarshallUtil.getDefaultSource(systemID, ServiceProviderHandler.class);
        if (source.exists()) {
            sources.add(new Pair(source, "[Built-in]"));
            LOG.info("FOUND " + "[Built-in]" + source.getSystemID());
        }
        // 各META-INF/org.seasar.mayaa.provider.ServiceProvider を列挙する。順序は不定。
        Iterator<SourceDescriptor> it = MarshallUtil.iterateMetaInfSources(systemID);
        while (it.hasNext()) {
            source = it.next();
            if (source.exists()) {
                sources.add(new Pair(source, "META-INF"));
                LOG.info("FOUND " + "META-INF" + source.getSystemID());
            }
        }
        source = FactoryFactory.getBootstrapSource(ApplicationSourceDescriptor.WEB_INF, systemID);
        if (source.exists()) {
            sources.add(new Pair(source, "WEB-INF"));
            LOG.info("FOUND " + "WEB-INF" + source.getSystemID());
        }

        if (loadBackwardWay) {
            Collections.reverse(sources);
        }

        ServiceProvider provider = null;
        for (Pair s: sources) {
            LOG.info("LOADING " + s.location + s.source.getSystemID());
            provider = marshallServiceProvider(s.source, provider);
            validate(s.source, provider);
            if (loadBackwardWay && provider != null) {
                LOG.info("LOADED " + s.location + s.source.getSystemID());
                return provider;
            }
        }
        Pair last = sources.get(sources.size() - 1);
        LOG.info("LOADED " + last.location + last.source.getSystemID());
        return provider;
    }

    private void validate(SourceDescriptor source, ServiceProvider provider) throws IllegalStateException {
        ArrayList<String> lackedElementNames = new ArrayList<>();
        try {
            provider.getEngine();
        } catch (IllegalStateException e) {
            lackedElementNames.add("engine");
        }

        try {
            provider.getParentSpecificationResolver();
        } catch (IllegalStateException e) {
            lackedElementNames.add("parentSpecificationResolver");
        }

        try {
            provider.getScriptEnvironment();
        } catch (IllegalStateException e) {
            lackedElementNames.add("scriptEnvironment");
        }

        try {
            provider.getTemplateBuilder();
        } catch (IllegalStateException e) {
            lackedElementNames.add("templateBuilder");
        }

        try {
            provider.getTemplateAttributeReader();
        } catch (IllegalStateException e) {
            lackedElementNames.add("templateAttributeReader");
        }

        try {
            provider.getPathAdjuster();
        } catch (IllegalStateException e) {
            lackedElementNames.add("pathAdjuster");
        }

        try {
            provider.getSpecificationBuilder();
        } catch (IllegalStateException e) {
            lackedElementNames.add("specificationBuilder");
        }

        try {
            provider.getLibraryManager();
        } catch (IllegalStateException e) {
            lackedElementNames.add("libraryManager");
        }

        if (!lackedElementNames.isEmpty()) {
            throw new IllegalStateException("Some required elements are not specified in \"" + source.toString() + "\" " + lackedElementNames.toString());
        }
    }

    public void setServiceClass(Class<?> serviceClass) {
        if (serviceClass == null) {
            throw new IllegalArgumentException();
        }
        _serviceClass = serviceClass;
    }

    public Class<?> getServiceClass() {
        if (_serviceClass == null) {
            throw new IllegalArgumentException();
        }
        return _serviceClass;
    }

    public ServiceProvider getServiceProvider() {
        if (_provider == null) {
            _provider = getServiceProvider(_context);
        }
        return _provider;
    }

    // ContextAware implements -------------------------------------

    public void setUnderlyingContext(Object context) {
        if (context == null) {
            throw new IllegalArgumentException();
        }
        _context = context;
    }

    public Object getUnderlyingContext() {
        if (_context == null) {
            throw new IllegalStateException();
        }
        return _context;
    }

}
