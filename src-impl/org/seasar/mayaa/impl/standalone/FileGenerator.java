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
package org.seasar.mayaa.impl.standalone;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.seasar.mayaa.impl.util.IOUtil;
import org.seasar.mayaa.impl.util.IteratorUtil;

/**
 * コンテキストルートからレンダリング対象のファイルを探し、レンダリングした結果を
 * ファイルに書き出します。
 * コンテキストパスはROOTの扱い(空文字列)になります。
 *
 * 必要な情報はbasePathとoutputPathです。
 * basePathはWebアプリケーションでいうところのコンテキストルートで、レンダリング
 * するファイルを探すルートフォルダです。
 * 存在しない場合はIllegalArgumentExceptionが発生します。
 * outputPathはレンダリング結果を出力する先のフォルダ名です。
 * 存在しない場合は自動的に作成します。
 *
 * また、AutoPageBuilderと同様の形式でファイル名フィルタを指定できます。
 * フィルタを指定しない場合は".html"の拡張子を持つファイルが対象になります。
 * フィルタの指定方法は2パターンあり、セミコロン(";")で区切ることで複数指定できます。
 * <ol>
 * <li>"."で始まる英数字のみの文字列の場合は拡張子とみなし、一致するものを対象とします。
 *    (大文字小文字を区別しない)</li>
 * <li>1以外の場合は正規表現とみなし、絶対パスがマッチするものを対象とします。</li>
 * </ol>
 *
 * オプション一覧
 * <dl>
 * <dt>-v, --version</dt>
 *   <dd>バージョン番号を表示します。</dd>
 * <dt>-h, --help</dt>
 *   <dd>ヘルプを表示します。</dd>
 * <dt>-b, --base &lt;コンテキストルートのパス名&gt;</dt>
 *   <dd>コンテキストルートにあたるbasePathを指定します。<strong>必須</strong>。</dd>
 * <dt>-o, --output &lt;出力先フォルダ名&gt;</dt>
 *   <dd>生成したファイルを出力する先outputPathを指定します。<strong>必須</strong>。</dd>
 * <dt>-f, --filters &lt;ファイル名フィルタ&gt;</dt>
 *   <dd>ファイル名フィルタを指定します。デフォルトは".html"。</dd>
 * <dt>-a, --attributes &lt;属性名と値のセット&gt;</dt>
 *   <dd>"name=value;name=value"の形で属性を指定します。applicationスコープの属性になります。</dd>
 * <dt>-p, --property &lt;ファイル名&gt;</dt>
 *   <dd>--attributesの代わりにプロパティファイルを使ってapplicationスコープの属性を指定します。</dd>
 * </dl>
 *
 * @author Koji Suga (Gluegent Inc.)
 */
public class FileGenerator {

    public static boolean generating = false;

