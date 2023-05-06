---
layout: base
title: タグ描画時のスクリプト - execプロセッサ
eleventyNavigation:
  key: タグ描画時のスクリプト
  subtitle: execプロセッサ
  parent: プロセッサリファレンス
  order: 23
tags: processor
prev:
  path: before_render
  title: 描画前後のスクリプト
next:
  path: echo
  title: 属性のみを置き換える
---

## {{ title }}

ループごとに少しだけ準備処理をするなど、タグを描画するタイミングでスクリプトを実行したい場合があるでしょう。その場合には[execプロセッサ](/docs/processor/exec/)を使います。
`exec`プロセッサには JavaScriptファイルのパスを指定する `src`属性と、スクリプトを `${}` 表記でそのまま記述する `script` 属性があります。

### 簡単な例

ごく簡単なサンプルで実際に使ってみましょう。{% proc "m:exec" %}プロセッサでループ変数が偶数か奇数かを判定し、変数 `evenodd` に格納します。
さらに{% proc "m:write" %}プロセッサを使ってループ変数の値と判定結果を出力します。

```html {data-filename=exec.html}
<html>
<body>
    <div id="loop">
        <div id="evenodd"><span id="value">dummy</span></div>
    </div>
</body>
</html>
```

```xml {data-filename=exec.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">

    <m:for m:id="loop" init="${ var i = 1 }"
            test="${ i &lt; 6 }" after="${ i += 1 }" />

    <m:exec m:id="evenodd" replace="false"
        script="${ var evenodd = (i % 2 == 0) ? 'even' : 'odd'; }" />

    <m:write m:id="value" value="${ i + ': ' + evenodd }" />

</m:mayaa>
```

変数 `evenodd` は <a href="/docs/processors/#exec">`exec` プロセッサ</a>と同じ階層でのみ有効です。`id="value"` の span タグは <a href="/docs/processors/#exec">`exec` プロセッサ</a>のボディ内にある (内側の階層にある) と見なせますので、`evenodd` 変数を参照することができます。(※:1.1.0-beta5 以前では、<a href="/docs/processors/#exec">`exec` プロセッサ</a>のボディ内でのみ有効でした)
ブラウザで http://localhost:8080/mayaa/exec.html にアクセスしてみましょう。

```html
実行結果
<html>
<body>
    <div id="evenodd">1: odd</div>
    <div id="evenodd">2: even</div>
    <div id="evenodd">3: odd</div>
    <div id="evenodd">4: even</div>
    <div id="evenodd">5: odd</div>
</body>
</html>
```
実行結果には、正しく `evenodd` 変数の内容が出力されました。


このサンプルだけで見ると意味がないように見えるかもしれませんが、たとえば配列の要素や計算結果を変数で定義しなおして、設定ファイルの見通しを良くするような使い方に効果を発揮します。


実用場面を考えてみましょう。request に入っているオブジェクトの深い位置へアクセスするには、`object.getProperty().getChildren()[1].getValue()` のように長い文でアクセスをすることになってしまいます。JavaScript の機能を使って短縮しても `object.property.children[1].value` でやはり分かりづらいです。

このようなときに `var child = object.property.children[1]` と変数宣言すれば、`child.value`、`child.name` のように分かりやすくプロパティを参照できます。
