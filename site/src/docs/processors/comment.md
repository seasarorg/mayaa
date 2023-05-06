---
layout: base
title: コメントを書く - commentプロセッサ
eleventyNavigation:
  key: コメントを書く
  subtitle: commentプロセッサ
  parent: プロセッサリファレンス
  order: 21
tags: reference
prev:
  path: ignore
  title: 特定idを処理しない
next:
  path: template_suffix
  title: テンプレート切り替え
---

## {{ title }}

Mayaa の「コメント」には２種類のコメントがあります。テンプレートの一部であるコメントと、mayaa ファイルに書くコメントです。

### テンプレートの一部であるコメント

テンプレートの一部であるコメントとは、テンプレートに HTML コメントの形式 (`<!-- -->`) で記述したコメントのことで、処理結果として出力されます。このコメントの内側に `id` を持つタグがあっても無視されます。ただし `${ }` の形式でスクリプトが書かれている場合、それは処理対象になります。
mayaa ファイルから HTML コメントを出力したい場合、{% proc "m:comment" %}プロセッサを使います。


### 簡単な例

ごく簡単なサンプルで実際に使ってみましょう。サンプル中の `id` は `inComment` と `commentOut` がありますが、`inComment` のタグは HTML コメントの内部にあります。


```html {data-filename=comment.html}
<html>
<body>
    <!--
        <span id="inComment">${ commentValue }</span>
    -->

    <span id="commentOut">hello</span>
</body>
</html>
```

```xml {data-filename=comment.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:beforeRender>
        var commentValue = "script in comment";
    </m:beforeRender>

    <m:write m:id="inComment" value="comment" />

    <m:comment m:id="commentOut">
        <m:write value="comment out by mayaa " />
        <m:doBody />
    </m:comment>
</m:mayaa>
```

ブラウザで http://localhost:8080/mayaa/comment.html にアクセスしてみましょう。

```html
実行結果 (改行などは実際の実行結果と異なります)
<html>
<body>
    <!--
        <span id="inComment">script in comment</span>
    -->

    <!--comment out by mayaa hello-->
</body>
</html>
```

コメント内にある `id="inComment"` は処理されず、そのボディにあった${ commentValue } が変数 `commentValue` の内容に置き換わっています。また、`id="commentOut"` が {% proc "m:comment" %}プロセッサによってコメントアウトされています。このように、コメント内に動的な値を出したい場合にも対応できます。


### mayaa ファイルに書くコメント

mayaa ファイルに XML コメントの形式 (`<!-- -->`) で記述したコメントは、mayaa の処理対象外になります。つまりこちらは一般的なコメントの扱いになります。
