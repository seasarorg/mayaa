---
layout: base
title: いろいろな値を出してみる
eleventyNavigation:
  key: いろいろな値を出してみる
  parent: Getting Started
  order: 1
tags: tutorial
---
## {{ title }}

### Hello Mayaa

「[Mayaaを実行する](/getting-started/)」で配置した index.html および index.mayaa について振り返ります。

#### テンプレートとMayaaファイル

MayaaにはHTMLを出力するための要素として「テンプレート」と「Mayaaファイル」の2つが存在します。
テンプレートは出力するHTMLテキストを記述するもので拡張子html などを持った通常のHTMLファイルです。
MayaaファイルはHTMLファイル内で動的な値に置き換えたり、繰り返しや条件判定によって出力内容を変更するための
機能を記述するXMLファイルです。拡張子は mayaa です。

通常は HTMLファイルの拡張子を除外したファイル名部分（今回は /index ）を使って対応するMayaaファイルを探します。
つまり、 /index.html に対応するのは /index.mayaa となります。ページ共通のMayaaファイルも配置できますが追って説明します。

Mayaaファイルについての詳細は[Mayaaファイルの記述方法](/getting-started/notation/)をご覧ください。

**テンプレート**
```html {data-filename=src/main/webapp/index.html}
<html>
<body>
    <span id="message" >dummy message</span>
</body>
</html>
```

**Mayaaファイル**
```xml {data-filename=src/main/webapp/index.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:write m:id="message" value="Hello Mayaa!" />
</m:mayaa>
```

### idを使ってテンプレートとMayaaファイル内の機能を関連づける

テンプレート `index.html` 上の span タグと `id` が一致するものを mayaa ファイル `index.mayaa` 上から探し、`value` の値である「Hello mayaa!」という文字列に置き換えて出力します。
1つのMayaaファイルには別の機能を持った`id`を複数記述することができます。

ブラウザで http://localhost:8080/index.html にアクセスしてみましょう。index.html は明示的に指定しなくても自動的に内部で付加されますので省略できます。

```html
実行結果 (改行などは実際の実行結果と異なります)
<html>
<body>
     Hello Mayaa!
</body>
</html>
```

テンプレートやMayaaファイル内の id を変更したり、Mayaaファイル内の`value`の値を変更したりして挙動がどう変わるかてみてください。

### 基本的にはエレメントを置き換える

mayaa ファイルで使用している{% proc "m:write" %}は `value` の値をそのまま出力するためのプロセッサです。

Mayaaではテンプレート内のエレメント(HTMLタグ)とidによって関連づけられた場合にそのエレメントをMayaaで記述された内容に置き換えます。

この挙動を変更してテンプレート上のタグを維持したまま中身だけ書き換えたい場合は、属性として `replace="false"` を追加します。`replace` はテンプレート上のタグを残すかどうかの設定で、デフォルトは `true` です。

```xml {data-filename=src/main/webapp/index.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:write m:id="message" value="Hello Mayaa!" replace="false" />
</m:mayaa>
```

```html
実行結果 (改行などは実際の実行結果と異なります)
<html>
<body>
    <span id="message">Hello Mayaa!</span>
</body>
</html>
```

### スクリプトでオブジェクトを出力する

