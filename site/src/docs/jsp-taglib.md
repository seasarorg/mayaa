---
layout: base
title: JSPタグライブラリを使う
eleventyNavigation:
  key: JSPタグライブラリを使う
  parent: 高度な使い方
  order: 3
tags: tutorial
---

## {{ title }}

Mayaa では JSP カスタムタグをほぼそのまま利用できます (違いについては「[Mayaa と JSP カスタムタグ](mayaa_customtag_diff.html)」を参照してください)。ここでは例として JSTL 1.0 の一部を使ってみましょう。Mayaa Getting Started package には JSTL 1.1 のライブラリ (Jakarta Taglibs - Standard Taglib 1.1.2) を同梱していますので、.mayaa ファイルに記述を追加するだけで使えるようになります。(core および format のみ)

### JSTL core の out タグを使う

「Hello Mayaa!」とだけ表示するサンプルを JSTL core の `out` タグを使って作ってみましょう。「インストールしよう」で動作させた `C:\tomcat\webapps\mayaa` の下に新しくファイルを追加します。

ダミーメッセージを含むテンプレート `hello_jstl.html` と、その設定を行う `hello_jstl.mayaa` を次のように作成します。



```html {data-filename=hello_jstl.html}
<html>
<body>
    <span id="message">dummy message</span>
</body>
</html>
```

```xml {data-filename=hello_jstl.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org"
        xmlns:c="http://java.sun.com/jsp/jstl/core">
    <c:out m:id="message" value="Hello Mayaa!" />
</m:mayaa>
```

「いろいろな値を出してみる」で作成したものとの異なるのは mayaa ファイルのみです。記述方法については次の「6. 設定の記述方法」で説明しますので、ここでは例の通りに作成してください。

ブラウザで http://localhost:8080/mayaa/hello_jstl.html にアクセスしてみましょう。

```html
実行結果 (改行などは実際の実行結果と異なります)
<html>
<body>
    Hello Mayaa!
</body>
</html>
```

「いろいろな値を出してみる」で {% proc "m:write" %}を使った場合と全く同じ結果になりました。id 指定したタグを出力することも、値としてスクリプトを使用することも、{% proc "m:write" %}と同様に実現できます。

#### テンプレート上のタグを残す

「いろいろな値を出してみる」のサンプルの {% proc "m:write" %}を JSTL core の `out` タグに置き換えたものです。

```html {data-filename=hello_jstl.html}
<html>
<body>
    <span id="message">dummy message</span>
</body>
</html>
```

```xml {data-filename=hello_jstl.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org"
        xmlns:c="http://java.sun.com/jsp/jstl/core">
    <c:out m:id="message" value="Hello Mayaa!" m:replace="false" />
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

#### ■スクリプトでオブジェクトを出力する

「いろいろな値を出してみる」のサンプルの {% proc "m:write" %}を JSTL core の `out` タグに置き換えたものです。

```html {data-filename=hello_jstl.html}
<html>
<body>
    <span id="message">dummy message</span>
</body>
</html>
```

```xml {data-filename=hello_jstl.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org"
        xmlns:c="http://java.sun.com/jsp/jstl/core">
    <c:out m:id="message" value="${ 1 + 2 }" />
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

この実行結果は Mayaa 1.1.2 以前の場合、{% proc "m:write" %}の実行結果「3」と異なり、「3.0」となります。
これは、スクリプトから `c:out` に渡る値の型が double になるためです。(「[Mayaa と JSP カスタムタグ](mayaa_customtag_diff.html)」を参照してください)


## Appendix 1. カスタムタグの注意点

Mayaa で JSP カスタムタグを利用する場合、JSP カスタムタグの仕様と異なる点があります。

JSTL を使う場合に注意する点は、動的な値の扱い方の違い、スクリプト変数のスコープの違いの 2 点です。



### 動的な値の扱い方の違い

JSP との大きな違いは、動的な値の扱い方です。JSP カスタムタグの動的な値の扱い方は 3 種類あります。それぞれの場合について、JSP と Mayaa の違いを説明します。



#### ■RT 版 (JSTL 1.0)

Mayaa が動的な値を処理します。

RT 版とは、動的な値に Scriptlet を使うものです。TLD で `rtexprvalue` に `true` が設定されている属性に Scriptlet を使えます。たとえば次のような書き方をします。

```html
<c:out value="<%= name %>" />
```

Mayaa では Scriptlet の位置に `${ .. }` の形式でスクリプトを書きます。評価結果がカスタムタグに渡ることは JSP と同じです。

```
<c:out value="${ name }" />
```

Mayaa で RT 版を使う場合、動的な値は `${ .. }` の形式でスクリプトを記述してください。


#### ■JSP 1.2 EL 版 (JSTL 1.0)

カスタムタグが動的な値を処理します。

EL 版とは、動的な値にカスタムタグの式言語を使うものです。TLD ですべての属性に `rtexprvalue` に `false` が設定されています。たとえば次のような書き方をします。

```
<c:out value="${ name }" />
```
JSP 1.2 EL 版では **文字列のままカスタムタグに渡され、カスタムタグの処理として式言語の評価が行われます** 。従って JSP 1.2 EL 版の動作は JSP と Mayaa に違いはありません。ただし、Mayaa のスクリプトと記述方法が同じなため混同しないよう注意してください。

Mayaa で JSP 1.2 EL 版を使う場合、`${ .. }` の中には JSP 1.2 仕様の EL を記述してください。

#### ■JSP 2.0 EL 版 (JSTL 1.1)

Mayaa が動的な値を処理します。

JSP 2.0 EL 版は JSP 1.2 EL 版と違い、式言語の評価を JSP コンテナが行います。`rtexprvalue` に `true` が設定されている属性に式言語を使えます。Mayaa は JSP コンテナとして振舞いますので、式言語の評価も Mayaa の役目になります。Mayaa で式言語にあたるのはスクリプトですから、`${ .. }` という記述を Mayaa がスクリプトとして評価します。従って Mayaa の場合、RT 版と JSP 2.0 EL 版で動的な値の扱い方は同じです。

Mayaa で JSP 2.0 EL 版を使う場合、`${ .. }` は Mayaa のスクリプトとして記述してください。

#### ■Mayaa のスクリプトと式言語の違い

Mayaa のスクリプトは JavaScript (ECMA Script: 実装は <a href="http://www.mozilla.org/rhino/" title="Rhino - JavaScript for Java">Rhino (http://www.mozilla.org/rhino/)</a>) です。
文法は当然 Java と異なりますが、それ以外にも最終結果として Java のオブジェクトまたはプリミティブ値として返すときに期待する動作と異なる場合があります。

たとえば「<a href="hello.html#script" title="2-1. 最初の一歩">スクリプトでオブジェクトを出力する</a>」の例では **Mayaa 1.1.2 以前の場合、** 結果として「3」を期待したところ「3.0」が返ってきました。これは JSP カスタムタグが Object 型を受け取るように作られているために型を決定できず、最も精度の高い double で渡していることが原因です。Mayaa 1.1.3 にて Java ←→ JavaScript 間の変換時は元の型が維持されるよう修正しましたが、JavaScript 内で Object 型の引数に対して Number を渡す場合は、同様の問題が発生します。

この例の場合に限れば、文字列に変換した後で渡す `fmt:numberFormat` を使うなどの回避策があります。式言語の暗黙的な動作を期待しているカスタムタグの
場合はこのような回避策を取る必要があるかもしれません。