    /**
     * メインメソッド。
     *
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        generating = true;
        Argument argument;
        try {
            argument = parse(args);
            validate(argument);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.out.println();
            printHelp();
            return;
        }

        if (argument.isVersion()) {
            printVersion();
            return;
        }
        if (argument.isHelp()) {
            printHelp();
            return;
        }

        FileSearchRenderer renderer = new FileSearchRenderer();

        renderer.init(
                argument.getBasePath(),
                argument.getOutputPath(),
                argument.getFilters());
        renderer.addAttributes(extractAttributes(argument));
        renderer.start();
        renderer.destroy();
    }

    /**
     * コマンドライン引数をパースして返します。
     *
     * @param args コマンドライン引数
     * @return コマンドライン引数オブジェクト
     * @throws IllegalArgumentException 引数が不足している場合
     */
    public static Argument parse(String[] args) {
        Argument argument = new Argument();

        Iterator it = IteratorUtil.toIterator(args);
        while (it.hasNext()) {
            String arg = (String) it.next();
            if (arg.equals("-v") || arg.equals("--version")) {
                argument.setVersion(true);
            } else if (arg.equals("-h") || arg.equals("--help")) {
                argument.setHelp(true);
            } else if (arg.equals("-b") || arg.equals("--base")) {
                if (it.hasNext() == false) {
                    throw new IllegalArgumentException("lack of argument \"base path\".");
                }
                argument.setBasePath((String) it.next());
            } else if (arg.equals("-o") || arg.equals("--output")) {
                if (it.hasNext() == false) {
                    throw new IllegalArgumentException("lack of argument \"output path\".");
                }
                argument.setOutputPath((String) it.next());
            } else if (arg.equals("-f") || arg.equals("--filters")) {
                if (it.hasNext() == false) {
                    throw new IllegalArgumentException("lack of argument \"filters\".");
                }
                argument.setFilters((String) it.next());
            } else if (arg.equals("-a") || arg.equals("--attributes")) {
                if (it.hasNext() == false) {
                    throw new IllegalArgumentException("lack of argument \"attributes\".");
                }
                argument.setAttributes((String) it.next());
            } else if (arg.equals("-p") || arg.equals("--property")) {
                if (it.hasNext() == false) {
                    throw new IllegalArgumentException("lack of argument \"property file path\".");
                }
                argument.setPropertyFilePath((String) it.next());
            }
        }

        return argument;
    }

    /**
     * 引数の妥当性を検証します。
     *
     * <ul>
     * <li>basePathが指定されていること</li>
     * <li>basePathのディレクトリが存在すること</li>
     * <li>outputPathが指定されていること</li>
     * <li>propertyが指定されているならファイルが存在すること</li>
     * </ul>
     *
     * @param argument
     */
    public static void validate(Argument argument) {
        if (argument.isHelp() || argument.isVersion()) {
            return;
        }
        if (argument.getBasePath() == null) {
            throw new IllegalArgumentException(
                    "Please set base path (-b, --base).");
        }
        if (argument.getOutputPath() == null) {
            throw new IllegalArgumentException(
                    "Please set output path (-o, --output).");
        }

        File base = new File(argument.getBasePath());
        if (base.exists() == false) {
            throw new IllegalArgumentException(
                    "base path \"" + base.getPath() + "\" is not exists.");
        } else if (base.isDirectory() == false) {
            throw new IllegalArgumentException(
                    "base path \"" + base.getPath() + "\" is not directory.");
        }

        if (argument.getPropertyFilePath() != null) {
            File prop = new File(argument.getPropertyFilePath());
            if (prop.exists() == false) {
                throw new IllegalArgumentException(
                        "property file \"" + prop.getPath() + "\" is not exists.");
            } else if (prop.isFile() == false) {
                throw new IllegalArgumentException(
                        "property file \"" + prop.getPath() + "\" is not file.");
            } else if (prop.canRead() == false) {
                throw new IllegalArgumentException(
                        "property file \"" + prop.getPath() + "\" cannot read.");
            }
        }
    }

