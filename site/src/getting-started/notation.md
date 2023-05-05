---
layout: base
title: Mayaaファイルの記述方法
eleventyNavigation:
  key: Mayaaファイル
  subtitle: 設定の記述方法
  parent: Getting Started
  order: 2
tags: tutorial
---

## {{ title }}

Mayaa の設定を記述する方法は大きく分けて 2 種類あります。ひとつはこれまでのサンプルコードのように mayaa ファイルを使う方法、もうひとつはテンプレートの HTML ファイルに直接設定を書き込む方法です。また、この 2 種類の方法を混在させることもできます。


### mayaa ファイルを使う

Mayaa の基本的な利用方法は、テンプレートに対応する mayaa ファイルを用意する方法です。テンプレートを純粋な HTML のままにしたい場合はこの形を使います。


<!-- テンプレート切り替え機能の話は別 -->
mayaa ファイルはテンプレート HTML ファイルの拡張子を `.mayaa` に変えた XML ファイルです。テンプレートにつきひとつの mayaa ファイルを用意し、動的な処理を設定します。


![図 2-5-1: テンプレートと mayaa ファイルからページを生成する](/images/about_mayaa_standard.gif)

記述方法は、前提知識として XML 名前空間を知っていると理解しやすくなります。XML 名前空間に関する説明や参考資料はこのページの最後にあります。ここでは Mayaa の機能を表すプレフィクスとして "`m:`" を使って説明します。



#### ■mayaa ファイルのタグはプロセッサ

mayaa ファイルにタグとして書く、Mayaa の様々なテンプレート処理機能のことをプロセッサと呼びます。たとえば「{% proc "write" %}プロセッサ」を使うなら `<m:write>`タグを書き、「{% proc "if" %}プロセッサ」を使うなら `<m:if>` タグを書きます。


#### ■HTML タグにプロセッサを割り当てる

動的な出力を行うには、基本的には HTML タグに id 属性を付け、mayaa ファイルでその id を指定してプロセッサと対応させます。デフォルトでは id を指定した元のタグとそのボディ (タグで囲まれた範囲) を消し、mayaa ファイルで定義したプロセッサの出力によって置き換えます。プロセッサのタグに `replace="false"` と属性を付けた場合、元のタグを消さずにボディだけをプロセッサの出力によって置き換えます。

「置き換える」という点に注意してください。たとえば図 2-5-2 のテンプレートには、id 属性を持つタグが 3 つあります。


![図 2-5-2: id でテンプレート上の要素を識別する](/images/mayaa_file1.gif)

このテンプレートに対し図 2-5-3 の mayaa ファイルを合わせて実行すると、実際に処理されるのは (A)(1) と (B)(2) のみになります。これは (B) が置き換えられた結果、(B) に内包されている (C) が無くなってしまうためです。


![図 2-5-3: テンプレート上の要素にプロセッサを設定する](/images/mayaa_file2.gif)

```html
図 2-5-2 のテンプレートを図 2-5-3 の mayaa ファイルと合わせた場合の実行結果
<html>
<body>
    # dynamic title

    dynamic contents
</body>
</html>
```

(C) を有効にするには、(B) にボディ処理できるプロセッサを対応させます。ボディ処理できるプロセッサには、たとえば `if` があります。どのプロセッサがボディ処理できるかは<a href="/docs/processors/">プロセッサ リファレンス</a>を参照してください。

また、Mayaa ファイルの `m:id` を付けるプロセッサは、`<m:mayaa>`タグの子として書かれているプロセッサに限定されます。(※1.1.10 以降)

{% proc "m:if" %}の子に `m:id` を書いても有効にならない。
また、`m:id` を書いていない {% proc "m:if" %}も有効にならない。
```xml
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:if test="${ foo == bar }">
        <m:write m:id="message" value="Hello Mayaa!" />
    </m:if>
</m:mayaa>
```

<h4>■<a id="mayaaid" name="mayaaid"></a>バインディングに HTML の id を使いたくない場合</h4>

HTML の id 属性は CSS や JavaScript などから利用することもあります。mayaa ファイルとのバインディングに id 属性を使いたくない場合には、`m:id` として mayaa 名前空間の id を使うことができます。その場合、mayaa ファイルに書いていた Mayaa の名前空間指定 (`xmlns:m="http://mayaa.seasar.org"`) を、テンプレートの html タグの属性として書く必要があります。

`m:id` 属性と id 属性の両方がある場合、mayaa ファイルとのバインディングには `m:id` 属性が優先されます。また、`m:id` 属性は実行時に出力されません。

また、HTML の id を完全に無視したい場合、5-6.「<a href="equals_id_resolver.html">id 属性を無視する</a>」に従って設定してください。

```html {data-filename=hello.html}
<html xmlns:m="http://mayaa.seasar.org">
<body>
    <span m:id="message" id="forCss">dummy message</span>
</body>
</html>
```
```xml {data-filename=hello.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:write m:id="message" value="Hello Mayaa!" replace="false" />
</m:mayaa>
```

### ページを置く場所

