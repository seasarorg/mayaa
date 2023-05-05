---
layout: base
title: 定義済みオブジェクト
eleventyNavigation:
  key: 定義済みオブジェクト
  parent: ドキュメント
  order: 1
---

## {{ title }}


<div class="toc">
<a id="toc" name="toc">目次</a>
<ul>
<li><a href="#scope">スコープ</a>
<ul>
	<li><a href="#application">application: アプリケーションスコープ</a></li>
	<li><a href="#session">session: セッションスコープ</a></li>
	<li><a href="#request">request: リクエストスコープ</a></li>
	<li><a href="#page">page: ページスコープ</a></li>
	<li><a href="#param">param: リクエストパラメータスコープ</a></li>
	<li><a href="#binding">binding: コンポーネント引数スコープ</a></li>
	<li><a href="#header">header: リクエストヘッダスコープ</a></li>
	<li><a href="#default">_: 標準スコープ</a></li>
</ul></li>
<li><a href="#response">response: レスポンス</a></li>
<li><a href="#functions">グローバル関数</a></li>
</ul>
</div>

### 定義済みオブジェクト {#scope}

オブジェクトを格納するスコープは、Mayaa がスクリプトから操作できるオブジェクトとして定義しています。たとえば `request` スコープに格納されている `message` という名前の属性を操作するには２つの簡略表記があります。


* `request.message`
* `request['message']`

どちらも意味的には同じですが、前者は属性としてわかりやすい表記、後者は動的な名前を扱える表記として使い分けられます。属性はスコープによって読み出し専用と書き込み可能の２種類があります。


スコープのメソッドは次のものが用意されています。上記簡略表記は内部的に `getAttribute` と `setAttribute` を使います。


`getAttribute(String name)`
: 指定名の属性をスコープから取得します。スコープ内になければ `null` を返します。

`setAttribute(String name, Object value)`
: 指定名の属性をスコープにセットします。読み込み専用スコープの場合は例外を投げます。(`org.seasar.mayaa.impl.cycle.scope.ScopeNotWritableException`)
`removeAttribute(String name)`
: 指定名の属性をスコープから削除します。読み込み専用スコープの場合は例外を投げます。(`org.seasar.mayaa.impl.cycle.scope.ScopeNotWritableException`)
`isAttributeWritable()`
: スコープが書き込み可能かどうかを `true`/`false` で返します。
`hasAttribute(String name)`
: 指定名の属性がスコープに格納されているかどうかを `true`/`false` で返します。
`iterateAttributeNames()`
: スコープが持つ属性の名前 (String) の Iterator を返します。
`newAttribute(String name, Class attributeClass)`
: 指定名の属性がスコープから取得します。スコープ内にない場合の処理はスコープごとに異なりますが、一般的には attributeClass のインスタンスを生成し、スコープに指定名でセットしてから返します。

Mayaa 標準のスコープは次の 8 つです。

#### ■application: アプリケーションスコープ {#application}

アプリケーション全体で共有されるスコープです。Servlet の `application` スコープにあたります。



#### ■session: セッションスコープ {#session}

セッション全体で共有されるスコープです。Servlet の `session` スコープにあたります。



#### ■request: リクエストスコープ {#request}

リクエスト全体で共有されるスコープです。Servlet の `request` スコープの属性 (`get/setAttribute`) にあたります。

`getContextPath()`
: WEBアプリケーションコンテキストのパス部を返します。<br>("http://localhost/mayaa/index.html"にアクセスされた場合には"/mayaa")

`getRequestedPath()`
: リクエストされたパス文字列を返します。<br>("http://localhost/mayaa/index.html"にアクセスされた場合には"/index.html")

`getPageName()`
: リクエストされたページ名を返します。<br>("http://localhost/mayaa/index.html"にアクセスされた場合には"/index")

`getRequestedSuffix()`
: リクエストで明示されたページ接尾辞を返します。

