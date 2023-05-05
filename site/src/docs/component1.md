---
layout: base
title: HTML 部品 (静的)
eleventyNavigation:
  key: コンポーネントによる共通化
  subtitle: HTML 部品 (静的)
  parent: 高度な使い方
  order: 5
tags: tutorial
---

## {{ title }}

Mayaa には HTML テンプレートと mayaa ファイルのセットを部品として扱う機能があります。このテンプレート部品のことを「コンポーネント」と呼びます。コンポーネントを作ることで機能の共有・再利用を実現できます。

コンポーネントの作り方は簡単で、基本的には通常の HTML テンプレートを作るのと変わりません。作った HTML テンプレートのうちコンポーネントにしたい部分に id をつけ、<a href="/docs/processors/#doRender">doRender プロセッサ</a>と結びつけることでコンポーネントとして扱えるようになります。コンポーネントを使う側は <a href="/docs/processors/#insert">insert プロセッサ</a>を使ってコンポーネントを指定します。コンポーネントの HTML テンプレートを直接表示させれば、通常の HTML テンプレートとして動作をテストすることもできます。

![図 2-6-1: コンポーネントはテンプレートの一部として作成](/images/component1.gif)


### 簡単な例

まずは簡単な例として、静的な表示のコンポーネントを作ってみましょう。


```html {data-filename=hello.html}
<html>
<body>
    # Hello
    <div id="comp">dummy</div>
</body>
</html>
```

```xml {data-filename=hello.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:insert id="comp" path="/component.html" replace="false" />
</m:mayaa>
```

```html {data-filename=component.html}
<html>
<body>
    # dummy for preview
    <span id="centered">
        <div style="text-align: center">component value</div>
    </span>
</body>
</html>
```

```xml {data-filename=component.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:doRender id="centered" replace="false" />
</m:mayaa>
```

関係するファイルは４つです。ユーザからリクエストされる `hello.html` およびその mayaa ファイル、それと `hello.html` から呼び出される `component.html` およびその mayaa ファイルです。

ブラウザで http://localhost:8080/mayaa/hello.html にアクセスしてみましょう。

```html
実行結果 (改行などは実際の実行結果と異なります)
<html>
<body>
    # Hello
    <div id="comp"><span id="centered">
        <div style="text-align: center">component value</div>
    </span></div>
</body>
</html>
```

実行結果は、ベースとなる `hello.html` の `<div id="comp">` のボディが、コンポーネントである `component.html` の `<span id="centered">` によって置き換えられたものになっています。

![図 2-6-2: コンポーネント描画イメージ](/images/component2.gif)

このとき、{% proc "m:doRender" %}プロセッサおよび{% proc "m:insert" %}プロセッサに割り当てられているタグを出力するかどうかは、
それぞれの `replace` の値に従います。この例では `hello.html` と `component.html` の両方とも `replace="false"` ですので、`hello.html` のタグの内側に `component.html` のタグが出力されています。
pathに指定するコンポーネントファイルパスは、コンテキストルートからの相対パスを指定します。先頭"/"はコンテキストルートを表します。



### ひとつのテンプレートで複数のコンポーネントを定義する

コンポーネントはひとつのテンプレートで複数定義できます。定義したコンポーネントを区別するには、それぞれのコンポーネントに名前を付け、呼び出すときにその名前を指定します。

名前を付けない場合、空文字列 ("") が名前として使われます。また、同一テンプレート上の複数のコンポーネントに同じ名前を付けることはできません。


```html {data-filename=hello.html}
<html>
<body>
    # Hello
    <div id="comp1">dummy</div>
    <div id="comp2">dummy</div>
</body>
</html>
```

```xml {data-filename=hello.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:insert m:id="comp1"
            path="/component.html" name="center1" replace="false" />
    <m:insert m:id="comp2"
            path="/component.html" name="center2" replace="false" />
</m:mayaa>
```
```html {data-filename=component.html}
<html>
<body>
    # dummy for preview
    <span id="centered1">
        <div style="text-align: center">component value 1</div>
    </span>
    <span id="centered2">
        <div style="text-align: center">component value 2</div>
    </span>
</body>
</html>
```

```xml {data-filename=component.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:doRender m:id="centered1" name="center1" replace="false" />
    <m:doRender m:id="centered2" name="center2" replace="false" />
</m:mayaa>
```

```html
実行結果 (改行などは実際の実行結果と異なります)
<html>
<body>
    # Hello
    <div id="comp1"><span id="centered1">
        <div style="text-align: center">component value 1</div>
    </span></div>
    <div id="comp2"><span id="centered2">
        <div style="text-align: center">component value 2</div>
    </span></div>
</body>
</html>
```

### コンポーネントでコンポーネントを使う

コンポーネントは通常のテンプレート同様、他のコンポーネントを利用できます。利用方法も同様です。

次の例は `hello.html` が `component1.html` のコンポーネントを利用し、`component1.html` のコンポーネントが `component2.html` のコンポーネントを利用しています。


```html {data-filename=hello.html}
<html>
<body>
    # Hello
    <div id="comp">dummy</div>
</body>
</html>
```
```xml {data-filename=hello.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:insert m:id="comp" path="/component1.html" replace="false" />
</m:mayaa>
```

```html {data-filename=component1.html}
<html>
<body>
    # dummy for preview
    <span id="centered">
        <div style="text-align: center">component value 1</div>
        <div id="comp2">dummy</div>
    </span>
</body>
</html>
```

```xml {data-filename=component1.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:doRender m:id="centered" replace="false" />
    <m:insert m:id="comp2" path="/component2.html" replace="false" />
</m:mayaa>
```

```html {data-filename=component2.html}
<html>
<body>
    # dummy for preview
    <span id="righted">
        <div style="text-align: right">component value 2</div>
    </span>
</body>
</html>
```
```xml {data-filename=component2.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:doRender m:id="righted" replace="false" />
</m:mayaa>
```

```html
実行結果 (改行などは実際の実行結果と異なります)
<html>
<body>
    # Hello
    <div id="comp"><span id="centered">
        <div style="text-align: center">component value 1</div>
        <div id="comp2"><span id="righted">
            <div style="text-align: right">component value 2</div>
        </span></div>
    </span></div>
</body>
</html>
```

![図 2-6-3: 2 段のコンポーネント描画イメージ](/images/component3.gif)

