---
layout: base
title: 条件分岐とループ
eleventyNavigation:
  key: 条件分岐とループ
  parent: Getting Started
  order: 2
tags: tutorial
---

## 条件分岐とループ

### 条件分岐を使う

条件を満たす場合のみ出力するサンプルを作ります。`if.html` と、その設定を行う `if.mayaa` を次のように作成します。

```html {data-filename=src/main/webapp/if.html}
<html>
<body>
    <span id="visible">
        <span id="message">dummy message</span>
    </span>
</body>
</html>
```

```xml {data-filename=/src/main/webapp/if.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:if m:id="visible" test="${ 1 == 1 }" />

    <m:write m:id="message" value="Hello Mayaa!" />
</m:mayaa>
```

動作は JavaScript の if 文と同じです。mayaa ファイルにある{% proc "m:if" %}の `test` を評価すると `true` になりますので、ボディの `span id="message"` が評価されて "Hello Mayaa!" と出力されます。

{% proc "m:if" %}と {% proc "m:write" %}は別々に書かれていることに注意してください。HTML 側では `id="message"` のエレメントが `id="visible"` のエレメントの子要素として書かれていますが、Mayaa 側は `m:id` を持つエレメントが同列に並ぶように書きます。</p>

ブラウザで http://localhost:8080/if.html にアクセスしてみましょう。

```html
実行結果 (改行などは実際の実行結果と異なります)
<html>
<body>

        Hello Mayaa!

</body>
</html>
```

test の内容が `false` になるよう変更すると、"Hello Mayaa!" は表示されません。
```xml {data-filename=if.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:if m:id="visible" test="${ 1 &lt; 1 }" />

    <m:write m:id="message" value="Hello Mayaa!" />
</m:mayaa>
```

```html
実行結果 (改行などは実際の実行結果と異なります)
<html>
<body>



</body>
</html>
```

条件判定 `test` の式で比較演算子 `<` を `&lt;` と記述している点に注意してください。Mayaaファイル内ではスクリプトを XML の属性として記述しますので、一部の記号をエスケープする必要があります(`<` → `&lt;` `>` → `&gt;` `&` → `&amp;`)。
なお、`beforeRender`ではCDATAで囲むことでエスケープする必要はなくなります。

### ループを使う {#for}

一部分を繰り返し出力するサンプルを作ります。`for.html` と、その設定を行う `for.mayaa` を次のように作成します。


```html {data-filename=src/main/webapp/for.html}
<html>
<body>
    <span id="loop">
        <span id="message">dummy message</span>
    </span>
</body>
</html>
```

```xml {data-filename=src/main/webapp/for.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:for m:id="loop"
            init="${ var i = 0 }"
            test="${ i &lt; 5 }"
            after="${ i++ }" />

    <m:write m:id="message"
            value="Hello Mayaa!&lt;br&gt;" escapeXml="false" />
</m:mayaa>
```

動作は JavaScript の for 文と同じです。Mayaaファイルにある{% proc "m:for" %}は、最初に一度だけ `init` を実行します。その後、`test` を評価して結果が `true` になるならボディを実行し、最後に `after` を実行してから `test` の評価に戻ります。

実行結果を見やすくするため、"`Hello Mayaa!`" の後ろに`<br>`を付け、タグをそのまま出力するよう[escapeXml属性](/docs/processors/#write) を`false`に設定しています。この時も`value`内で`&lt;br&gt;`と記載しています。

ブラウザで http://localhost:8080/for.html にアクセスしてみましょう。

```html
実行結果 (改行などは実際の実行結果と異なります)
<html>
<body>
    Hello Mayaa!<br>
    Hello Mayaa!<br>
    Hello Mayaa!<br>
    Hello Mayaa!<br>
    Hello Mayaa!<br>
</body>
</html>
```

結果として "`Hello Mayaa<br>`" が 5 回出力されます。

初期化部 `init` で宣言した変数 `i` は、{% proc "m:for" %}の範囲内、つまり `span id="loop"` を処理する間で有効です。
`m:write` を使って `i` の値を出力してみましょう。

```html {data-filename=for.html}
<html>
<body>
    <span id="loop">
        <span id="message">dummy message</span>
    </span>
</body>
</html>
```

```xml {data-filename=for.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:for m:id="loop"
            init="${ var i = 0 }"
            test="${ i &lt; 5 }"
            after="${ i++ }" />

    <m:write m:id="message"
            value="${ i }&lt;br&gt;" escapeXml="false" />
</m:mayaa>
```

```html
実行結果 (改行などは実際の実行結果と異なります)
<html>
<body>
    0<br>
    1<br>
    2<br>
    3<br>
    4<br>
</body>
</html>
```

変数 `i` の値が 1 ずつ増えていくのがわかります。
