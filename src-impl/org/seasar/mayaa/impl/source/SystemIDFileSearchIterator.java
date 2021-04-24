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
package org.seasar.mayaa.impl.source;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.seasar.mayaa.impl.util.FileSearchIterator;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * 指定フォルダ以下のファイルのSystemIDを順番に返すIterator。
 * フォルダ内のファイルを全て返したら、次はサブフォルダを処理します。
 * 見つけたファイルのうち、ファイル名がフィルタに合致するファイルのみを対象とします。
 * ただし隠しファイルは対象としません。
 *
 * フィルタはセミコロン(";")で区切ることで複数指定できます。
 * フィルタの指定方法は2パターンあります。
 * <ol>
 * <li>"."で始まる英数字のみの文字列の場合は拡張子とみなし、一致するものを対象とします。
 *    (大文字小文字を区別しない)</li>
 * <li>1以外の場合は正規表現とみなし、絶対パスがマッチするものを対象とします。</li>
 * </ol>
 *
 * @author Taro Kato (Gluegent, Inc.)
 * @author Koji Suga (Gluegent Inc.)
 */
public class SystemIDFileSearchIterator extends FileSearchIterator implements Iterator<String> {

    /**
     * filtersにnullを渡すと、".html"というフィルタが指定されたものとみなす。
     *
     * @param rootDir 探索を開始するフォルダ
     * @param includeFilters 対象とするフィルタ文字列の配列
     * @param excludeFilters 除外するフィルタ文字列の配列
     */
    public SystemIDFileSearchIterator(final File rootDir, final String[] includeFilters, final String[] excludeFilters) {
        super(rootDir, new FilenameFilter() {
            final String rootAbsolutePath = rootDir.getAbsolutePath().replace(File.separatorChar, '/');
            Pattern filenamePattern;
            Pattern filenameExcludePattern;
            {
                if (includeFilters != null && includeFilters.length > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < includeFilters.length; i++) {
                        String filter = includeFilters[i].trim();
                        // 拡張子のフィルタか？
                        if (filter.matches("^\\.[a-zA-Z0-9]+")) {
                            sb.append(".*").append(filter).append("$|");
                        } else {
                            sb.append(filter).append("|");
                            // それ以外は正規表現とみなす
                        }
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    filenamePattern = Pattern.compile(sb.toString());    
                }
                if (excludeFilters != null && excludeFilters.length > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < excludeFilters.length; i++) {
                        String filter = excludeFilters[i].trim();
                        // それ以外は正規表現とみなす
                        sb.append(filter).append("|");
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    filenameExcludePattern = Pattern.compile(sb.toString());    
                }
            }
            public boolean accept(File dir, String name) {
                File file = new File(dir, name);
                if (file.isHidden()) {
                    return false;
                }
                if (filenamePattern == null) {
                    return name.toLowerCase().endsWith(".html");
                }

                String absolutePath = file.getAbsolutePath().replace(File.separatorChar, '/');
                if (!absolutePath.startsWith(rootAbsolutePath)) {
                    // rootDir配下でない場合はfalseを返す
                    return false;
                }
                final String systemID = absolutePath.substring(rootAbsolutePath.length());
                if (filenameExcludePattern != null && filenameExcludePattern.matcher(systemID).matches()) {
                    return false;
                }
                if (filenamePattern.matcher(systemID).matches()) {
                    return true;
                }
                if (file.isDirectory()) {
                    return true;
                }
                return false;
            }
            public String toString() {
                if (includeFilters != null) {
                    return "[" + StringUtil.join(includeFilters, ",") + "]";
                }
                return "";
            }
        });
    }

    @Override
    public boolean hasNext() {
        return super.hasNextFile();
    }

    @Override
    public String next() {
        return makeSystemID((File) super.nextFile());
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove");
    }

    protected String makeSystemID(File current) {
        String rootPath = getRoot().getPath();
        String filePath = current.getPath();
        filePath = filePath.substring(rootPath.length());
        filePath = filePath.replace(File.separatorChar, '/');
        if (filePath.length() > 0 && filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }
        return filePath;
    }

}
