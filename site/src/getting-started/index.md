---
layout: base
title: Mayaaを実行する
eleventyNavigation:
  key: Getting Started
  title: Mayaaを実行する
  sectiontitle: Getting Started
  order: 2
---
## {{ title }}

### Mayaaを入手する

現在の Mayaa は[Maven Central Repository](https://central.sonatype.com/artifact/com.github.seasarorg.mayaa/mayaa) から配布されています。<br>
※ v1.1.32以前は seasarプロジェクトのMevenリポジトリから配布されていました。([お知らせ 2017/03/27](/news/entries/20170327-v1.1.33/))

**groupId:**  com.github.seasarorg.mayaa<br>
**artifactId:** mayaa

### Webアプリケーションの土台を作る

Mayaaを使うためにまずはJavaウェブアプリケーションの土台となる構造を用意しましょう。
Mevenではarchetypeというプロジェクトのテンプレートを元に初期構造を生成する仕組みがありますので、
それを利用してWARプロジェクトを作成し、簡易に動作させるように[Jetty](https://www.eclipse.org/jetty/)で実行できるようにします。
下記の環境を前提とします。

* Java8 以上のJava開発環境
* Maven 3 以上

#### WARプロジェクトの生成
`maven-archetype-webapp`はシンプルで最低限の構造を生成します。コマンドプロンプトなどで下記コマンドを実行しましょう。
`-D`パラメータで指定している groupId, artifactId, version はなんでも構いません。
```sh
$ mvn archetype:generate -DarchetypeArtifactId=maven-archetype-webapp \
      -DgroupId=myproject \
      -DartifactId=mayaa-starter \
      -Dversion=0.1
```

実行すると確認のプロンプトが出ますのでエンターキーを押すとカレントディレクトリ内に
artifactId で指定した名前のサブディレクトリが作られ、その中にファイル群が生成されます。

```
mayaa-starter
 + pom.xml
 + src
   + main
     + webapp
       + index.jsp
       + WEB-INF
         + web.xml
```
次にMavenからJettyをつかってWebアプリケーションが実行できるように[Jettyプラグイン](https://www.eclipse.org/jetty/documentation/jetty-10/programming-guide/index.html#jetty-maven-plugin)を導入します。
pom.xml をエディタで開いて下記の内容を追記します。（先頭に + がついている行を追加します。+ は不要です。）

```xml {data-filename=pom.xml}
<project>
    [...]
    <build>
        <finalName>simple-webapp</finalName>
+       <plugins>
+         <plugin>
+           <groupId>org.eclipse.jetty</groupId>
+           <artifactId>jetty-maven-plugin</artifactId>
+           <version>10.0.0</version>
+         </plugin>
+       </plugins>
    </build>
    [...]
</project>
```

一旦ここで実行しておきましょう。
Maven の archetype:generate によって生成された index.jsp が Jettyで表示されるはずです。
ここまででうまくいかない場合は Jetty プラグインのバージョンやJDKバージョンとの整合性などが考えられますので
関連サイトなどでご確認ください。

```sh-
$ mvn jetty:run
[INFO] Scanning for projects...
[INFO] 
[INFO] -------------------------< mayaa-starter:no3 >--------------------------
[INFO] Building no3 Maven Webapp 1.0-SNAPSHOT
[INFO]   from pom.xml
[INFO] --------------------------------[ war ]---------------------------------
  (中略)
[INFO] --- jetty:10.0.0:run (default-cli) @ no3 ---
[INFO] Configuring Jetty for project: no3 Maven Webapp
  (中略)
[INFO] Started ServerConnector@18e4674d{HTTP/1.1, (http/1.1)}{0.0.0.0:8080}
[INFO] Started Server@6f5bd362{STARTING}[10.0.0,sto=0] @1596ms

Hit <enter> to redeploy:
```
ここまで表示されたら [http://localhost:8080/](http://localhost:8080/) にブラウザでアクセスしてみましょう。
初期配置されている src/main/webapp/index.jsp が JSPによって処理されて "Hello World" と表示されるはずです。

### Mayaaを組み込む

#### Mayaaの依存関係を追加する

```xml {data-filename=pom.xml}
  <dependency>
    <groupId>com.github.seasarorg.mayaa</groupId>
    <artifactId>mayaa</artifactId>
    <version>1.2</version>
  </dependency>
```

#### MayaaServlet を設定する

Mayaa自体はJSP代替のビューエンジンとして機能します。その実体はサーブレット `MayaaServlet` です。
Mayaa の基本的な使い方は、MayaaServlet を定義しページを返すときに forward する使い方です。
JSP にforward する場合と同じです。従って、Mayaa に任せるファイル (一般的には *.html) を MayaaServlet にマッピングする設定を行います。

Mayaaの主な特徴は次の４点です。

* テンプレートとして使えるファイルは HTML、XHTML、XML
* 設定ファイル（mayaaファイル）を使う方法、使わない方法があり、両方の混在も可能
* Java オブジェクトを操作する言語として JavaScript を採用
* JSP カスタムタグを使える

![基本的には forward して使う](/images/about_mayaa_forward.gif)

拡張子が .html の HTML ファイルのみを Mayaa に任せる場合、web.xml に次のように設定します。

```xml {data-filename=src/main/webapp/WEB-INF/web.xml}
 <!DOCTYPE web-app PUBLIC
  "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN"
  "http://java.sun.com/dtd/web-app_2_3.dtd" >

 <web-app>
   <display-name>Archetype Created Web Application</display-name>
+    <servlet>
+        <servlet-name>MayaaServlet</servlet-name>
+        <servlet-class>org.seasar.mayaa.impl.MayaaServlet</servlet-class>
+    </servlet>

+    <servlet-mapping>
+        <servlet-name>MayaaServlet</servlet-name>
+        <url-pattern>*.html</url-pattern>
+    </servlet-mapping>
 </web-app>
```
Mayaa はリクエストされたファイルの Mime-Type が html、xhtml、xml であるか、もしくは拡張子が .mayaa である場合に処理を行います。
それ以外であれば前述のパス解決を行ったあと、そのまま素通しします。
他の Servlet やアプリケーションに処理を任せるものがなければ、すべての URL を Mayaa に任せることもできます。
```xml
<servlet-mapping>
    <servlet-name>MayaaServlet</servlet-name>
    <url-pattern>/*</url-pattern>
</servlet-mapping>
```

### ページを配置する

Mayaa の基本的な使い方では、HTML テンプレートファイルとそれに対応する設定ファイル (拡張子 .mayaa) の２つを
元にして動的なページを生成します。HTML テンプレートの動的な部分に id 属性で印を付け、その id に対して設定をする形になります。

![図 1-1-2: Mayaa の基本的な動作](/images/about_mayaa_standard.gif)


```html {data-filename=src/main/webapp/index.html}
<html>
<body>
    <span id="message" >dummy message</span>
</body>
</html>
```
```xml {data-filename=src/main/webapp/index.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:write m:id="message" value="Hello Mayaa!" />
</m:mayaa>
```
改めて `mvn jetty:run` で実行してみましょう。
今度は "Hwllo World" ではなく、"Hello　Mayaa!"が表示されると思います。

JettyプラグインはWARを作成せず webappディレクトリを直接参照しており、起動させたまま各種ファイルを編集すると
起動中のWebアプリケーションの環境に直接反映できます。
いま配置した index.html や index.mayaa を編集して保存し、ブラウザをリロードすると反映されることが確認できると思います。
これはMayaa自体も htmlファイルや mayaaファイルの変更を検知して反映する機能を持っているためです。

以上でMayaaを動かすことができました。

それでは、次にMayaaでいろいろな値をHTMLとして出力してみましょう。

[いろいろな値を出してみる](/getting-started/various-values/)


