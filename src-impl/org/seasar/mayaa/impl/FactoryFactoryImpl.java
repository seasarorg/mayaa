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
        // If the switch is ON, force forward order without compatibility fallback.
        if (FactoryFactory.isDisabledBackwardOrderLoading()) {
            return getFactory(interfaceClass, context, false);
        }
        try {
            return getFactory(interfaceClass, context, true);
        } catch (NeedCompatibilityException e) {
            assert(e.getCompatibilityType() == CompatibilityType.LoadFactoryDefinitionForwardWay);
            return getFactory(interfaceClass, context, false);    
        }
    }

    static class Pair {
        SourceDescriptor source;
        String location;
        Pair(SourceDescriptor source, String location) {
            this.source = source;
            this.location = location;
        }
    }

    /**
     * UnifiedFactoryの最低限の妥当性チェックを行う。
     * getServiceClass()が呼び出せることを確認する。
     * 
     * @param source 読み込んだソース
     * @param factory チェック対象のファクトリ
     * @throws IllegalStateException ファクトリが適切に初期化されていない場合
     */
    private <T extends UnifiedFactory> void validateFactory(
            SourceDescriptor source, T factory) throws IllegalStateException {
        if (factory == null) {
            throw new IllegalStateException("Factory is null after loading: " + source.getSystemID());
        }
        try {
            factory.getServiceClass(); // 最低限の動作確認
        } catch (IllegalStateException e) {
            throw new IllegalStateException(
                "Factory is not properly initialized: " + source.getSystemID() + 
                " - " + e.getMessage(), e);
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
        
        List<Pair> sources = collectSources(interfaceClass);
        
        if (loadBackwardWay) {
            Collections.reverse(sources);
            return loadFactoryBackward(interfaceClass, context, sources);
        } else {
            return loadFactoryForward(interfaceClass, context, sources);
        }
    }

    /**
     * ソースファイルを収集する。
     * Built-in → META-INF → WEB-INF の順序で収集される。
     * 
     * @param interfaceClass ファクトリインタフェースクラス
     * @return 存在するソースファイルのリスト
     */
    private List<Pair> collectSources(Class<?> interfaceClass) {
        String systemID = interfaceClass.getName();
        List<Pair> sources = new ArrayList<>();
        
        // Mayaa Built-in source file
        SourceDescriptor source = MarshallUtil.getDefaultSource(systemID, UnifiedFactoryHandler.class);
        if (source.exists()) {
            sources.add(new Pair(source, "[Built-in]"));
            LOG.info("FOUND [Built-in] " + source.getSystemID());
        }
        
        // 各META-INF/org.seasar.mayaa.provider.ServiceProvider を列挙する。順序は不定。
        Iterator<SourceDescriptor> it = MarshallUtil.iterateMetaInfSources(systemID);
        while (it.hasNext()) {
            source = it.next();
            if (source.exists()) {
                sources.add(new Pair(source, "META-INF"));
                LOG.info("FOUND META-INF " + source.getSystemID());
            }
        }
        
        // WEB-INF
        source = getBootstrapSource(ApplicationSourceDescriptor.WEB_INF, systemID);
        if (source.exists()) {
            sources.add(new Pair(source, "WEB-INF"));
            LOG.info("FOUND WEB-INF " + source.getSystemID());
        }
        
        return sources;
    }

    /**
     * Backward順序でファクトリをロード（最初に見つかった有効なものを返す）。
     * 各ソースを順に試し、最初に妥当性検証を通過したファクトリを返す。
     * 
     * @param interfaceClass ファクトリインタフェースクラス
     * @param context アプリケーションコンテキスト
     * @param sources ソースファイルのリスト（既にリバース済み）
     * @return 最初に有効なファクトリ、または null
     */
    private <T extends UnifiedFactory> T loadFactoryBackward(
            Class<T> interfaceClass, Object context, List<Pair> sources) {
        for (Pair s : sources) {
            LOG.info("LOADING " + s.location + " " + s.source.getSystemID());
            T factory = marshallFactory(interfaceClass, context, s.source, null);
            
            if (factory != null) {
                try {
                    validateFactory(s.source, factory);
                    LOG.info("LOADED " + s.location + " " + s.source.getSystemID());
                    return factory;
                } catch (IllegalStateException e) {
                    LOG.warn("Factory validation failed for " + s.location + " " + 
                            s.source.getSystemID() + ", trying next: " + e.getMessage());
                    // 次のソースを試す
                }
            }
        }
        return null;
    }

    /**
     * Forward順序でファクトリをロード（チェーン的に上書き）。
     * 全てのソースを順に読み込み、前のファクトリを引き継ぎながら処理する。
     * 
     * @param interfaceClass ファクトリインタフェースクラス
     * @param context アプリケーションコンテキスト
     * @param sources ソースファイルのリスト
     * @return 最終的なファクトリ、または null
     */
    private <T extends UnifiedFactory> T loadFactoryForward(
            Class<T> interfaceClass, Object context, List<Pair> sources) {
        T factory = null;
        Pair lastSource = null;
        
        for (Pair s : sources) {
            LOG.info("LOADING " + s.location + " " + s.source.getSystemID());
            factory = marshallFactory(interfaceClass, context, s.source, factory);
            lastSource = s;
        }
        
        if (factory != null && lastSource != null) {
            validateFactory(lastSource.source, factory);
            LOG.info("LOADED " + lastSource.location + " " + lastSource.source.getSystemID());
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
