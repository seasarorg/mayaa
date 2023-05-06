---
layout: base
title: タグを消す - removeプロセッサ
eleventyNavigation:
  key: タグを消す
  subtitle: removeプロセッサ
  parent: プロセッサリファレンス
  order: 25
---

## {{ title }}

テンプレートを直接ブラウザでプレビューするときのためのダミーを、実行時に自動的に非表示にしたい場合があるでしょう。その場合は{% proc "m:null" %}プロセッサもしくは **rendered 属性** を使います。`m:rendered` 属性に `false` をセットした場合、プロセッサとして{% proc "m:null" %}を指定したものとして扱います。


### 簡単な例

ごく簡単なサンプルで実際に使ってみましょう。テンプレートにリスト (`ul`) があり、リスト要素 (`li`) の先頭のみを使う場合を考えて見ましょう。


```html {data-filename=remove.html}
<html>
<body>
    <ul>
        <span id="loop">
            <li><span id="message">dummy message</span></li>
        </span>
        <li class="dummy">dummy 1</li>
        <li class="dummy">dummy 2</li>
        <li class="dummy">dummy 3</li>
    </ul>
</body>
</html>
```

```xml {data-filename=remove.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:for m:id="loop"
            init="${ var i = 0; }"
            test="${ i &lt; 5 }"
            after="${ i += 1 }" />

    <m:write m:id="message" value="${ i }" />

    <m:null m:xpath="//li[@class='dummy']" />
</m:mayaa>
```

実行時に出力したくないタグに `class="dummy"` を設定し、XPath を使って{% proc "m:null" %}プロセッサを対応させています。

ブラウザで http://localhost:8080/mayaa/remove.html にアクセスしてみましょう。

```html
実行結果 (改行などは実際の実行結果と異なります)
<html>
<body>
    <ul>
        <li>0</li>
        <li>1</li>
        <li>2</li>
        <li>3</li>
        <li>4</li>
    </ul>
</body>
</html>
```

ダミーの要素は出力されず、ループして出力するよう設定したもののみが出力されていることがわかります。

### 使い分け

出力しない設定をする方法は`null`プロセッサと `m:rendered` 属性の 2 種類があります。基本的に mayaaファイルに設定を記述する場合は`null`プロセッサを使い、テンプレートに設定を記述する場合は `m:rendered` 属性を使います。テンプレートに記述する場合は、純粋な HTML でなくなってしまうことと、複数要素を一括して指定する方法がないというデメリットがあります。
