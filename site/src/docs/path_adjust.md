---
layout: base
title: パスを自動調整する - PathAdjuster
eleventyNavigation:
  key: パスを自動調整する
  subtitle: PathAdjuster
  parent: 高度な使い方
  order: 13
---

## PathAdjuster:パスを自動調整する

<a href="component1.html">コンポーネント機能</a>、<a href="layout.html">レイアウト共有機能</a>を使うときにテンプレートが別のディレクトリにあると、ブラウザで直接開く場合と実行時とで画像やスタイルシートなどのファイルパスが変わってしまう場合があります。そのような場合、パスを "./" で始めることで自動調整させることができます。

<a href="path_adjust_settings.html">設定</a>で有効/無効、"./" で始まっていないパスを対象とするかどうかを変更できます。


### 具体的な例
レイアウト共有機能を使う例で見てみましょう。


#### スタイルシートが正しく読み込まれない状態

次のようなファイル構成があるとします。hello.html は layout.html をレイアウトとして指定し、layout.html からは base.css を使っています。このままではファイルとしてプレビューはできても、実行時にはスタイルシートが読み込まれません。

```
コンテキストルート
  + css/
    - base.css
  + layouts/
    - layout.html
    - layout.mayaa
  - hello.html
  - hello.mayaa
```

```html {data-filename=hello.html}
<html>
<body>
    # DummyTitleHello

    <div id="content">Hello Mayaa!</div>
</body>
</html>
```

```xml {data-filename=hello.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org"
        extends="/layouts/layout.html">

    <m:doRender m:id="content" name="contentBody" />
</m:mayaa>
```

```html {data-filename=layouts/layout.html}
<html>
<head>
    <link href="../css/base.css" rel="stylesheet" type="text/css">
</head>
<body>
    # Hello

    <div id="contentPosition">Dummy content</div>
</body>
</html>
```

```xml {data-filename=layouts/layout.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:insert m:id="contentPosition" name="contentBody" />
</m:mayaa>
```

ブラウザで http://localhost:8080/mayaa/hello.html にアクセスしてみましょう。

```html
実行結果 (改行などは実際の実行結果と異なります)
<html>
<head>
    <link href="../css/base.css" rel="stylesheet" type="text/css">
</head>
<body>
    # Hello
    Hello Mayaa!
</body>
</html>
```

このとき、http://localhost:8080/css/base.css というスタイルシートを探していることになりますが、このパスには存在しないためスタイルシートは適用されません。


#### 正しく読み込まれるようにする

ファイルとしてプレビューする場合と実行時とで、どちらでも同じファイルを指すようにするために layout.html を次のように書き換えます。この状態でもファイルとしてプレビューした場合にはスタイルシートは正しく読み込まれます。


```html {data-filename=layouts/layout.html}
<html>
<head>
    <link href="./../css/base.css" rel="stylesheet" type="text/css">
</head>
<body>
    # Hello

    <div id="contentPosition">Dummy content</div>
</body>
</html>
```

Mayaa はパスが "./" で始まっている場合、そのテンプレートからの相対パスと解釈して絶対パスに置き換えます。
ブラウザで http://localhost:8080/mayaa/hello.html にアクセスしてみましょう。

```html
実行結果 (改行などは実際の実行結果と異なります)
<html>
<head>
    <link href="/mayaa/css/base.css" rel="stylesheet" type="text/css">
</head>
<body>
    # Hello
    Hello Mayaa!
</body>
</html>
```

スタイルシートのパスがコンテキストパスからの絶対パスに変換されているため、スタイルシートは正しく読み込まれます。

### 調整対象となるタグおよび属性

パスの調整は、HTML および XHTML の以下のタグの属性のみを対象とします。


<table align="center" cellpadding="5" cellspacing="2" class="tbline" summary="パスを自動調整するタグ名と属性名">
<thead>
<tr>
<th nowrap="nowrap"><div align="center">タグ名</div></th>
<th nowrap="nowrap"><div align="center">属性名</div></th>
</tr>
</thead>
<tbody>
<tr><td>a</td><td>href</td></tr>
<tr><td rowspan="2">applet</td><td>code</td></tr>
<tr><!--            applet  --><td>codebase</td></tr>
<tr><td>area</td><td>href</td></tr>
<tr><td>base</td><td>href</td></tr>
<tr><td>blockquote</td><td>cite</td></tr>
<tr><td>del</td><td>cite</td></tr>
<tr><td>embed</td><td>src</td></tr>
<tr><td>form</td><td>action</td></tr>
<tr><td rowspan="2">frame</td><td>longdesc</td></tr>
<tr><!--            frame  --><td>src</td></tr>
<tr><td>iframe</td><td>src</td></tr>
<tr><td rowspan="2">img</td><td>src</td></tr>
<tr><!--            img  --><td>usemap</td></tr>
<tr><td rowspan="2">input</td><td>src</td></tr>
<tr><!--            input  --><td>usemap</td></tr>
<tr><td>ins</td><td>cite</td></tr>
<tr><td>link</td><td>href</td></tr>
<tr><td rowspan="3">object</td><td>codebase</td></tr>
<tr><!--            object  --><td>data</td></tr>
<tr><!--            object  --><td>usemap</td></tr>
<tr><td>q</td><td>cite</td></tr>
<tr><td>script</td><td>src</td></tr>
</tbody>
</table>

### 調整対象となるプロセッサおよび属性

以下のプロセッサおよび属性も自動調整の対象となります。


<table align="center" cellpadding="5" cellspacing="2" class="tbline" summary="パスを自動調整するタグ名 (プロセッサ名) と属性名">
<thead>
<tr>
<th nowrap="nowrap"><div align="center">タグ名</div></th>
<th nowrap="nowrap"><div align="center">属性名</div></th>
</tr>
</thead>
<tbody>
<tr><td>m:mayaa</td><td>extends</td></tr>
<tr><td>m:insert</td><td>path</td></tr>
<tr><td>m:exec</td><td>src</td></tr>
</tbody>
</table>
