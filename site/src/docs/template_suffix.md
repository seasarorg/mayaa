---
layout: base
title: テンプレートサフィックスによる切り替え
eleventyNavigation:
  key: テンプレートサフィックス
  subtitle: 多言語対応などテンプレートの切り替え
  parent: 高度な使い方
  order: 9
---

## テンプレート切り替え

Mayaa では、ひとつの URL で複数のテンプレートを使いわけることができます。たとえば、利用者の言語にあわせたテンプレートを使う、というように利用できます。

### 複数のファイルを用意する

まず、同じ URL でアクセスするテンプレートを、必要な数だけ用意します。同じ URL として扱うために、テンプレートのファイル名を "$" で区切って種類を表現します。

たとえば /use_suffix.html という URL で、英語のページを "en"、日本語のページを "ja" とするとき、次のファイルを用意します。

* use_suffix$en.html
* use_suffix$ja.html

### 種類を指定する

次に、どのファイルを使うかを指定します。`m:mayaa` に  **m:templateSuffix**  属性を書き、その値として種類 (en や ja) を文字列として指定します。スクリプトを書く場合、その結果が文字列になるようにしてください。

下の例ではパラメータとして `"locale"` を渡し、その値を使って切り替えます。

```html {data-filename=use_suffix$en.html}
<html>
<body>
    in English.

</body>
</html>
```

```html {data-filename=use_suffix$ja.html}
<html>
<body>
    in Japanese.

</body>
</html>
```

```xml {data-filename=use_suffix.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org"
        m:templateSuffix="${ param.locale }">
</m:mayaa>
```

ブラウザで http://localhost:8080/mayaa/use_suffix.html?locale=en にアクセスしてみましょう。「in English.」と表示されますね。次に http://localhost:8080/mayaa/use_suffix.html?locale=ja にアクセスしてみましょう。今度は「in Japanese.」と表示されますね。

```html
実行結果 (改行などは実際の実行結果と異なります)
use_suffix.html?locale=en の場合
<html>
<body>
    in English.

</body>
</html>
```
```html
use_suffix.html?locale=ja の場合
<html>
<body>
    in Japanese.

</body>
</html>
```

### 指定した種類が存在しない場合

**m:templateSuffix** 属性で指定したテンプレートファイルが存在しない場合、デフォルトのテンプレートが使われます。デフォルトのテンプレートは、$ とその後ろがない、単純なファイル名のものです。

デフォルトのテンプレート "use_suffix.html" を用意して、locale に存在しない文字列を指定してみましょう。


```html {data-filename=use_suffix.html}
<html>
<body>
    Default.

</body>
</html>
```

ブラウザで http://localhost:8080/mayaa/use_suffix.html?locale=aaa にアクセスしてみましょう。「Default.」と表示されますね。

```html
実行結果 (改行などは実際の実行結果と異なります)
use_suffix.html?locale=aaa の場合
<html>
<body>
    Default.

</body>
</html>
```
