---
layout: base
title: 属性のみを置き換える - echoプロセッサ
eleventyNavigation:
  key: 属性のみを置き換える
  subtitle: echoプロセッサ
  parent: プロセッサリファレンス
  order: 22
tags: processor
prev:
  path: exec
  title: タグ描画時のスクリプト
next:
  path: with
  title: プロセッサをまとめる
---

## {{ title }}

通常はタグとボディを置き換えるかボディのみを置き換えることで動的ページを生成しますが、場合によってはテンプレートのタグの属性を一部のみ置き換えたいこともあるでしょう。その場合、{% proc "m:echo" %}プロセッサを使います。

### 簡単な例

ごく簡単なサンプルで実際に使ってみましょう。`ol` タグの `type` 属性の値を `"A"` から `"i"` に置き換えます。`start` 属性はテンプレートのまま出力されます。

```html {data-filename=echo_list.html}
<html>
<body>
    <ol id="list" type="A" start="3">
        <li>item 1</li>
        <li>item 2</li>
        <li>item 3</li>
        <li>item 4</li>
    </ol>
</body>
</html>
```

```xml {data-filename=echo_list.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:echo m:id="list">
        <m:attribute name="type" value="i" />
    </m:echo>
</m:mayaa>
```

テンプレートの `ol` タグに{% proc "m:echo" %}を対応付け、`m:echo`の子として{% proc "m:attribute" %}を書きます。`m:attribute`の`name`属性に変更したい属性名、
`value`に変更後の属性値を設定します。
`m:attribute`を複数書けば、複数の属性を置き換えられます。
ブラウザで http://localhost:8080/mayaa/echo_list.html にアクセスしてみましょう。

```html
実行結果 (改行などは実際の実行結果と異なります)
<html>
<body>
    <ol id="list" type="i" start="3">
        <li>item 1</li>
        <li>item 2</li>
        <li>item 3</li>
        <li>item 4</li>
    </ol>
</body>
</html>
```

他の属性はそのままに、`type` 属性だけ置き換わりましたね。

### 属性を追加する#attribute

先ほどの例は既存の属性値を変更するものでしたが、{% proc "m:echo" %}プロセッサで新規に属性を追加することもできます。テンプレートのタグで定義されていない属性名を指定すれば、新規に追加することになります。

```html {data-filename=echo_list.html}
<html>
<body>
    <ol id="list" type="A" start="3">
        <li>item 1</li>
        <li>item 2</li>
        <li>item 3</li>
        <li>item 4</li>
    </ol>
</body>
</html>
```

```xml {data-filename=echo_list.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:echo m:id="list">
        <m:attribute name="type" value="i" />
        <m:attribute name="style" value="color: blue;" />
    </m:echo>
</m:mayaa>
```

```html
実行結果 (改行などは実際の実行結果と異なります)
<html>
<body>
    <ol id="list" type="i" start="3" style="color: blue;">
        <li>item 1</li>
        <li>item 2</li>
        <li>item 3</li>
        <li>item 4</li>
    </ol>
</body>
</html>
```

### テンプレートの属性値を利用する

<p>置き換える {% proc "m:echo" %}のスコープ内で、テンプレートの属性名を変数名として使用することで、スクリプトからテンプレートの属性値を参照できます。</p>

```html {data-filename=echo_list.html}
<html>
<body>
    <ol id="list" type="A" start="3">
        <li>item 1</li>
        <li>item 2</li>
        <li>item 3</li>
        <li>item 4</li>
    </ol>
</body>
</html>
```

```xml {data-filename=echo_list.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:echo m:id="list">
        <m:attribute name="type" value="i" />
        <m:attribute name="style" value="color: blue;" />
        <m:attribute name="start" value="${ new Number(start) + 2 }" />
    </m:echo>
</m:mayaa>
```

```html
実行結果 (改行などは実際の実行結果と異なります)
<html>
<body>
    <ol id="list" type="i" start="5" style="color: blue;">
        <li>item 1</li>
        <li>item 2</li>
        <li>item 3</li>
        <li>item 4</li>
    </ol>
</body>
</html>

```
#### 注意点

* テンプレートの属性値は文字列であるため、数値として演算する場合には `Number` に変換する必要があります。
* `class`など、JavaScript の予約語になっている属性名の場合、`page['class']` のように `page` スコープの属性であることを明示する必要があります。
