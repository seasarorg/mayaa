/*
 * Copyright 2004-2007 the Seasar Foundation and the Others.
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
import java.text.MessageFormat;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections.map.AbstractReferenceMap;
import org.apache.commons.collections.map.ReferenceMap;
import org.cyberneko.html.HTMLEntities;
import org.seasar.mayaa.impl.cycle.script.ScriptUtil;
import org.seasar.mayaa.impl.source.ClassLoaderSourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public final class StringUtil {

    private static Map _propFiles =
        new ReferenceMap(AbstractReferenceMap.SOFT, AbstractReferenceMap.SOFT, true);
    private static final String[] ZERO = new String[0];

    private StringUtil() {
        // no instantiation.
    }

    public static boolean isEmpty(Object test) {
        boolean result;
        if (test instanceof String) {
            result = isEmpty((String)test);
        } else {
            result = ScriptUtil.isEmpty(test);
            if (result == false) {
                result = isEmpty(test.toString());
            }
        }
        return result;
    }

    public static String valueOf(Object value) {
        String result = null;
        if (value != null) {
            result = value.toString();
        }
        return result;
    }

    /**
     * testがnullまたは空文字列ならtrueを返します。
     *
     * @param test 対象の文字列
     * @return nullまたは空文字列ならtrue
     */
    public static boolean isEmpty(String test) {
        return test == null || test.length() == 0;
    }

    public static boolean hasValue(Object test) {
        return !isEmpty(test);
    }

    public static boolean hasValue(String test) {
        return !isEmpty(test);
    }

    /**
     * パスの形式統一処理をしたものを返します。
     * 区切り文字を"/"に変え、末尾の"/"を取り除き、先頭が"/"でなければ
     * "/"始まりにします。
     * pathがnullまたは空文字列、trim()で空文字列になる場合は空文字列を返します。
     *
     * @param path 処理対象のパス
     * @return 処理後のパス
     */
    public static String preparePath(final String path) {
        if (isEmpty(path)) {
            return "";
        }
        char[] chars = toTrimedCharArray(path);
        if (chars.length == 0) {
            return "";
        }

        int length = chars.length;
        if (File.separatorChar != '/') {
            for (int i = 0; i < length; i++) {
                if (chars[i] == File.separatorChar) {
                    chars[i] = '/';
                }
            }
        }
        if (chars[length - 1] == '/') {
            length = length - 1;
        }
        if (chars[0] != '/') {
            char[] extended = new char[length + 1];
            System.arraycopy(chars, 0, extended, 1, length);
            extended[0] = '/';
            length = length + 1;

            chars = extended;
        }
        return new String(chars, 0, length);
    }

    /**
     * {@link String#trim()}後に{@link String#toCharArray()}したものと
     * 同じ、前後の空白を除去したchar配列を返す。
     *
     * @param value 処理対象の文字列
     * @return 前後の空白を除去したchar配列
     */
    public static char[] toTrimedCharArray(String value) {
        if (value == null) {
            return null;
        }
        char[] chars = value.toCharArray();
        int end = chars.length;
        int start = 0;

        while ((start < end) && (chars[start] <= ' ')) {
            start++;
        }
        while ((start < end) && (chars[end - 1] <= ' ')) {
            end--;
        }
        if (start > 0 || end < chars.length) {
            int length = end - start;
            char[] result = new char[length];
            System.arraycopy(chars, start, result, 0, length);
            return result;
        }
        return chars;
    }

    /**
     * &amp;xx;表記のエンティティを実体に置き換えて返します。
     *
     * @param blockString 置き換え処理対象の文字列
     * @return 実体に置き換えた後の文字列
     */
    public static String resolveEntity(String blockString) {
        StringBuffer buffer = new StringBuffer();
        int start = blockString.indexOf("&");
        if (start == -1) {
            return blockString;
        }
        buffer.append(blockString.substring(0, start));
        String entity;
        while (true) {
            int end = blockString.indexOf(";", start);
            if (end == -1) {
                buffer.append(blockString.substring(start));
                break;
            }
            entity = blockString.substring(start + 1, end);
            int value = HTMLEntities.get(entity);
            if (value != -1) {
                buffer.append((char) value);
            } else {
                buffer.append(blockString.substring(start, end + 1));
            }
            start = blockString.indexOf("&", end);
            if (start == -1) {
                buffer.append(blockString.substring(end + 1));
                break;
            }
            if (start != end + 1) {
                buffer.append(blockString.substring(end + 1, start));
            }
            if (start == blockString.length()) {
                break;
            }
        }
        return buffer.toString();
    }

    public static String[] parsePath(String path, String suffixSeparator) {
        String[] ret = new String[3];
        int paramOffset = path.indexOf('?');
        if (paramOffset >= 0) {
            path = path.substring(0, paramOffset);
        }
        int lastSlashOffset = path.lastIndexOf('/');
        String folder = "";
        String file = path;
        if (lastSlashOffset >= 0) {
            folder = path.substring(0, lastSlashOffset + 1);
            file = path.substring(lastSlashOffset + 1);
        }
        int lastDotOffset = file.lastIndexOf('.');
        if (lastDotOffset > 0) {
            ret[2] = file.substring(lastDotOffset + 1);
            file = file.substring(0, lastDotOffset);
        } else {
            ret[2] = "";
        }
        int suffixSeparatorOffset = file.lastIndexOf(suffixSeparator);
        if (suffixSeparatorOffset > 0) {
            ret[0] = folder + file.substring(0, suffixSeparatorOffset);
            ret[1] = file.substring(suffixSeparatorOffset + suffixSeparator.length());
        } else {
            ret[0] = folder + file;
            ret[1] = "";
        }
        return ret;
    }

    public static boolean isRelativePath(String path) {
        if (isEmpty(path)) {
            return false;
        }
        return path.startsWith("./");
    }

    /**
     * ファイル相対パスをコンテキスト相対パスにする。
     *
     * @param base リンクのあるページのコンテキスト相対パス ("/"始まり)
     * @param path リンクに書かれているパス
     * @return コンテキスト相対パス
     */
    public static String adjustRelativePath(String base, String path) {
        if (isRelativePath(path) == false) {
            return path;
        }

        if (isEmpty(base)) {
            throw new IllegalArgumentException();
        }

        try {
            String baseDir = base.substring(0, base.lastIndexOf('/'));
            return adjustRecursive(baseDir, path);
        } catch (StringIndexOutOfBoundsException e) {
            throw new RuntimeException(e);
        }
    }

    protected static String adjustRecursive(String dir, String path) {
        if (isEmpty(path)) {
            return dir + '/';
        }

        try {
            if (path.startsWith("../")) {
                if (isEmpty(dir)) {
                    return adjustRecursive(dir, path.substring(3));
                }

                String parentDir = dir.substring(0, dir.lastIndexOf('/'));
                return adjustRecursive(parentDir, path.substring(3));
            } else if (path.startsWith("./")) {
                return adjustRecursive(dir, path.substring(2));
            }

            return dir + '/' + path;
        } catch (StringIndexOutOfBoundsException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * コンテキスト相対パスからファイル相対パスにする。
     * hostPageからpathへたどるための相対パスを返す。
     *
     * @param hostPage リンクのあるページのコンテキスト相対パス ("/"始まり)
     * @param path リンクのコンテキスト相対パス ("/"始まり)
     * @return hostPageからpathへたどるための相対パス
     */
    public static String adjustContextRelativePath(String hostPage, String path) {
        if (isEmpty(hostPage) || isEmpty(path) || (path.startsWith("/") == false)) {
            return path;
        }

        String[] hostPath = hostPage.split("/");
        String[] linkPath = path.split("/");

        int matchedDirs = 1;// 最初は空なので1から
        while (hostPath.length > matchedDirs && linkPath.length > matchedDirs &&
                hostPath[matchedDirs].equals(linkPath[matchedDirs])) {
            matchedDirs += 1;
        }

        int depth = hostPath.length - (matchedDirs + 1);

        StringBuffer sb = new StringBuffer();
        sb.append(times("../", depth));

        for (int i = matchedDirs; linkPath.length > i; i++) {
            sb.append(linkPath[i]);
            sb.append('/');
        }
        if (path.charAt(path.length() - 1) != '/') {
            sb.delete(sb.length() - 1, sb.length());
        }

        return sb.toString();
    }

    /**
     * unitをcount回繰り返した文字列を返す。
     * unitがnull、あるいはcountが0以下ならば空文字列を返す。
     *
     * @param unit 繰り返す文字列
     * @param count 繰り返す回数
     * @return unitをcount回繰り返した文字列。
     */
    public static String times(String unit, int count) {
        if (count <= 0 || isEmpty(unit)) {
            return "";
        }
        StringBuffer sb = new StringBuffer(unit.length() * count);
        for (int i = 0; i < count; i++) {
            sb.append(unit);
        }
        return sb.toString();
    }

    public static String removeFileProtocol(String systemID) {
        if (hasValue(systemID) && systemID.startsWith("file://")) {
            return systemID.substring(7);
        }
        return systemID;
    }

    private static final String SP_OPEN = "${";
    private static final String SP_CLOSE = "}";

    public static boolean hasSystemProperties(String value) {
        int openIndex = value.indexOf(SP_OPEN);
        int closeIndex = value.lastIndexOf(SP_CLOSE);
        return (openIndex >= 0 && closeIndex >= 0 && openIndex < closeIndex);
    }

    public static String replaceSystemProperties(String value) {
        if (hasSystemProperties(value) == false) {
            return value;
        }

        StringBuffer buffer = new StringBuffer(value.length() + 100);

        int openIndex = value.indexOf(SP_OPEN);
        buffer.append(value.substring(0, openIndex));

        while (openIndex < value.length()) {
            int fromIndex = openIndex + SP_OPEN.length();

            int closeIndex = value.indexOf(SP_CLOSE, fromIndex);
            if (closeIndex >= 0) {
                String key = value.substring(fromIndex, closeIndex).trim();

                String propertyValue = System.getProperty(key);
                if (propertyValue == null) {
                    throw new IllegalStateException(key);
                }
                buffer.append(propertyValue);

                int lastIndex = closeIndex + SP_CLOSE.length();
                openIndex = value.indexOf(SP_OPEN, lastIndex);
                if (openIndex < 0) {
                    openIndex = value.length();
                }
                buffer.append(value.substring(lastIndex, openIndex));
            } else {
                buffer.append(value.substring(openIndex));
                openIndex = value.length();
            }
        }

        return buffer.toString();
    }

    public static String getMessage(Class clazz, int index) {
        return getMessage(clazz, index, ZERO);
    }

    public static String getMessage(Class clazz, int index, String param0) {
        return getMessage(clazz, index, new String[] { param0 });
    }

    public static String getMessage(Class clazz, int index,
            String param0, String param1) {
        return getMessage(clazz, index, new String[] { param0, param1 });
    }

    public static String getMessage(Class clazz, int index,
            String param0, String param1, String param2) {
        return getMessage(clazz, index,
                new String[] { param0, param1, param2 });
    }

//    public static String getMessage(Class clazz, int index,
//            String param0, String param1, String param2, String param3) {
//        return getMessage(clazz, index,
//                new String[] { param0, param1, param2, param3 });
//    }

    protected static String getMessage(
            Class clazz, int index, String[] params) {
        Package key = clazz.getPackage();
        Properties properties = (Properties) _propFiles.get(key);
        if (properties == null) {
            ClassLoaderSourceDescriptor source =
                new ClassLoaderSourceDescriptor();
            source.setSystemID("message.properties");
            source.setNeighborClass(clazz);
            properties = new Properties();
            _propFiles.put(key, properties);
            if (source.exists()) {
                InputStream stream = source.getInputStream();
                try {
                    properties.load(stream);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    IOUtil.close(stream);
                }
            }
        }
        String className = ObjectUtil.getSimpleClassName(clazz);
        StringBuffer propertyName = new StringBuffer(className);
        if (index > 0) {
            propertyName.append(".").append(index);
        }
        String message = properties.getProperty(propertyName.toString());
        if (isEmpty(message)) {
            message = "!" + clazz.getName() + "!";
        }
        if (params == null) {
            params = ZERO;
        }
        return MessageFormat.format(message, params);
    }

    public static String escapeXml(String text) {
        if (text == null) {
            return "";
        }
        char[] chars = text.toCharArray();
        StringBuffer sb = new StringBuffer(chars.length + 50);

        for (int i = 0; i < chars.length; i++) {
            switch (chars[i]) {
                case '&': sb.append("&amp;"); break;
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '"': sb.append("&quot;"); break;
                default: sb.append(chars[i]);
            }
        }

        return sb.toString();
    }

    public static String escapeWhitespace(String text) {
        if (text == null) {
            return "";
        }
        char[] chars = text.toCharArray();
        StringBuffer sb = new StringBuffer(chars.length + 50);

        for (int i = 0; i < chars.length; i++) {
            switch (chars[i]) {
                case '\r': sb.append("&#xd;"); break;
                case '\n': sb.append("&#xa;"); break;
                case '\t': sb.append("&#x9;"); break;
                default: sb.append(chars[i]);
            }
        }

        return sb.toString();
    }

    public static String escapeEol(String text, boolean forHTML) {
        if (text == null) {
            return "";
        }
        String br = (forHTML ? "<br>" : "<br />");

        char[] chars = text.toCharArray();
        StringBuffer sb = new StringBuffer(chars.length + 50);

        for (int i = 0; i < chars.length; i++) {
            switch (chars[i]) {
                case '\r':
                    sb.append(br);
                    if (chars.length > i + 1 && chars[i + 1] == '\n') {
                        i++;
                    }
                    break;
                case '\n': sb.append(br); break;
                default: sb.append(chars[i]);
            }
        }

        return sb.toString();
    }

    public static String parsePrefix(String qName) {
        if (qName == null) {
            throw new IllegalArgumentException();
        }
        int position = qName.indexOf(':');
        if (position >= 0) {
            return qName.substring(0, position);
        }
        return "";
    }

    public static boolean equals(Object s1, Object s2) {
        return s1 == null && s2 == null
            || (s1 != null && s1.equals(s2));
    }

    public static String join(Object[] items, String delimiter) {
        if (items == null) {
            return String.valueOf(items);
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < items.length; i++) {
            if (i > 0) {
                sb.append(delimiter);
            }
            sb.append(items[i]);
        }
        return sb.toString();
    }

    /**
     * 配列の浅いコピーを作成して返します。
     *
     * @param src 元となる配列
     * @return srcの浅いコピー
     * @throws NullPointerException
     */
    public static String[] arraycopy(String[] src) {
        String[] copy = new String[src.length];
        System.arraycopy(src, 0, copy, 0, src.length);
        return copy;
    }

}
