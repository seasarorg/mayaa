---
layout: base
title: Mayaa Coreプロセッサリファレンス
tags: reference
eleventyNavigation:
  key: プロセッサリファレンス
  parent: ドキュメント
  order: 2
---

## {{ title }}

Mayaa 標準で用意しているプロセッサのリファレンスマニュアルです。


    <div class="toc">
    <h3 class="nomargin-top"><a id="toc" name="toc"></a>目次</h3>

#### 出力系
<ul>
<li><a href="#attribute">attribute プロセッサ</a></li>
<li><a href="#comment">comment プロセッサ</a></li>
<li><a href="#echo">echo プロセッサ</a></li>
<li><a href="#element">element プロセッサ</a></li>
<li><a href="#formatDate">formatDate プロセッサ</a></li>
<li><a href="#formatNumber">formatNumber プロセッサ</a></li>
<li><a href="#write">write プロセッサ</a></li>
</ul>

#### 制御系
<ul>
<li><a href="#for">for プロセッサ</a></li>
<li><a href="#forEach">forEach プロセッサ</a></li>
<li><a href="#if">if プロセッサ</a></li>
<li><a href="#with">with プロセッサ</a></li>
</ul>

#### コンポーネント系
<ul>
<li><a href="#doBase">doBase プロセッサ</a></li>
<li><a href="#doBody">doBody プロセッサ</a></li>
<li><a href="#doRender">doRender プロセッサ</a></li>
<li><a href="#insert">insert プロセッサ</a></li>
</ul>

#### その他
<ul>
<li><a href="#exec">exec プロセッサ</a></li>
<li><a href="#ignore">ignore タグ</a></li>
<li><a href="#null">null タグ</a></li>
</ul>
</div>


### 出力系

#### ■attribute プロセッサ {#attribute}

[echo と組み合わせた利用例](/docs/processors/echo/)

echo プロセッサおよび element プロセッサの子として使用し、親プロセッサに属性を追加します。value の実行結果が null の場合はその属性を削除します。
{% proc "write" %}プロセッサのescapeXml, escapeWhitespace相当の変換をしてから出力します。

<table align="center" cellpadding="5" cellspacing="2" class="tbline">
<thead><tr><th nowrap="nowrap"><div>属性名</div></th><th nowrap="nowrap"><div>説明</div></th><th><div>必須</div></th><th><div>値の制限</div></th><th><div>規定値</div></th></tr></thead>
<tbody>
<tr>
  <td>name</td>
  <td>生成する属性名。</td>
  <td><div align="center">○</div></td>
  <td>文字列</td>
  <td></td>
</tr>
<tr>
  <td>value</td>
  <td>生成する属性値。null の場合はその属性自体を削除。</td>
  <td><div align="center">○</div></td>
  <td></td>
  <td></td>
</tr>
<tr>
  <td nowrap="nowrap">escapeAmp ( **※** )</td>
  <td>true の場合、value の<br>&amp;<br>を<br>&amp;amp;<br>に置換する。</td>
  <td></td>
  <td>論理値</td>
  <td>true</td>
</tr>
</tbody>
</table>

#### ■comment プロセッサ  {#comment}
ボディの内容をコメント(`<!--`と`-->`ではさんだ状態)として出力します。
タグのボディが有効です。[利用例](/docs/processors/comment/)

#### ■echo プロセッサ  {#echo}
割り当てられたタグをそのまま出力します。m:echo タグのボディに attribute プロセッサを書くと属性を追加または変更、削除できます。
タグのボディが有効です。[利用例](/docs/processors/echo/)

echo プロセッサの属性およびボディでは、スクリプトでテンプレート側の属性値を参照できます。属性名を変数名とし、属性値を文字列として持っています。たとえば a タグにバインディングした echo で `${ href }` とすれば、元の a タグの href 属性の値を取得できます。