`getExtension()`
: リクエストされたページ拡張子を返します。<br>("http://localhost/mayaa/index.html"にアクセスされた場合には"html")

`getMimeType()`
: リクエストされたパスより類推できるMIME型を返します。

`getLocales()`
: リクエストのロケール (`java.util.Locale`の配列) を返します。

`getParamValues()`
: リクエストパラメータを配列として持つスコープを取得します。このスコープは、同じ名前のパラメータがひとつのみの場合には長さ 1 の配列を返します。

`getHeaderValues()`
: HTTP ヘッダを配列として持つスコープを取得します。このスコープは、同じ名前の HTTP ヘッダがひとつのみの場合には長さ 1 の配列を返します。


#### ■page: ページスコープ {#page}

ページ内とタグの範囲で共有されるスコープです。JSP の `NESTED` 変数スコープにあたります。`m:beforeRender` で宣言した属性はページの描画が終わるまで有効ですが、`m:exec` や `m:for` などで宣言した属性はそのタグが終わるまでの間のみ有効です。スクリプトの `var` で宣言した変数は `page` スコープの属性として扱われます。



#### ■param: リクエストパラメータスコープ {#param}

リクエスト全体で共有される読み出し専用スコープです。Servlet の `request` スコープのパラメータ (`getParameter`) にあたります。同じ名前のパラメータが複数ある場合には、一番先頭のものひとつを返します。



#### ■binding: コンポーネント引数スコープ {#binding}

コンポーネント引数の読み出し専用スコープです。コンポーネントではない場合には `param` と同じ値を戻します。



#### ■header: リクエストヘッダスコープ {#header}

HTTP ヘッダの読み出し専用スコープです。同じ名前のパラメータが複数ある場合には、一番先頭のものひとつを返します。


#### ■_ (アンダースコア): 標準スコープ {#default}

スコープを指定しない場合と同じ動作をするスコープです。JavaScript の予約語に含まれる名前のパラメータを探索するために使用します。

### response: レスポンス {#response}

ブラウザへ返す情報を扱うオブジェクトです。Servlet の response にあたります。 **特殊な場合を除いて直接操作しないことを推奨します** 。

レスポンスのメソッドのうち、使用する可能性が比較的高いものは以下の通りです。


`setContentType(String contentType)`
: コンテンツタイプ (MIME 型およびエンコーディング情報) を設定します。

`setStatus(int code)`
: HTTP ステータスコードを設定します。(`200`, `404`, `500` など)

`addHeader(String name, String value)`
: レスポンスのヘッダを追加します。

`setHeader(String name, String value)`
: レスポンスのヘッダを設定します。同名のヘッダが設定されている場合は上書きします。

`getOutputStream()`
: 実際の出力となる `OutputStream` を取得します。

`getWriter()`
: バッファされた `Writer` を取得します。

`flush()`
: レスポンスのバッファを強制的に出力します。


### グローバル関数 {#functions}

Mayaa 独自の関数として、forward, redirect, error, load の 4 つ (引数の違うものも含めると 6 つ) が定義されています。これらは Mayaa 上のスクリプトから呼び出せます。

`forward(String path)`
: リクエストを指定したパスに転送します。

`redirect(String path)`
: 指定したパスへのリダイレクト応答を送信します。

`error(int errorCode)`
: 指定した HTTP ステータスコードの HTTP エラー応答を送信します。(`403`, `404`, `500` など)

`error(int errorCode, String message)`
: 指定した HTTP ステータスコード、エラーメッセージの HTTP エラー応答を送信します。

`load(String scriptPath)`
: 指定したパスのスクリプトを読み込み、実行します。文字コードは UTF-8 とします。`exec` プロセッサの `src` 属性を使う場合と同様です。

`load(String scriptPath, String encoding)`
: 指定したパスのスクリプトを指定した文字コードで読み込み、実行します。`exec` プロセッサの `src` 属性、`encoding` 属性を使う場合と同様です。

`throwJava(Throwable t)`
: 引数 `t` の例外を throw します。
