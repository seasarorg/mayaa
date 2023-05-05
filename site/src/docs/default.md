---
layout: base
title: ページ共通の設定
eleventyNavigation:
  key: ページ共通の設定 
  subtitle: default.mayaa
  parent: 高度な使い方
  order: 11
---

## {{ title }}

すべてのページで共通の設定をしたい内容がある場合、それぞれのページごとに設定していくのは大変です。そのような場合には、デフォルト設定を使うことで簡単に設定できます。

### すべてのページで共通の設定をする

デフォルト設定はコンテキストルートにある「default.mayaa」という名前のファイルで行います。このファイルが無い場合にはデフォルト設定は行われません。
次の例はメッセージと共にシステム時刻を表示します。システム時刻は `hello_with_time.html` の currentTime という `id` に関連付けますが、`hello_with_time.mayaa` には対応する設定がありません。その代わり `default.mayaa` に設定されています。

```html {data-filename=hello_with_time.html}
<html>
<body>
    <span id="currentTime">dummy time</span>
    <br>
    <span id="message">dummy message</span>
</body>
</html>
```
```xml {data-filename=hello_with_time.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:write m:id="message" value="Hello Mayaa!" />
</m:mayaa>
```
```xml {data-filename=default.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:write m:id="currentTime"
            value="${ new java.util.Date().toString() }" />
</m:mayaa>
```

ブラウザで http://localhost:8080/mayaa/hello_with_time.html にアクセスしてみましょう。

```html
実行結果 (改行などは実際の実行結果と異なります)
<html>
<body>
    Thu Oct 20 20:01:38 JST 2005
    <br>
    Hello Mayaa!
</body>
</html>
```

実行結果は、`default.mayaa` で設定したとおりにシステム時刻が出力されています。



### デフォルト設定を個別設定で上書きする

デフォルト設定を使いたくない特殊なページを作ることがあるでしょう。その場合には、ページの設定で上書きをすることができます。


```html {data-filename=hello_with_time.html}
<html>
<body>
    <span id="currentTime">dummy time</span>
    <br>
    <span id="message">dummy message</span>
</body>
</html>
```

```xml {data-filename=hello_with_time.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:write m:id="message" value="Hello Mayaa!" />

    <m:write m:id="currentTime" value="Now" />
</m:mayaa>
```

```xml {data-filename=default.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:write m:id="currentTime"
            value="${ new java.util.Date().toString() }" />
</m:mayaa>
```

```html
実行結果 (改行などは実際の実行結果と異なります)
<html>
<body>
    Now
    <br>
    Hello Mayaa!
</body>
</html>
```

デフォルト (`default.mayaa`) の設定ではなく `hello_with_time.mayaa` の設定が優先されていることがわかります。