<table align="center" cellpadding="5" cellspacing="2" class="tbline">
<thead><tr><th nowrap="nowrap"><div>属性名</div></th><th nowrap="nowrap"><div>説明</div></th><th><div>必須</div></th><th><div>値の制限</div></th><th><div>規定値</div></th></tr></thead>
<tbody>
<tr>
  <td>name</td>
  <td>出力するタグの要素名。</td>
  <td>&nbsp;</td>
  <td>文字列</td>
  <td>要素名を置き換える場合のみ使用する。</td>
</tr>
</tbody>
</table>

#### ■element プロセッサ  {#element}
`name` で指定したタグを出力します。m:element タグのボディに attribute プロセッサを書くと属性を追加できます。
タグのボディが有効です。

<table align="center" cellpadding="5" cellspacing="2" class="tbline">
<thead><tr><th nowrap="nowrap"><div>属性名</div></th><th nowrap="nowrap"><div>説明</div></th><th><div>必須</div></th><th><div>値の制限</div></th><th><div>規定値</div></th></tr></thead>
<tbody>
<tr>
  <td>name</td>
  <td>生成するタグの要素名。</td>
  <td><div align="center">○</div></td>
  <td>文字列</td>
  <td></td>
</tr>
</tbody>
</table>

#### ■formatDate プロセッサ  {#formatDate}
日付を指定したパターンにフォーマットして出力します。
value に null を渡した場合、default 属性をしているなら default を出力します。そうでない場合は何も出力しません。

<table align="center" cellpadding="5" cellspacing="2" class="tbline">
<thead><tr><th nowrap="nowrap"><div>属性名</div></th><th nowrap="nowrap"><div>説明</div></th><th><div>必須</div></th><th><div>値の制限</div></th><th><div>規定値</div></th></tr></thead>

<tbody>
<tr>
  <td nowrap="nowrap">value</td>
  <td>出力する値。</td>
  <td><div align="center">○</div></td>
  <td>java.util.Date</td>
  <td></td>
</tr>
<tr>
  <td>pattern</td>
  <td>日付をフォーマットするパターン。<br>java.text.SimpleDateFormat のフォーマット書式に準じます。</td>
  <td></td>
  <td>文字列</td>
  <td></td>
</tr>
<tr>
  <td nowrap="nowrap">default</td>
  <td nowrap="nowrap">value が null の場合に出力する値。</td>
  <td></td>
  <td></td>
  <td></td>
</tr>
</tbody>
</table>

#### ■formatNumber プロセッサ  {#formatNumber}
数値を指定したパターンにフォーマットして出力します。value に null を渡した場合、default 属性をしているなら default を出力します。
そうでない場合は何も出力しません。

<table align="center" cellpadding="5" cellspacing="2" class="tbline">
<thead><tr><th nowrap="nowrap"><div>属性名</div></th><th nowrap="nowrap"><div>説明</div></th><th><div>必須</div></th><th><div>値の制限</div></th><th><div>規定値</div></th></tr></thead>

<tbody>
<tr>
  <td nowrap="nowrap">value</td>
  <td>出力する値。</td>
  <td><div align="center">○</div></td>
  <td>数値</td>
  <td></td>
</tr>
<tr>
  <td>pattern</td>
  <td>数値をフォーマットするパターン。<br>java.text.DecimalFormat のフォーマット書式に準じます。</td>
  <td></td>
  <td>文字列</td>
  <td></td>
</tr>
<tr>
  <td nowrap="nowrap">default</td>
  <td nowrap="nowrap">value が null の場合に出力する値。</td>
  <td></td>
  <td></td>
  <td></td>
</tr>
</tbody>
</table>

#### ■write プロセッサ  {#write}
value で指定したオブジェクト、またはタグのボディを文字列として出力します。タグのボディが有効です。
[利用例](/getting-started/various-values/)

* value 属性とタグのボディの両方を記述した場合、value 属性が優先されます。
* value 属性のスクリプトでは、bodyText という変数を利用できます。bodyText はタグに記述したボディの内容を持ちます。ボディを記述していない場合、対象の HTML テンプレートの内容を持ちます。