Mayaa はリクエストされたファイルを次の順番で探します。(例: http://localhost:8080/mayaa/index.html にアクセスされた場合)

1. コンテキストルートの下 (例: C:\tomcat\webapps\mayaa\index.html)
2. /WEB-INF/page の下 (例: C:\tomcat\webapps\mayaa\WEB-INF\page\index.html)
3. クラスパス (例: C:\tomcat\webapps\mayaa\WEB-INF\classes\index.html)
4. 2, 3 番目は、Mayaa を通さなければアクセスできない場所ですので、MayaaServlet の設定を間違えている場合や MayaaServlet のみ異常終了した場合に、直接テンプレートの HTML ファイルを表示してしまうことを防げます。

[設定でこのパスを追加できます。](/docs/settings/template_path/)

### テンプレート上に設定を書く

Mayaa は、mayaa ファイルではなくテンプレート上に設定を書くこともできます。後述するレイアウト共有機能など一部の機能は mayaa ファイルを使わなければ実現できませんが、簡単な処理の場合などにファイル数が多くなるのを避けることができます。


#### ■HTML タグにプロセッサを割り当てる

mayaa ファイルを使う場合は、id によるマッピングで HTML タグにプロセッサを割り当てます。テンプレートに直接書く場合は、その代わりとして `m:inject` 属性を使います。また、mayaa ファイルに書いていた名前空間指定 (`xmlns:m="http://mayaa.seasar.org"`) を、テンプレートの html タグの属性として書く必要があります。

`m:inject` 属性の値にはマッピングするプロセッサ名 (mayaa ファイルに書く場合のタグ名) を書き、プロセッサの属性は mayaa ファイルと同様に書きます。ただし、プロセッサの属性のプレフィクスを省略することはできません。(名前空間とプレフィクスについてはこのページの最後で説明しています)

例として {% proc "m:write" %}(hello.html) と `c:out` (hello_jstl.html) のサンプルを、テンプレート上に設定を書く形で書き換えてみます。特に `c:out` のサンプルに注意してください。`value` は `c:out` の属性であるため、プレフィクスとして `c:` が付きます。

```html
hello.htmlでテンプレート上に設定を書いた場合
<html xmlns:m="http://mayaa.seasar.org">
<body>
    <span m:inject="m:write" m:value="Hello Mayaa!">dummy message</span>
</body>
</html>
```

```html
hello_jstl.html でテンプレート上に設定を書いた場合
<html xmlns:m="http://mayaa.seasar.org"
      xmlns:c="http://java.sun.com/jsp/jstl/core">
<body>
    <span m:inject="c:out" c:value="Hello Mayaa!">dummy message</span>
</body>
</html>
```

### mayaa ファイルとテンプレート上の設定の両方を使う

mayaa ファイルを使う方法とテンプレート上に書く方法の 2 種類を説明しましたが、これらの方法を混在させることもできます。



```html
hello.html で id 指定とテンプレート上の設定の両方を使う
<html xmlns:m="http://mayaa.seasar.org">
<body>
    <div id="condition">
    <span m:inject="m:write" m:value="Hello Mayaa!">dummy message</span>
    </div>
</body>
</html>
```
```xml {data-filename=hello.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org">
    <m:if m:id="condition" test="${ 1 == 1 }" />
</m:mayaa>
```

同じタグにテンプレート上の指定、mayaa ファイルでの指定の両方を書いた場合、テンプレート上の指定が優先されます。

### 名前空間とプレフィクス

Mayaa の設定ファイルは XML 形式で、プロセッサをタグとして記述します。Mayaa のエンジン機能と JSP カスタムタグなどの機能名が重複しないよう、XML 名前空間を利用して区別します。ここでは XML 名前空間についての説明を行いません。また、簡単のため厳密ではない表現をすることがあります。XML 名前空間については<a href="#reference">参考文献</a>をご覧ください。

Mayaa の機能で推奨するプレフィクスは次の通りです。


|機能名|URL|推奨プレフィクス|
|-----|---|-------------|
|Mayaa のエンジン機能|http://mayaa.seasar.org|m|

JSP カスタムタグを利用する場合、JSP の taglib ディレクティブで指定するプレフィクスと URI を、XML 名前空間のプレフィクス、URL として指定します。


属性の名前空間が属しているタグと同じ場合はプレフィクスを省略できます。下の例で青色・斜体になっているプレフィクスが省略可能なものです。これまでのサンプルは省略した形で表記していましたが、厳密な記述をするときにはこの例のようになります。


```xml
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org"
        xmlns:c="http://java.sun.com/jsp/jstl/core">
    <m:if <span class="omit">m:</span>id="role" <span class="omit">m:</span>test="${ user.isManager() }" >
        <m:write <span class="omit">m:</span>value="manager" />
    </m:if>

    <c:out m:id="message" <span class="omit">c:</span>value="Hello Mayaa!" />
</m:mayaa>
```

### 参考文献 {#reference}

Namespaces in XML
: [http://www.w3.org/TR/REC-xml-names](http://www.w3.org/TR/REC-xml-names)
  
XML名前空間の簡単な説明
: [http://www.kanzaki.com/docs/sw/names.html](http://www.kanzaki.com/docs/sw/names.html)
