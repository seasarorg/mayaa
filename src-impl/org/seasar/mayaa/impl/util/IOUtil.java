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
package org.seasar.mayaa.impl.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.impl.CONST_IMPL;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class IOUtil {

    private static final Log LOG = LogFactory.getLog(IOUtil.class);
    private static boolean _useURLCache = false;

    static {
        // 一応環境変数で設定できるように。基本的に有効にしない。
        String env = System.getProperty("org.seasar.mayaa.useURLCache");
        if (env != null && env.equalsIgnoreCase("true")) {
            _useURLCache = true;
        }
    }

    private IOUtil() {
        // no instantiation.
    }

    /**
     * InputStreamをcloseする。
     * 例外が発生した場合はログにINFOレベルで出力する。
     *
     * @param stream closeするInputStream
     */
    public static void close(InputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ignore) {
                LOG.info(ignore.getMessage(), ignore);
            }
        }
    }

    /**
     * Readerをcloseする。
     * 例外が発生した場合はログにINFOレベルで出力する。
     *
     * @param reader closeするReader
     */
    public static void close(Reader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException ignore) {
                LOG.info(ignore.getMessage(), ignore);
            }
        }
    }

    /**
     * OutputStreamをcloseする。
     * 例外が発生した場合はログにINFOレベルで出力する。
     *
     * @param stream closeするOutputStream
     */
    public static void close(OutputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ignore) {
                LOG.info(ignore.getMessage(), ignore);
            }
        }
    }

    /**
     * InputStreamから指定文字エンコーディングでStringを読み出して返す。
     * 読み出したあとはInputStreamをcloseする。
     *
     * @param is 読み出すInputStream
     * @param encoding 文字エンコーディング
     * @return InputStreamの内容
     * @throws RuntimeException 内部で発生した例外をラップしたもの
     */
    public static String readStream(InputStream is, String encoding) {
        try {
            Reader reader = new InputStreamReader(is, encoding);
            try {
                StringBuffer sb = new StringBuffer();
                char[] buffer = new char[1024];
                int readSize = -1;
                while ((readSize = reader.read(buffer)) > 0) {
                    sb.append(buffer, 0, readSize);
                }
                return sb.toString();
            } finally {
                is.close();
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * OutputStreamへ指定文字エンコーディングでStringを書き出す。
     * 書き出したあとはOutputStreamをcloseする。
     *
     * @param os 書き出すInputStream
     * @param value 出力する内容
     * @param encoding 文字エンコーディング
     * @throws RuntimeException 内部で発生した例外をラップしたもの
     */
    public static void writeStream(OutputStream os, String value, String encoding) {
        try {
            Writer writer = new OutputStreamWriter(os, encoding);
            try {
                writer.write(value);
                writer.flush();
            } finally {
                os.close();
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * URLからInputStreamを取得する。
     * 標準だとURLのキャッシュを使わない。
     * キャッシュを使わない場合、jarファイルをホールドしない代わりに
     * パフォーマンスに問題が出る場合がある。
     * urlがnullの場合はnullを返す。
     *
     * URLのキャッシュを使うようにするには、システムプロパティに
     * "org.seasar.mayaa.useURLCache=true" を設定すること。
     *
     * @param url 読み込むURL
     * @return InputStream
     */
    public static InputStream openStream(URL url) {
        if (url == null) {
            return null;
        }
        try {
            URLConnection connection = url.openConnection();
            // キャッシュを使うとjarファイルを掴んでしまう
            // TODO useURLCacheをエンジン設定できるようにする
            connection.setUseCaches(_useURLCache);
            return connection.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * URLがファイルプロトコルかどうか判定する。
     * urlがnullまたは不正な場合はfalseを返す。
     *
     * @param url 対象のURL
     * @return ファイルプロトコルならtrue
     */
    public static File getFile(URL url) {
        if (url != null) {
            if ("file".equalsIgnoreCase(url.getProtocol())) {
                return new File(url.toString().substring(5));
            }
        }
        return null;
    }

    /**
     * URLから最終更新時刻を取得する。
     * fileにのみ対応。それ以外は{@link CONST_IMPL.NULL_DATE_MILLIS}を返す。
     *
     * @param url 読み込むURL
     * @return InputStream
     */
    public static long getLastModified(URL url) {
        File file = getFile(url);
        if (file != null) {
            return file.lastModified();
        }
        // TODO ServletContext#getResource で file ではなく jndi の URL が来た場合の対応
        return CONST_IMPL.NULL_DATE_MILLIS;
    }

    /**
     * カレントスレッドのContextClassLoaderを使ってリソースを読み込むInputStreamを返す。
     * リソースが見つからない場合、あるいは権限がない場合はnullを返す。
     * リソースのパスはクラスパスをルートとする絶対パス。
     *
     * @param name リソースの名前
     * @return InputStream
     */
    public static URL getResource(String name) {
        return getResource(name, Thread.currentThread().getContextClassLoader());
    }

    /**
     * カレントスレッドのContextClassLoaderを使ってリソースを読み込むInputStreamを返す。
     * リソースが見つからない場合、あるいは権限がない場合はnullを返す。
     * リソースのパスはクラスパスをルートとする絶対パス。
     *
     * @param name リソースの名前
     * @return InputStream
     */
    public static InputStream getResourceAsStream(String name) {
        return getResourceAsStream(name, Thread.currentThread().getContextClassLoader());
    }

    /**
     * クラスのあるパッケージを起点としてパスを解決する。
     * java.lang.Class#resolveName(String)
     *
     * @param neighbor 基準とするクラス
     * @param name リソースの名前
     * @return 解決したパス
     */
    protected static String resolveName(Class neighbor, String name) {
        if (name == null) {
            return name;
        }
        if (name.startsWith("/") == false) {
            Class c = neighbor;
            while (c.isArray()) {
                c = c.getComponentType();
            }
            String baseName = c.getName();
            int index = baseName.lastIndexOf('.');
            if (index != -1) {
                name = baseName.substring(0, index).replace('.', '/') + '/' + name;
            }
        } else {
            name = name.substring(1);
        }
        return name;
    }

    /**
     * neighborのClassLoaderを使ってリソースのURLを返す。
     * リソースが見つからない場合、あるいは権限がない場合はnullを返す。
     * リソースのパスはクラスパスをルートとする絶対パス。
     * neighborがnullの場合はカレントスレッドのContextClassLoaderを使う。
     *
     * @param name リソースの名前
     * @param neighbor リソースを探すための起点とするクラス
     * @return InputStream
     */
    public static URL getResource(String name, Class neighbor) {
        if (neighbor == null) {
            return getResource(name);
        }
        return getResource(resolveName(neighbor, name), neighbor.getClassLoader());
    }

    /**
     * neighborのClassLoaderを使ってリソースを読み込むInputStreamを返す。
     * リソースが見つからない場合、あるいは権限がない場合はnullを返す。
     * リソースのパスはneighborのパッケージを起点とする相対パス。
     * neighborがnullの場合はカレントスレッドのContextClassLoaderを使う。
     *
     * @param name リソースの名前
     * @param neighbor リソースを探すための起点とするクラス
     * @return InputStream
     */
    public static InputStream getResourceAsStream(String name, Class neighbor) {
        return openStream(getResource(name, neighbor));
    }

    /**
     * 指定したClassLoaderを使ってリソースのURLを返す。
     * リソースが見つからない場合、あるいは権限がない場合はnullを返す。
     * リソースのパスはクラスパスをルートとする絶対パス。
     * loaderがnullの場合はnullを返す。
     *
     * @param name リソースの名前
     * @param loader リソースを読み込むためのクラスローダー
     * @return InputStream
     */
    public static URL getResource(String name, ClassLoader loader) {
        if (loader == null) {
            return null;
        }
        return loader.getResource(name);
    }

    /**
     * 指定したClassLoaderを使ってリソースを読み込むInputStreamを返す。
     * リソースが見つからない場合、あるいは権限がない場合はnullを返す。
     * リソースのパスはクラスパスをルートとする絶対パス。
     * loaderがnullの場合はnullを返す。
     *
     * @param name リソースの名前
     * @param loader リソースを読み込むためのクラスローダー
     * @return InputStream
     */
    public static InputStream getResourceAsStream(String name, ClassLoader loader) {
        return openStream(getResource(name, loader));
    }

}
