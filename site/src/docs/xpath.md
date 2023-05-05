---
layout: base
title: idの代わりにXPathで関連付け
eleventyNavigation:
  key: idの代わりにXPathで関連付け
  subtitle: XPathリゾルバ
  parent: 高度な使い方
  order: 9
---

## id の代わりに XPath

テンプレートのタグとプロセッサを関連付けるには id を使いますが、その代替手段として XPath を使う方法があります。使うには mayaa ファイルで m:id="テンプレートの id" と書く代わりに `m:xpath="XPath ロケーションパス"` と書きます。XPath に関する説明や参考資料はこのページの最後にあります。
`m:id` 同様、`m:xpath` を付けるプロセッサは `<m:mayaa>` タグの子として書かれているプロセッサに限定されます。(※1.1.10 以降)

### 簡単な例

テンプレートで `id` 属性を持たないタグのボディを置き換えてみましょう。XPath のロケーションパスで `h1` タグを指定します。

```html {data-filename=xpath.html}
<html>
<body>
    <h1>Dummy</h1>
    <p>content</p>
</body>
</html>
```

```xml {data-filename=xpath.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:write m:xpath="/html/body/h1" value="Title" replace="false" />
</m:mayaa>
```

ルートはそのまま HTML のルートですので、 "`/html/body/h1`" というロケーションパスは `h1` タグを指します。
ブラウザで http://localhost:8080/mayaa/xpath.html にアクセスしてみましょう。

```html
実行結果 (改行などは実際の実行結果と異なります)
<html>
<body>
    <h1>Title</h1>
    <p>content</p>
</body>
</html>
```

テンプレートに `id` がなくても置き換わっているのがわかります。


### 複数のタグを一度に指定する

XPath では、複数のタグにマッチするロケーションパスを指定できます。複数のタグにマッチした場合、マッチしたすべてのタグがプロセッサに対応付けられます。
次の例では XPath のロケーションパスに "`//a`" を指定しています。このパスはすべての `a` タグにマッチしますので、すべての `a` タグに `target="_blank"` を追加します。

```html {data-filename=xpath_multi.html}
<html>
<body>
    <ul>
        <li><a href="path1.html">link1</a></li>
        <li><a href="path2.html">link2</a></li>
        <li><a href="path3.html">link3</a></li>
    </ul>
</body>
</html>
```

```xml {data-filename=xpath_multi.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:echo m:xpath="//a">
        <m:attribute name="target" value="_blank" />
    </m:echo>
</m:mayaa>
```

ブラウザで http://localhost:8080/mayaa/xpath_multi.html にアクセスしてみましょう。

```html
実行結果 (改行などは実際の実行結果と異なります)
<html>
<body>
    <ul>
        <li><a href="path1.html" target="_blank">link1</a></li>
        <li><a href="path2.html" target="_blank">link2</a></li>
        <li><a href="path3.html" target="_blank">link3</a></li>
    </ul>
</body>
</html>
```

指定したロケーションパスにマッチするもの、つまりすべての `a` タグに `target="_blank"` が追加されているのが分かります。

### XPath

XPath は XML 文書からノードを選択するための簡潔な言語で、「ロケーションパス」という表記を使ってノードを指定します。"/" 区切りでノードを選択する表記はロケーションパスの簡略表記です。Java2 SDK 5.0 から XPath を評価するための API (javax.xml.xpath) が提供されます。
XPath については参考文献をご覧ください。


### 参考文献 {#reference}

[XML Path Language (XPath)](http://www.w3.org/TR/xpath)<br>
[http://www.w3.org/TR/xpath](http://www.w3.org/TR/xpath)<br>
日本語訳: http://www.infoteria.com/jp/contents/xml-data/REC-xpath-19991116-jpn.htm
