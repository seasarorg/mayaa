---
layout: base
title: テンプレートの場所
eleventyNavigation:
  key: テンプレートの場所
  parent: エンジンの設定
  order: 7
---

## テンプレートの場所

<div class="toc">
<a id="toc" name="toc">目次</a>
<ul>
<li><a href="#pageSourceFactory">テンプレートを探すパスを設定</a></li>
<li><a href="#templatePathPattern">テンプレートと見なすファイル / 見なさないファイルを設定</a></li>
</ul>
</div>

<h3><a name="pageSourceFactory"></a>テンプレートを探すパスを設定</h3>

<ファイルを置く場所>「<a href="/docs/settings/">エンジン設定方法</a>」と同様に、PageSourceFactory の設定をすることで<a href="deploy.html">ページを置く場所</a>を追加できます。PageSourceFactory の設定には、次に示す XML ファイルを使います。このファイルは `META-INF` の下に `org.seasar.mayaa.source.PageSourceFactory` という名前で作成します。下図はファイルを置く場所を表しています。
```
WEB-INF/
 +-- classes/
 |    |
 |    +-- META-INF/
 |    |    |
 |    |    +-- org.seasar.mayaa.source.PageSourceFactory
 |    |
 |    +-- (クラスファイルなど)
 |
 +-- lib/
 |
 (省略)
```

#### ■設定ファイル

追加設定する場合、`factory` の子として `parameter` 要素を必要なだけ記述します。


```xml {data-filename=org.seasar.mayaa.source.PageSourceFactory}
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE factory
    PUBLIC "-//The Seasar Foundation//DTD Mayaa Factory 1.0//EN"
    "http://mayaa.seasar.org/dtd/mayaa-factory_1_0.dtd">
<factory>
    <parameter name="folder" value="/WEB-INF/page2"/>
    <parameter name="absolutePath" value="C:/page1"/>
    <parameter name="absolutePath" value="C:/page2"/>
</factory>
```
追加設定したパスは、追加設定した順に、下記の優先順で組み込まれます。

1. コンテキストルートの下
1. /WEB-INF/page の下
1.  **追加したパスの下** 
1. クラスパスルート/META-INFの下


パラメータの指定方法は 2 種類あります。

`folder`
: コンテキストルート下のフォルダを指定します。"/" で始まり "/" なしで終わるよう指定してください。

`absolutePath`
: フォルダの絶対パスを指定します。


#### ■活用例

パラメータの値には `${ ... }` と指定することで環境変数を利用できます。たとえば `absolutePath` を `${ mayaa_path }` と設定しておき、開発者 PC では環境変数を `-Dmayaa_path=C:/page`、運用環境では `-Dmayaa_path=/var/www/html` と定義しておくと、Java のコードと全く別の場所でテンプレートファイルを管理することができます。



### テンプレートと見なすファイル / 見なさないファイルを設定 {#templatePathPattern}

「[エンジン設定方法](/docs/settings/)」と同様に、engine にパラメータを設定をすることでテンプレートと見なすファイル、見なさないファイルを定義できます。( **PageSourceFactory への指定ではないことにご注意ください** )

たとえば、コンテキストルート直下にある HTML ファイルのうち、一部のみ Mayaa に処理させたくない場合などに利用します。


#### ■設定例

```xml {data-filename=org.seasar.mayaa.provider.ServiceProvider}
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE provider
    PUBLIC "-//The Seasar Foundation//DTD Mayaa Provider 1.0//EN"
    "http://mayaa.seasar.org/dtd/mayaa-provider_1_0.dtd">
<provider>
    <engine>
        <parameter name="`notTemplatePathPattern`" value="`.*`"/>
        <parameter name="`templatePathPattern`" value="`.*\.html`"/>
        <parameter name="`notTemplatePathPattern`" value="`/doc/.*\.html`"/>
    </engine>
</provider>
```

パラメータの指定方法は 2 種類あります。これらは複数指定することができ、 **下に指定したものから順番に判定され** 、どれにもマッチしないパスの場合は何も設定しない場合と同様に mime-type で HTML と定義されているものがテンプレートとして判定されます。

`templatePathPattern`
: テンプレートと見なすパターンを正規表現で指定します。

`notTemplatePathPattern`
: テンプレートではないと見なすパターンを正規表現で指定します。

たとえば設定例のとおりに設定した場合、次の優先順で条件を満たすパスがテンプレートとして判定されます。

1. `/doc/.*\.html` にマッチしないこと
2. `.*\.html` にマッチすること

この結果、`/doc/index.html` はテンプレートではない、`/index.html` はテンプレートである、`/help.htm` はテンプレートではない、と判定されます。