>Mayaa では様々な場所でスクリプトを使用できます。スクリプト言語は JavaScript (標準で利用する実装は [Rhino](https://github.com/mozilla/rhino)) です。スクリプトを使用するときには `${ }` で囲んで記述します。

```html {data-filename=hello.html}
<html>
<body>
    <span id="message">dummy message</span>
</body>
</html>
```
```xml {data-filename=hello.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:write m:id="message" value="${ 1 + 2 }" />
</m:mayaa>
```

```html
実行結果 (改行などは実際の実行結果と異なります)
<html>
<body>
    3
</body>
</html>
```

### request や session 等に格納されている値を使う

JSP と同じようにスクリプトから request や session を使えます。たとえば request スコープにある "attributeName" という属性の値を取得するには、`${request.getAttribute("属性名")}` と書きます。

参照するだけであれば、もっと簡単に直接 `${属性名}` と変数名として記述できます。変数をスコープから探す順番は、page スコープ、request スコープ、session スコープ、application スコープの順で、どこにも見つからない場合は例外を投げます。

またクライアントから送信されてきたパラメータを参照するには、`${param.パラメータ名}` とします。この場合、存在しないパラメータ名を指定すると `null` が返ります。
詳細は [定義済みオブジェクト](/docs/defined-objects/) をご覧ください。

試しにクライアントから送信されてきたパラメータを参照してみましょう。`hello.mayaa` を次のように書き換えます。

```xml {data-filename=hello.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:write m:id="message" value="Hello ${ param.name }" />
</m:mayaa>
```

ブラウザで http://localhost:8080/mayaa/hello.html?name=world にアクセスしてみましょう。

```html
実行結果 (改行などは実際の実行結果と異なります)
<html>
<body>
    Hello world
</body>
</html>
```
URL で渡した name を参照できましたね。

### Javaのオブジェクトの表示

前節で記載したように requestスコープやsessionスコープに保存された値は文字列以外のJavaオブジェクトも参照することができます。

サンプルとしてJavaオプジェクトを作成します。
ここではBean は `example.MyBean` です。値はインスタンス生成時にセットしてしまいます。

実際の開発ではビジネスとロジックを処理するServletにて`HttpServletRequest#setAttribute`にJavaオブジェクトのインスタンスをセットし、Mayaa へ forward して画面を表示する使い方が多いでしょう。ここでは話を簡単にするために Mayaa だけで済ませてしまいます。

```java {data-filename=src/main/java/example/MyBean.java}
package example;

import java.math.BigDecimal;
import java.util.Date;

public class MyBean {

    private int _id = 1000;

    private String _name = "MyBean name";

    private BigDecimal _decimal = new BigDecimal("12.345");

    private Date _timestamp = new Date();

    // setter, getter 省略
```

次に usebean.mayaa の 5 行目で request の属性として MyBean のインスタンスをセットします（参考 [beforeRender - 描画前後のスクリプト](/docs/before_render/)）、それを画面に表示しています。
```xml {data-filename=src/main/webapp/usebean.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org"
        xmlns:fmt="http://java.sun.com/jsp/jstl/fmt">
    <m:beforeRender>
        request.setAttribute('bean', new Packages.example.MyBean());
    </m:beforeRender>

    <m:write m:id="id" value="${ bean.id }" />
    <m:write m:id="name" value="${ bean.name }" />
    <fmt:formatNumber m:id="decimal" value="${ bean.decimal }" />
    <fmt:formatDate m:id="timestamp" value="${ bean.timestamp }" />
</m:mayaa>
```

```html {data-filename=src/main/webapp/usebean.html}
<html>
<head>
    <title>use bean</title>
</head>
<body>
    # Use bean
    <table border="1">
    <tr>
        <th>property</th>
        <th>value</th>
    </tr>
    <tr>
        <td>id</td>
        <td><span id="id">10 dummy</span&gt;</td>
    </tr>
    <tr>
        <td>name</td>
        <td><span id="name">dummy name</span></td>
    </tr>
    <tr>
        <td>decimal</td>
        <td><span id="decimal">1.2 dummy</span></td>
    </tr>
    <tr>
        <td>timestamp</td>
        <td><span id="timestamp">1970/1/1 dummy</span></td>
    </tr>
    </table>
</body>
</html>
```

ブラウザで http://localhost:8080/usebean.html にアクセスしてみましょう。

```html
実行結果 (改行などは実際の実行結果と異なります)
<html>
<head>
    <title>use bean</title>
</head>
<body>
    # Use bean
    <table border="1">
    <tr>
        <th>property</th>
        <th>value</th>
    </tr>
    <tr>
        <td>id</td>
        <td>1000</td>
    </tr>
    <tr>
        <td>name</td>
        <td>MyBean name</td>
    </tr>
    <tr>
        <td>decimal</td>
        <td>12.345</td>
    </tr>
    <tr>
        <td>timestamp</td>
        <td>2005/11/14</td>
    </tr>
    </table>
</body>
</html>
```

