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
package org.seasar.mayaa.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.FactoryFactory;
import org.seasar.mayaa.UnifiedFactory;
import org.seasar.mayaa.cycle.scope.ApplicationScope;
import org.seasar.mayaa.impl.NeedCompatibilityException.CompatibilityType;
import org.seasar.mayaa.impl.cycle.web.ApplicationScopeImpl;
import org.seasar.mayaa.impl.factory.UnifiedFactoryHandler;
import org.seasar.mayaa.impl.source.ApplicationSourceDescriptor;
import org.seasar.mayaa.impl.util.IOUtil;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.impl.util.XMLUtil;
import org.seasar.mayaa.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class FactoryFactoryImpl extends FactoryFactory
        implements CONST_IMPL {

    private static final Log LOG = LogFactory.getLog(FactoryFactoryImpl.class);

    protected boolean checkInterface(Class<?> clazz) {
        if (clazz != null && clazz.isInterface()
                && UnifiedFactory.class.isAssignableFrom(clazz)) {
            return true;
        }
        return false;
    }

    protected <T extends UnifiedFactory> T marshallFactory(
            Class<T> interfaceClass, Object context,
            SourceDescriptor source, T beforeFactory) {
        if (source == null) {
            throw new IllegalArgumentException();
        }
        String systemID = source.getSystemID();
        T factory;
        if (source.exists()) {
            if (LOG.isInfoEnabled()) {
                LOG.info("marshall factory: " + source.getSystemID());
            }
            UnifiedFactoryHandler<T> handler = new UnifiedFactoryHandler<>(interfaceClass, beforeFactory);
            InputStream stream = source.getInputStream();
            try {
                XMLUtil.parse(handler, stream, PUBLIC_FACTORY10,
                        systemID, true, true, false);
            } catch (Throwable t) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Factory parse error on " + systemID, t);
                }
                factory = beforeFactory;
            } finally {
                IOUtil.close(stream);
            }
            factory = handler.getUnifiedFactory();
        } else {
            factory = beforeFactory;
        }
        if (factory != null) {
            factory.setUnderlyingContext(context);
        }
        return factory;
    }

    @Override
    protected <T extends UnifiedFactory> T getFactory(Class<T> interfaceClass, Object context) {
        try {
            return getFactory(interfaceClass, context, true);
        } catch (NeedCompatibilityException e) {
            assert(e.getCompatibilityType() == CompatibilityType.LoadFactoryDefinitionForwardWay);
            return getFactory(interfaceClass, context, false);    
        }
    }

    /**
     * org.seasar.mayaa.provider.ServiceProviderファイルに定義されている内容で{@code ServiceProvider}を生成する。
     * v1.2.1まではビルトイン、ロード中のMETA-INF内、WEB-INF内にそれぞれ生成しており、後から生成されたもので無効になる（設定が継承されるわけではない）。
     * v1.2.1からはWEB-INF、ロード中のMETA-INF内、ビルトインの順で最初に見つかったファイルのみを対象に生成する。
     * <p>
     * ただし、各エレメントの`class`属性で指定されたクラスにインタフェースクラスを1つ引数にとるコンストラクタが定義されている場合は、
     * 先に生成されたインスタンスを引き渡す仕様との互換性のために元の順序で生成を行う（オブジェクトベースの継承の挙動となっている）
     * 
     * @param interfaceClass 生成するファクトリクラスに要求するインタフェースクラスオブジェクト
     * @param context 設定する{@code UnderlyingContext}オブジェクト
     * @param loadBackwardWay WEB-INF、ロード中のMETA-INF内、ビルトインの順で読み込む場合はtrue
     * @return 生成された{@code ServiceProvider}
     * @throws IllegalStateException ファイルに記述された内容不正などで必要なオブジェクトが生成されなかった時
     */
    private <T extends UnifiedFactory> T getFactory(Class<T> interfaceClass, Object context, boolean loadBackwardWay) {
        if (checkInterface(interfaceClass) == false || context == null) {
            throw new IllegalArgumentException();
        }
        String systemID = interfaceClass.getName();
        List<SourceDescriptor> sources = new ArrayList<>();

        // Collect source files
        // Mayaa Built-in source file
        SourceDescriptor source = MarshallUtil.getDefaultSource(systemID, UnifiedFactoryHandler.class);
        if (source.exists()) {
            sources.add(source);
        }
        // 
        Iterator<SourceDescriptor> it = MarshallUtil.iterateMetaInfSources(systemID);
        while (it.hasNext()) {
            source = it.next();
            if (source.exists()) {
                sources.add(source);
            }
        }
        source = getBootstrapSource(ApplicationSourceDescriptor.WEB_INF, systemID);
        if (source.exists()) {
            sources.add(source);
        }

        if (loadBackwardWay) {
            Collections.reverse(sources);
        }

        T factory = null;
        for (SourceDescriptor s: sources) {
            factory = marshallFactory(interfaceClass, context, s, factory);
            if (loadBackwardWay && factory != null) {
                return factory;
            }
        }
        return factory;
    }

    protected ApplicationScope getApplicationScope(Object context) {
        ApplicationScope application = new ApplicationScopeImpl();
        application.setUnderlyingContext(context);
        return application;
    }

    protected SourceDescriptor getBootstrapSource(
            String root, String systemID, Object context) {
        ApplicationSourceDescriptor appSource =
            new ApplicationSourceDescriptor();
        if (StringUtil.hasValue(root)) {
            appSource.setRoot(root);
        }
        appSource.setSystemID(systemID);
        appSource.setApplicationScope(getApplicationScope(context));
        return appSource;
    }

}