<table align="center" cellpadding="5" cellspacing="2" class="tbline">
<thead><tr><th nowrap="nowrap"><div>属性名</div></th><th nowrap="nowrap"><div>説明</div></th><th><div>必須</div></th><th><div>値の制限</div></th><th><div>規定値</div></th></tr></thead>

<tbody>
<tr>
  <td nowrap="nowrap">value</td>
  <td>出力する値。<br>ボディまたはvalueのどちらか一方必須。( **※** )</td>
  <td><div align="center">○</div></td>
  <td></td>
  <td></td>
</tr>
<tr>
  <td nowrap="nowrap">default</td>
  <td nowrap="nowrap">value が null の場合に出力する値。<br>(escapeXml, escapeWhitespace, escapeEol<br>による置換処理の対象になりません)</td>
  <td></td>
  <td></td>
  <td></td>
</tr>
<tr>
  <td nowrap="nowrap">escapeXml</td>
  <td>true の場合、value の<br>&amp; , < , > , &quot;<br>を<br>&amp;amp; , &amp;lt; , &amp;gt; , &amp;quot;<br>に置換する。</td>
  <td></td>
  <td>論理値</td>
  <td>true</td>
</tr>
<tr>
  <td nowrap="nowrap">escapeWhitespace</td>
  <td>true の場合、value の<br>CR , LF , TAB文字<br>を<br>&amp;#xd; , &amp;#xa; , &amp;#x9;<br>に置換する。</td>
  <td></td>
  <td>論理値</td>
  <td>false</td>
</tr>
<tr>
  <td nowrap="nowrap">escapeEol</td>
  <td>true の場合、value の改行コード (CRLF, CR, LF) を<br>br タグに置換する。</td>
  <td></td>
  <td>論理値</td>
  <td>true</td>
</tr>
</tbody>
</table>


### 制御系

#### ■for プロセッサ  {#for}
Java の for 文のように繰り返し処理をします。タグのボディが有効です。[利用例](/gettins-started/conditional/)

1. 最初に一度 `init` を実行してループ処理を開始します。
2. 1 回の処理の最初に `test` を実行し、`true` ならループ処理を続行し、`false` ならループ処理を終了します。
3. 1 回の処理の最後に `after` を実行します。


無限ループを避けるため、ループ回数が上限数以上になると例外が発生します。(`org.seasar.maya.impl.engine.processor.TooManyLoopException`)

<table align="center" cellpadding="5" cellspacing="2" class="tbline">
<thead><tr><th nowrap="nowrap"><div>属性名</div></th><th nowrap="nowrap"><div>説明</div></th><th><div>必須</div></th><th><div>値の制限</div></th><th><div>規定値</div></th></tr></thead>
<tbody>
<tr>
  <td nowrap="nowrap">init</td>
  <td>初期化処理。</td>
  <td></td>
  <td></td>
  <td></td>
</tr>
<tr>
  <td nowrap="nowrap">test</td>
  <td>判定する条件式。true ならば処理を続行。</td>
  <td><div align="center">○</div></td>
  <td>論理値</td>
  <td></td>
</tr>
<tr>
  <td nowrap="nowrap">after</td>
  <td>ループ処理 1 回の後処理。</td>
  <td></td>
  <td></td>
  <td></td>
</tr>
<tr>
  <td nowrap="nowrap">max</td>
  <td>ループ回数の上限。</td>
  <td></td>
  <td>整数値</td>
  <td>256</td>
</tr>
</tbody>
</table>

#### ■forEach プロセッサ  {#forEach}
items の値を繰り返し処理します。現在の要素は var 属性で指定した名前の変数に格納され、繰り返し回数は index 属性で指定した名前の変数に格納されます (0 から始まります)。タグのボディが有効です。

items には java.util.Collection、java.util.Iterator、java.util.Enumeration、java.util.Map を実装したものと、配列を使えます。Iterator、Enumeration、配列はそのままの順番で処理します。Collection は iterator メソッドで取得した Iterator の順番で処理します。