    /**
     * --attributesと--propertyから属性情報を抽出してMapにして返します。
     * ひとつもない場合はnullを返します。
     * --attributesと--propertyとで同一名の属性を定義した場合、--attributes側が
     * 優先されます。
     *
     * @param argument コマンドライン引数
     * @return 属性のMapまたはnull
     */
    public static Map extractAttributes(Argument argument) {
        Map result = null;
        if (argument.getAttributes() != null) {
            result = new HashMap(10);

            // name=value;name=value の形
            String[] attributes = argument.getAttributes().split(";");
            for (int i = 0; i < attributes.length; i++) {
                String att = attributes[i];
                int eqIndex = att.indexOf('=');
                if (eqIndex > 0) {
                    result.put(
                            att.substring(0, eqIndex).trim(),
                            att.substring(eqIndex + 1).trim());
                }
            }
        }
        if (argument.getPropertyFilePath() != null) {
            if (result == null) {
                result = new HashMap(10);
            }
            // propertiesファイルから読み出す
            FileInputStream is = null;
            try {
                is = new FileInputStream(argument.getPropertyFilePath());
                Properties prop = new Properties();
                prop.load(is);
                for (Iterator keys = prop.keySet().iterator(); keys.hasNext();) {
                    String key = (String) keys.next();
                    if (result.containsKey(key) == false) {
                        result.put(key, prop.getProperty(key).trim());
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                IOUtil.close(is);
            }
        }
        return result;
    }

    /**
     * バージョン情報を表示します。
     * jarのMANIFEST.MFにImplementation-Version属性があることを前提としています。
     */
    protected static void printVersion() {
        Package packageInfo = FileGenerator.class.getPackage();
        System.out.print("Mayaa version: ");
        if (packageInfo != null) {
            System.out.println(packageInfo.getImplementationVersion());
        } else {
            System.out.println("undefined");
        }
    }

    /**
     * コマンドラインヘルプを表示します。
     */
    protected static void printHelp() {
        printVersion();

        System.out.println("Usage: java -cp <CLASSPATH> org.seasar.mayaa.impl.standalone.FileGenerator <-options>");
        System.out.println("   options:");
        System.out.println("       -v, --version: show version no.");
        System.out.println("       -h, --help   : show this help message.");
        System.out.println("     * -b, --base <base-path>");
        System.out.println("         : set context root path.(required)");
        System.out.println("     * -o, --output <output-path>");
        System.out.println("         : set output root path.(required)");
        System.out.println("       -f, --filters <filter-strings>");
        System.out.println("         : set filters. (ex: \".html;.*foobar.*\")");
        System.out.println("       -a, --attributes <attributes>");
        System.out.println("         : set attributes of application scope.");
        System.out.println("           (ex: \"name1=value1;name2=value2\"");
        System.out.println("       -p, --property <property-file-path>");
        System.out.println("         : set properties to attributes of application scope.");
        System.out.println();
    }

    /**
     * コマンドライン引数を扱いやすくするためのオブジェクト。
     *
     * @author Koji Suga (Gluegent Inc.)
     */
    protected static class Argument {
        private boolean _version = false;
        private boolean _help = false;
        private String _basePath;
        private String _outputPath;
        private String _filters;
        private String _attributes;
        private String _propertyFilePath;

        /**
         * @return the attributes
         */
        public String getAttributes() {
            return _attributes;
        }
        /**
         * @param attributes the attributes to set
         */
        public void setAttributes(String attributes) {
            _attributes = attributes;
        }
        /**
         * @return the basePath
         */
        public String getBasePath() {
            return _basePath;
        }
        /**
         * @param basePath the basePath to set
         */
        public void setBasePath(String basePath) {
            _basePath = basePath;
        }
        /**
         * @return the filters
         */
        public String getFilters() {
            return _filters;
        }
        /**
         * @param filters the filters to set
         */
        public void setFilters(String filters) {
            _filters = filters;
        }
        /**
         * @return the help
         */
        public boolean isHelp() {
            return _help;
        }
        /**
         * @param help the help to set
         */
        public void setHelp(boolean help) {
            _help = help;
        }
        /**
         * @return the outputPath
         */
        public String getOutputPath() {
            return _outputPath;
        }
        /**
         * @param outputPath the outputPath to set
         */
        public void setOutputPath(String outputPath) {
            _outputPath = outputPath;
        }
        /**
         * @return the propertyFilePath
         */
        public String getPropertyFilePath() {
            return _propertyFilePath;
        }
        /**
         * @param propertyFilePath the propertyFilePath to set
         */
        public void setPropertyFilePath(String propertyFilePath) {
            _propertyFilePath = propertyFilePath;
        }
        /**
         * @return the version
         */
        public boolean isVersion() {
            return _version;
        }
        /**
         * @param version the version to set
         */
        public void setVersion(boolean version) {
            _version = version;
        }
    }

}
