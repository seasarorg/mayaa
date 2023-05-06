---
layout: base
title: HTML 部品 (動的)
eleventyNavigation:
  key: コンポーネントとの値の共有
  subtitle: HTML 部品 (動的)
  parent: 高度な使い方
  order: 5
tags: tutorial
---

## {{ title }}

コンポーネントで動的な表示を扱ってみましょう。スクリプトを使う方法などは通常のテンプレートと同じですので、特別なのは親 (利用する側) のページとの値の受け渡しです。基本的な考え方としては、コンポーネントを利用する {% proc "m:insert" %}タグの位置に、コンポーネントの内容を直接書いた場合と同じです。


### コンポーネントで親ページの変数を使う

次のサンプルはコンポーネント側で必要とする変数を決め、それを親ページ側であらかじめセットしておく方法のサンプルです。コンポーネントは "Hello Mayaa!" という文字列に続けて変数 `count` の内容を出力します。
親ページは{% proc "for" %}プロセッサを使いコンポーネントを繰り返し使用します。そのときのループカウンタとして `count` を使うようにします。

```html {data-filename=hello.html}
<html>
<body>
    # Hello
    <span id="loop">
        <div id="comp">dummy</div>
    </span>
</body>
</html>
```
```xml {data-filename=hello.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:for m:id="loop"
            init="${ var count = 0 }"
            test="${ count &lt; 5 }"
            after="${ count++ }" />

    <m:insert m:id="comp" path="/component.html" />
</m:mayaa>
```

```html {data-filename=component.html}
<html>
<body>
    # dummy for preview
    <span id="centered">
        <div id="message"
            style="text-align: center">component value</div>
    </span>
</body>
</html>
```
```xml {data-filename=component.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:doRender m:id="centered" />

    <m:write m:id="message"
        value="Hello Mayaa! ${ count }" replace="false" />
</m:mayaa>
```

ブラウザで http://localhost:8080/mayaa/hello.html にアクセスしてみましょう。

```html
実行結果 (改行などは実際の実行結果と異なります)
<html>
<body>
    # Hello
        <div id="message"
            style="text-align: center">Hello Mayaa! 0</div>
        <div id="message"
            style="text-align: center">Hello Mayaa! 1</div>
        <div id="message"
            style="text-align: center">Hello Mayaa! 2</div>
        <div id="message"
            style="text-align: center">Hello Mayaa! 3</div>
        <div id="message"
            style="text-align: center">Hello Mayaa! 4</div>
</body>
</html>
```

親テンプレート側の変数を素直に利用できていることがわかります。イメージとしては、JavaScript の変数スコープの考え方と同じです。もしコンポーネントの処理をするスコープで `count` という名前の変数がない場合、変数が見つからないというエラーが発生します。


![図 2-7-1: コンポーネントの変数スコープのイメージ](/images/component_var_scope.gif)


### 親ページからコンポーネントに変数を渡す

親ページから変数を明示的にコンポーネントへ渡すこともできます。{% proc "m:insert" %}タグの属性として「変数名="値"」と書くことで、指定した変数をコンポーネントに渡すことになります。コンポーネントのタイトルやキャプションを指定する場合などに便利です。
渡された変数を参照する場合、`${ binding.変数名 }` と書きます。存在しない変数名を指定すると `null` が返ります。


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
    <m:insert m:id="comp" path="/component.html"
        title="Component Title" />
</m:mayaa>
```

```html {data-filename=component.html}
<html>
<body>
    # dummy for preview
    <span id="centered">
        <h2><span id="componentTitle">Dummy Title</span></h2>
        <div id="message"
            style="text-align: center">component value</div>
    </span>
</body>
</html>
```
```xml {data-filename=component.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:doRender m:id="centered" />

    <m:write m:id="componentTitle" value="${ binding.title }" />
    <m:write m:id="message" value="Hello Mayaa!" replace="false" />
</m:mayaa>
```

```html
実行結果 (改行などは実際の実行結果と異なります)
<html>
<body>
    # Hello
        <h2>Component Title</h2>
        <div id="message" style="text-align: center">Hello Mayaa!</div>
</body>
</html>
```

この表記法のため、{% proc "insert" %}プロセッサが本来持つ属性名と同じ変数名 (たとえば id, path) はそのままでは使用できません。そのような変数を渡すには、属性の名前空間を mayaa の名前空間以外にします。

この変数の渡し方は、{% proc "insert" %}プロセッサの属性として正しくない属性を変数として扱うという仕組みのため、どんな名前空間を使っても問題ありません。
プレフィクスを付けた場合でも、変数名には属性のローカル名 (プレフィクスの無い状態) が使われます。


```xml {data-filename=hello.mayaa}
独自の名前空間を作って割り当てる
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org"
        xmlns:x="my_namespace">
    <m:insert m:id="comp" path="/component.html"
        x:title="Component Title" />
</m:mayaa>
```

### request, session で値を共有する

もちろん、request や session を使って値を共有することもできます。特別なことは何もありませんので説明は省略します。