Map は entrySet メソッドで取得した Set を利用し、Collection の処理に準じます。そのため Map を処理する場合には、要素は key と value の 2 つのプロパティを持ちます。

items が null の場合は長さ 0 の配列として処理します。以上のもの以外を items に渡すと、長さ 1 の配列として処理します。

<table align="center" cellpadding="5" cellspacing="2" class="tbline">
<thead><tr><th nowrap="nowrap"><div>属性名</div></th><th nowrap="nowrap"><div>説明</div></th><th><div>必須</div></th><th><div>値の制限</div></th><th><div>規定値</div></th></tr></thead>
<tbody>
<tr>
  <td nowrap="nowrap">items</td>
  <td>繰り返し処理する値。</td>
  <td><div align="center">○</div></td>
  <td>java.util.Collection,<br>java.util.Iterator,<br>java.util.Enumeration,<br>java.util.Map,<br>配列</td>
  <td></td>
</tr>
<tr>
  <td nowrap="nowrap">var</td>
  <td>繰り返しの現在要素の変数名。</td>
  <td><div align="center">○</div></td>
  <td>文字列</td>
  <td></td>
</tr>
<tr>
  <td nowrap="nowrap">index</td>
  <td>繰り返し回数の変数名。(0 開始)</td>
  <td></td>
  <td>文字列</td>
  <td></td>
</tr>
</tbody>
</table>

#### ■if プロセッサ  {#if}
`test` の値が `true` の場合のみボディを処理します。
タグのボディが有効です。[利用例](/getting-started/conditional/)

<table align="center" cellpadding="5" cellspacing="2" class="tbline">
<thead><tr><th nowrap="nowrap"><div>属性名</div></th><th nowrap="nowrap"><div>説明</div></th><th><div>必須</div></th><th><div>値の制限</div></th><th><div>規定値</div></th></tr></thead>
<tbody>
<tr>
  <td nowrap="nowrap">test</td>
  <td nowrap="nowrap">判定する条件式。true ならば有効。</td>
  <td><div align="center">○</div></td>
  <td>論理値</td>
  <td></td>
</tr>
</tbody>
</table>

#### ■with プロセッサ  {#with}
with プロセッサ自体は何もしません。複数のプロセッサをグルーピングするために使います。
タグのボディが有効です。[利用例](/docs/processors/with/)


### コンポーネント系

#### ■doBase プロセッサ  {#doBase}

##### コンポーネントで使う場合
コンポーネントを使う元テンプレートのタグのボディを処理します。

##### レイアウトを利用するテンプレートで使う場合
レイアウトの insert のタグのボディを処理します。

#### ■doBody プロセッサ  {#doBody}
**通常は利用しません。**

割り当てられたタグのボディを処理します。for プロセッサなどタグのボディが有効なプロセッサで動作します。
通常は明示的に使用することはありませんが、Mayaa ファイル側のボディに attribute 以外のプロセッサや
空白 (タブ文字、改行を含む) 以外の文字列を記述した場合、doBody を使用しなければタグのボディが処理されません。


#### ■doRender プロセッサ  {#doRender}
##### コンポーネントを定義する場合
割り当てられたタグをコンポーネントとして定義します。名前は同じテンプレート上で重複しないように付けてください。
[利用例](/docs/component1/)

<table align="center" cellpadding="5" cellspacing="2" class="tbline">
<thead><tr><th nowrap="nowrap"><div>属性名</div></th><th nowrap="nowrap"><div>説明</div></th><th><div>必須</div></th><th><div>値の制限</div></th><th><div>規定値</div></th></tr></thead>
<tbody>
<tr>
  <td>name</td>
  <td>コンポーネントの名前。</td>
  <td></td>
  <td>文字列</td>
  <td>"" (空文字列)</td>
</tr>
</tbody>
</table>

##### レイアウトを利用する場合
レイアウトの insert に渡す部分を定義します。
[利用例](/docs/layout/)

