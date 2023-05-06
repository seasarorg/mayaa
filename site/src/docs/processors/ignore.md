---
layout: base
title: 特定idを処理しない - ignoreプロセッサ
eleventyNavigation:
  key: 特定 id を処理しない
  subtitle: ignoreプロセッサ
  parent: プロセッサリファレンス
  order: 24
tags: processor
prev:
  path: remove
  title: タグを消す
next:
  path: comment
  title: コメントを書く
---

## {{ title }}

CSS や JavaScript から使うために `id` 属性を定義しても、Mayaa はそれをプロセッサに対応付けようとします。同じ要素を Mayaa で処理する場合には <a href="notation.html#mayaaid" title="設定の記述方法">`m:id` を使えば良い</a>のですが、Mayaa で処理したくない要素の場合もあるでしょう。
テンプレート上の `id` を無視する場合には、<a href="/docs/processors/#ignore">`ignore` タグ</a>を使ってその `id` を無視指定します。xpath 指定はできません。
mayaa ファイルに対応する指定がない場合には、初回アクセス時のログに次のような警告が出力されます。

※htmlのidを常に無視したい場合は 5-6「<a href="equals_id_resolver.html">id 属性を無視する</a>」をご覧ください。

```
the injection id([対象 id の値]) is not found on the template, file://[URL]
```

### 簡単な例

ごく簡単なサンプルで実際に使ってみましょう。テンプレートにリスト (`ul`) があり、リスト要素 (`li`) の先頭のみを使う場合を考えて見ましょう。


```html {data-filename=ignore.html}
<html>
<body>
    <span id="notfound">not found</span>
    <span id="ignored">ignored</span>
</body>
</html>
```

```xml {data-filename=ignore.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:ignore m:id="ignored" />
</m:mayaa>
```

ブラウザで http://localhost:8080/mayaa/ignore.html にアクセスしてみましょう。

```html
実行結果
<html>
<body>
    <span id="notfound">not found</span>
    <span id="ignored">ignored</span>
</body>
</html>
```

何の処理も行いませんので、テンプレートがそのまま出力されているのがわかります。Tomcat のログに前述の警告が出ていることを確認してください。
「notfound」は警告されますが、無視指定している「ignored」の警告は出ていませんね。
