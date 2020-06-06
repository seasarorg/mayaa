# Javaバージョンアップ非互換チェック

Mayaa 1.1.34時点のソースにおいてJDK1.4.2からJava7で公開されている非互換情報に対して影響の有無を確認する。

結果として、動作上の非互換はないがソースレベルでは必須ではないものの2ヶ所の改修影響があった。

1. 標準ライブラリがジェネリックス定義に(1.4.2 -> 5)
2. デフォルトのロケールとしてリソースの書式化のためのロケールと、メニューやUIのためのロケールを個別に指定できるようになった（6 -> 7）


## 非互換確認 1.4.2 - 5.0
https://www.oracle.com/technetwork/java/javase/compatibility-137462.html


### java.net.Proxy - Proxyクラスが2つのパッケージに定義される
影響なし。

### Generification - 標準ライブラリがジェネリックス定義に。
__影響あり。__

既存のソースで型パラメータの指定がない旨の警告が出る。

### Virtual Machine - VMのデフォルトがクライアントVMからサーバVMに変わる。
影響なし。

### Virtual Machine - `java.vm.info` プロパティの書式が変わる。
影響なし。

### Virtual Machine - クラスリテラルの参照でクラスが初期化されなくなる
影響なし。
既存ソース内で記述されているクラスリテラルは大きく下記のバターン
 * ロガーオブジェクトの取得時
 * ファクトリオブジェクトの引数として指定する時
 * TagHandlerでマーシャリングする時
 * StringUtilsでメッセージを取得する時

### Class Loader - ClassLoaderに指定するクラスを特定する名称としてバイナリ名以外は受け付けなくなった。
影響なし。
現行ソースではファクトリ定義のXML内にクラス名を指定する箇所があるが、
現時点では影響のない記述となっている。

### Serialization - コンパイラが自動的に生成したシリアルバージョンUID値の生成ロジックが変更となった。
影響なし。
既存ソース内でSerializableなクラスで明示的に`serialVersionUID`を指定していない箇所はない。

### Logging - java.util.logging.Levelのコンストラクタがnull許容しなくなった。
影響なし。

### Apache - java.xmlパッケージ内で使用されていたorg.apache.* が移動された。
影響なし。

### JAXP - JAXPが1.1から1.3に上がり、実装はXercesが用いられるようになった。
影響なし。
pom.xmlによる明示的なXercesへの依存関係があるため本件単体では影響なし。
Java7に向けてはXercesのバージョン互換チェックは必要。

### JAXP - SAXが2.0から2.0.2に上がった。
影響なし。

1. `ErrorHandler`, `EntityResolver`, `ContentHandler`, `DTDHandler` としてnullを設定できるようになった。nullを設定するとデフォルトの設定が適用される。
2. `DefaultHandler#resolveEntity`の例外宣言として`SAXException`に加え`IOException`が増えた。

2について `org.seasar.mayaa.impl.util.xml.XMLHandler`で`DefaultHandler#resolveEntity`をオーバーライドしているが、super実装を読んでいないため影響なし。

### JAXP - XSLT実装がXalanからXSLTCに変わった。
影響なし。

### 2D
影響なし。

### AWT
影響なし。

### Drag and Drop
影響なし。

### Swing
影響なし。

## 非互換確認 Java5 - Java6
https://www.oracle.com/technetwork/java/javase/compatibility-137541.html

### NIO - FileLockクラスが他のFileChannelクラスのインスタンスをチェックするようになった
影響なし。


### AWT
影響なし。

### java.beans.EventHandler Enforces Valid Arguments
影響なし。

### SWING
影響なし。

### The Duration and XMLGregorianCalendar equals() methods now return false for null parameter 
影響なし。

### The double-slash character string ("//") is reserved in JMX ObjectNames 
影響なし。

## 非互換確認 Java6 - Java7
https://www.oracle.com/java/technologies/compatibility.html

### JSR 334 - Improved Exception Handling May Cause source Incompatibility
影響なし。
影響のあるソースはコンパイラが検出するが、コンパイル結果としてエラーなしのため、影響なしと判断。

### API:Language - MirroredTypeException is now a subclass of MirroredTypesException
影響なし。

### API: Language - The TypeVisitor interface has been updated
影響なし。

### API: Language - Spec for java.lang.Float.parseFloat(String) and parseDouble(String) Updated to Document Exception
影響なし。

### API: Language - java.lang.Character.isLowerCase/isUpperCase Methods Are Updated to Comply with the Specified Unicode Definition
影響なし。

### API: Utilities - Inserting an Invalid Element into a TreeMap Throws an NPE
影響なし。

### API: Utilities - Formatter.format() Now Throws FormatFlagsConversionMismatchException
影響なし。

### API: NIO
影響なし。

### API: AWT
影響なし。

### API: 2D
影響なし。

### API: Internationalization - Separation of User Locale and User Interface Locale
__影響あり。__

リソースの書式化のためのロケールと、メニューやUIのためのロケールをデフォルトのロケールとして個別に指定できるようになった。
`DateFormatPool#borrowFormat` にて `Locale.getDefault()`を呼び出しているため、リソースの書式化のためのロケールを明示するかどうかを決める必要あり。

### API: JMX
影響なし。

### API: JDBC
影響なし。

### API: java.util package - Change in behavior of ArrayList iterator between JDK 6 and JDK 7
影響なし。

ArrayListのiterator実装が変更になり、size()メソッドをオーバーライドしつつ、iterator()メソッドをオーバーライドしていない場合は、注意が必要。
TemplateBuilderImpl.java 内の `AbsoluteCompareList` および `ReferenceCache`クラスにてArrayListを継承しているが、いずれも前述の条件を満たしていないため影響なし。

### HotSpot - Order of Methods returned by Class.get Methods can Vary
影響なし。

### Tools - ジェネリクス型推論処理
影響なし。
少なくとも既存コードにはジェネリクスの型推論を使用していないため。

### Tools - 可変引数処理
影響なし。
少なくとも既存コードには可変引数を使用していないため。

### JMX
影響なし。

### JAXP - The XSLTProcessorApplet Class is Removed
影響なし。

### JAXP - JAX-WS Server Throws a SOAP Fault when it Encounters a DTD
影響なし。

### Sound
影響なし。

### API: Utilities - Updated sort behavior for Arrays and Collections may throw an IllegalArgumentException
影響なし。

`Arrays.sort` および `Collections.sort` にて`Comparable`を満たさない要素をみつけた際に`IllegalArgumentException`をスローするが、利用箇所なし。

### API: Language - The ThreadGroup.setMaxPriority Method Now Behaves as Specified
影響なし。

### API: IO - java.io.File.setReadOnly and setWriteable Methods Have New Behavior
影響なし。

### API: Networking - Server Connection Shuts Down when Attempting to Read Data When http Response Code is -1
影響なし。

### API: Swing
影響なし。

### API: Text - The java.text.BreakIterator.isBoundary(int) Method Now Behaves as Specified
影響なし。

### API: AWT
影響なし。

### API: Internationalization - The MSLU Library Has been Removed
影響なし。

### API: Internationalization - UTF-8 implementation is upated to conform to Corrigendum to Unicode 3.0.1
影響なし。