<table align="center" cellpadding="5" cellspacing="2" class="tbline">
<thead><tr><th nowrap="nowrap"><div>属性名</div></th><th nowrap="nowrap"><div>説明</div></th><th><div>必須</div></th><th><div>値の制限</div></th><th><div>規定値</div></th></tr></thead>
<tbody>
<tr>
  <td>name</td>
  <td>レイアウトの insert 名。</td>
  <td></td>
  <td>文字列</td>
  <td>"" (空文字列)</td>
</tr>
</tbody>
</table>

#### ■insert プロセッサ  {#insert}

##### コンポーネントを使う場合
指定したパスのコンポーネントを割り当てます。
[利用例](/docs/component1/)

<p>パスはスクリプトで指定することもできます。( **※** )
<p>( **※** :1.1.8 で追加されたもの)
<table align="center" cellpadding="5" cellspacing="2" class="tbline">
<thead><tr><th nowrap="nowrap"><div>属性名</div></th><th nowrap="nowrap"><div>説明</div></th><th><div>必須</div></th><th><div>値の制限</div></th><th><div>規定値</div></th></tr></thead>
<tbody>
<tr>
  <td>path</td>
  <td>コンポーネントのパス。</td>
  <td><div align="center">○</div></td>
  <td>( **※**  1.1.7 以前は文字列)</td>
  <td></td>
</tr>
<tr>
  <td>name</td>
  <td>コンポーネントの名前。</td>
  <td></td>
  <td>文字列</td>
  <td>"" (空文字列)</td>
</tr>
</tbody>
</table>

##### レイアウトを定義する場合
extends しているテンプレートの名前が一致する doRender を割り当てます。
[利用例](/docs/layout/)

<table align="center" cellpadding="5" cellspacing="2" class="tbline">
<thead><tr><th nowrap="nowrap"><div>属性名</div></th><th nowrap="nowrap"><div>説明</div></th><th><div>必須</div></th><th><div>値の制限</div></th><th><div>規定値</div></th></tr></thead>
<tbody>
<tr>
  <td>name</td>
  <td>レイアウトの insert 名。</td>
  <td></td>
  <td>文字列</td>
  <td>"" (空文字列)</td>
</tr>
</tbody>
</table>


### その他

#### ■exec プロセッサ  {#exec}
スクリプトを実行するために使います。script、srcの両方に指定された場合、srcから先に実行されます。
[利用例](/docs/processors/exec/)

exec プロセッサで実行したスクリプトによって宣言された変数は、exec プロセッサと同じ階層のスコープで有効です。(1.1.9 以前の src 属性、および 1.1.0-beta6 以前の script 属性では、exec プロセッサの内側階層のスコープでのみ有効でした)

<table width="90%" align="center" cellpadding="5" cellspacing="2" class="tbline">
<thead><tr><th nowrap="nowrap"><div>属性名</div></th><th nowrap="nowrap"><div>説明</div></th><th><div>必須</div></th><th><div>値の制限</div></th><th><div>規定値</div></th></tr></thead>
<tbody>
<tr>
  <td>script</td>
  <td>実行するスクリプト。`${}` 表記で記述します。</td>
  <td></td>
  <td></td>
  <td></td>
</tr>
<tr>
  <td>src</td>
  <td>実行する外部 JavaScript ファイルのパス。コンテキストルートからの絶対パス、Mayaa ファイルからの相対パスで記述できます。</td>
  <td></td>
  <td>文字列</td>
  <td></td>
</tr>
<tr>
  <td>encoding</td>
  <td>src で指定したファイルを読み込むときの<br>文字エンコーディング。</td>
  <td></td>
  <td>Java文字セット名</td>
  <td>file.encoding<br>or UTF-8</td>
</tr>
</tbody>
</table>

#### ■ignore タグ  {#ignore}
割り当てた id を処理対象外にします。(ignore はプロセッサではなくエンジン機能ですが、利用の仕方に違いはありません)
[利用例](/docs/processors/remove/)


#### ■null タグ  {#null}
何も出力しません。(null はプロセッサではなくエンジン機能ですが、利用の仕方に違いはありません)
[利用例](/docs/processors/remove/)
