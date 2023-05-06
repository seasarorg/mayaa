---
layout: base
title: プロセッサをまとめる - withプロセッサ
eleventyNavigation:
  key: プロセッサをまとめる
  subtitle: withプロセッサ
  parent: プロセッサリファレンス
  order: 26
---

## {{ title }}

テンプレート上のタグひとつに対して複数のプロセッサを関連付けたい場合には、プロセッサの中に他のプロセッサを書くことで実現できます。複数のプロセッサをまとめる目的であれば、<a href="/docs/processors/#with">with プロセッサ</a>を使います。


### 簡単な例

ごく簡単なサンプルで実際に使ってみましょう。パラメータ `name` が空でなければ「`hello, (name の値)`」と表示し、`name` が空なら「`no name`」と表示します。

```html {data-filename=with.html}
<html>
<body>
    <span id="message">dummy</span>
</body>
</html>
```

```xml {data-filename=with.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:with m:id="message">
        <m:if test="${ param.name &amp;&amp; param.name.length > 0 }">
            <m:write value="hello," />
        </m:if>
        <m:write value="${ param.name }" default="no name" />
    </m:with>
</m:mayaa>
```

条件判定と {% proc "m:write" %}プロセッサの `default` を併用しています。パラメータ `param.name` 自体がない場合は `param.name` を `boolean` として評価した結果が `false` になります。(JavaScript の `undefined`)
ブラウザで http://localhost:8080/mayaa/with.html?name= にアクセスしてみましょう。

```html
実行結果 (改行などは実際の実行結果と異なります)
<html>
<body>
    no name
</body>
</html>
```

パラメータ `name` の値が空ですので「`hello,`」は出力されず、{% proc "m:write" %}プロセッサの `default` 値である「`no name`」だけが出力されています。

続いて [http://localhost:8080/mayaa/with.html?name=world](http://localhost:8080/mayaa/with.html?name=world) にアクセスしてみましょう。

```html
実行結果 (改行などは実際の実行結果と異なります)
<html>
<body>
    hello, world
</body>
</html>
```

パラメータ `name` の値がありますので「`hello,`」に続いて `name` の値である「world」が出力されています。


<div class="flex flex-nowrap w-full">
<div class="w-1/2 text-left"><a href="../{{ prev.path }}">{{ prev.title }}</a></div>
<div class="w-1/2 text-right right"><a href="../{{ next.path }}">{{ next.title }}</a></div>
</div>
