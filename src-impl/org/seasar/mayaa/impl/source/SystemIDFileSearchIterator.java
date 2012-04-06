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
 */package org.seasar.mayaa.impl.source;

import java.io.File;
import java.io.FilenameFilter;

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
public class SystemIDFileSearchIterator extends FileSearchIterator {

    /**
     * filtersにnullを渡すと、".html"というフィルタが指定されたものとみなす。
     *
     * @param rootDir 探索を開始するフォルダ
     * @param filters フィルタ文字列の配列
     */
    public SystemIDFileSearchIterator(File rootDir, final String[] filters) {
        super(rootDir, new FilenameFilter() {
            public boolean accept(File dir, String name) {
                File file = new File(dir.getPath() + File.separatorChar + name);
                if (file.isHidden()) {
                    return false;
                }
                if (file.isDirectory()) {
                    return true;
                }
                if (filters == null) {
                    return name.toLowerCase().endsWith(".html");
                }
                for (int i = 0; i < filters.length; i++) {
                    String filter = filters[i].trim();
                    // 拡張子のフィルタか？
                    if (filter.matches("^\\.[a-zA-Z0-9]+")) {
                        if (name.toLowerCase().endsWith(filter.toLowerCase())) {
                            return true;
                        }
                    } else {
                        // それ以外は正規表現とみなす
                        String absolutePath =
                            file.getAbsolutePath().replace(File.separatorChar, '/');
                        if (absolutePath.matches(filter)) {
                            return true;
                        }
                    }
                }
                return false;
            }
            public String toString() {
                if (filters != null) {
                    return "[" + StringUtil.join(filters, ",") + "]";
                }
                return "";
            }
        });
    }

    public Object next() {
        return makeSystemID((File) super.next());
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
