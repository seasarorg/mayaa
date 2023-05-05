---
layout: base
title: サイト更新履歴
eleventyNavigation:
  key: サイト更新履歴
  parent: News
---
## {{ title }}

### 2011/03/27 {#u20110327}
* 「[デフォルトレイアウト](/docs/settings/)」のクラス名が誤っていたのを修正しました。

### 2009/06/07 {#u20090607}
* 「[プロセッサリファレンス](/docs/processors/)」の [attribute](/docs/processors/#attribute) の説明に、escapeAmp属性の説明を追記しました。(1.1.25 からの機能です)
* 「[エンジン設定方法](/docs/settings/)」の `TemplateBuilder` のパラメータ説明に `replaceSSIInclude` を追加しました。
* 3-13.「[SSI include の置き換え](/docs/settings/include/)」の説明を追加しました。

### 2008/12/25 {#u20081225}
* 3-12.「[例外発生時のページ](docs/throwable/)」に 1.1.23 以降のエラーページ debug モードについての説明を追加しました。

### 2008/11/22 {#u20081122}
* 「[エンジン設定方法](/docs/settings/)」に `TemplateBuilder` のパラメータについての説明を追加しました。
* A-4.「IBM_JDKの注意点」を追加しました。

### 2008/03/13 {#u20080313}
* 5-7.「[テンプレートの場所](/docs/settings/template_path/)」でパスの説明が誤っていたのを修正しました。「クラスパスの下」ではなく、正しくは「クラスパスルート/META-INFの下」です。


### 2008/03/02 {#u20080302}
* 2-1.「[最初の一歩](hello.html)」にファイル更新時の再ビルドについての記述を追加しました。

### 2007/08/19 {#u20070819}
* 5-9.「[パス自動調整の設定](path_adjust_settings.html)」を追加しました。

### 2007/08/05 {#u20070805}
* 「[エンジン設定方法](/docs/settings/)」に設定項目"`forwardLimit`"の説明を追加しました。
* 「[エンジン設定方法](/docs/settings/)」に設定項目"`convertCharset`"の説明を追加しました。
* Appendix 3.「[その他の注意点](note.html#charset)」の文字エンコーディングの項に設定項目"`convertCharset`"の利用についての説明を追加しました。

### 2007/05/25 {#u20070525}
* 各ページ下部で前後のページへリンクを張りました。

### 2007/05/23 {#u20070523}
* 5-8.「[デフォルトレイアウト](default_layout.html)」を追加しました。(1.1.10 新機能)
* 3-3.「[タグ描画時のスクリプト](exec.html)」の説明に、スコープについての説明を追記しました。(1.1.10 より仕様変更があります)
* 「[プロセッサリファレンス](/docs/processors/)」の [exec](/docs/processors/#exec) の説明に、スコープについての説明を追記しました。(1.1.10 より仕様変更があります)
* Appendix 3.「[その他の注意点](note.html)」の 「[出力する文字エンコーディングを指定したい](note.html#charset)」 の説明を、1.1.3 での変更に合わせて変更しました。(無指定の場合のエンコーディングを `UTF-8` 統一)
* 説明文の中に出てくるプロセッサ名からリファレンスへリンクを張りました。
* 2-4. 「[設定の記述方法](notation.html)」に `m:id` は `<m:mayaa>`の子要素でのみ有効になることを追記しました。
* 「[id の代わりに XPath](xpath.html)」に `m:xpath` は `<m:mayaa>`の子要素でのみ有効になることを追記しました。

### 2007/04/30 {#u20070430}
* Appendix 2.「[JavaScript を書くときの Tips](mayaa_rhino_tips.html)」の「[変数宣言時の注意](mayaa_rhino_tips.html#var)」に1.1.9のリリース内容をふまえて追記しました。
### 2007/04/21 {#u20070421}
* 5-7.「[テンプレートの場所](/docs/settings/template_path/)」を追加しました。
* Appendix 2.「[JavaScript を書くときの Tips](mayaa_rhino_tips.html)」に「[変数宣言時の注意](mayaa_rhino_tips.html#var)」を追加しました。

### 2007/04/08 {#u20070408}
* 「[プロセッサリファレンス](/docs/processors/)」の [write](/docs/processors/#write) の説明に、ボディを使用する方法を追記しました。
* 「[プロセッサリファレンス](/docs/processors/)」の [insert](/docs/processors/#insert) の説明に、パスをスクリプトで指定可能ということを追記しました。
* appendix E「[定義済みオブジェクト](/docs/defined-objects/.html)」に標準スコープ `_` (アンダースコア) の説明を追加しました。

### 2007/01/14 {#u20070114}
* 3-12.「[例外発生時のページ](docs/throwable/)」を追加しました。
* 5-5.「[エラー処理の設定](error_handler.html)」を追加しました。
* 5-6.「[id属性を無視する](equals_id_resolver.html)」を追加しました。
* 「[定義済みオブジェクト](/docs/defined-objects/.html)」にグローバル関数 `throwJava` の説明を追加しました。

### 2006/09/27 {#u20060927}
* 「[エンジン設定方法](/docs/settings/)」に設定項目"requestedSuffixEnabled"の説明を追加しました。

### 2006/08/13 {#u20060813}
* 「[プロセッサリファレンス](/docs/processors/)」の [echo](/docs/processors/#echo) の説明に、テンプレートの属性を参照する方法を追記しました。

### 2006/08/06 {#u20060806}
* 「[エンジン設定方法](/docs/settings/)」に設定項目の説明を追加しました。
* 5-3.「[自動ビルド](auto_build.html)」を追加しました。
* 5-4.「[ビルド結果キャッシュ](page_serialize.html)」を追加しました。

### 2006/08/01 {#u20060801}
* 「[定義済みオブジェクト](/docs/defined-objects/.html)」のrequestスコープにあるgetParamValuesとgetHeaderValuesの記述を修正しました。これらのメソッドはスコープを返します。

### 2006/07/30 {#u20060730}
* 「[プロセッサリファレンス](/docs/processors/)」に1.0.6 (1.1.0-beta6) で追加した機能についての説明を追記しました。([echo](/docs/processors/#echo) の name 属性追加、[attribute](/docs/processors/#attribute) の属性削除機能)

### 2006/05/28 {#u20060528}
* 「[プロセッサリファレンス](/docs/processors/)」に1.0.5 (1.1.0-beta5) で追加した機能についての説明を追記しました。([formatNumber](/docs/processors/#formatNumber), [formatDate](/docs/processors/#formatDate) の default 属性追加)

### 2006/05/06 {#u20060506}
* 「[プロセッサリファレンス](/docs/processors/)」からそのプロセッサを使用しているページへリンクを張りました。

### 2006/04/09 {#u20060409}
* 「[プロセッサリファレンス](/docs/processors/)」に [comment](/docs/processors/#comment) についての説明を追加しました。

### 2006/04/08 {#u20060408}
* 「[定義済みオブジェクト](/docs/defined-objects/.html)」にrequestスコープのみのメソッドを追記しました。

### 2006/03/19 {#u20060319}
* 「[定義済みオブジェクト](/docs/defined-objects/.html)」でrequestスコープのメソッドgetParamValuesとgetHeaderValuesを標準スコープとして記述していた誤りを修正しました。

### 2006/02/26 {#u20060226}
* 「[プロセッサリファレンス](/docs/processors/)」の [write](/docs/processors/#write) プロセッサの default 属性仕様変更について追記しました。
* 「[エンジン設定方法](/docs/settings/)」を追加しました。
* 5-2.「[属性自動セット](jsp_template_attribute.html)」を追加しました。

### 2006/01/23 {#u20060123}
* 「[定義済みオブジェクト](/docs/defined-objects/.html)」にグローバル関数の説明を追加しました。
* 「[外部へのリンク](link.html)」を追加しました。

### 2006/01/16 {#u20060116}
* 「[プロセッサリファレンス](/docs/processors/)」に RC1 で追加された [forEach](/docs/processors/#forEach)、[formatDate](/docs/processors/#formatDate)、[formatNumber](/docs/processors/#formatNumber) についての説明を追加しました。
* 「プロセッサリファレンス」を分類し、アルファベット順に並び替えました。

### 2006/01/07 {#u20060107}
* 3-11.「[パスを自動調整する](path_adjust.html)」を追加しました。
* 「[プロセッサリファレンス](/docs/processors/)」の [write](/docs/processors/#write) について、仕様変更の内容を反映しました。

### 2005/12/14 {#u20051214}
* 3-4.「[属性のみを置き換える](echo.html)」の最後に、タグの属性名が JavaScript の予約語と競合する場合の対応を追記しました。

### 2005/12/08 {#u20051208}
* 「[プロセッサリファレンス](/docs/processors/)」の [for](/docs/processors/#for), [exec](/docs/processors/#exec) について規定値が誤っていたのを修正しました。
* 3-10.「[テンプレート切り替え](/docs/template_suffix/)」を追加しました。

### 2005/12/07 {#u20051207}
* サイトリニューアル時に 「[プロセッサリファレンス](/docs/processors/)」が古くなっていたのを修正しました。

### 2005/11/28 {#u20051128}
* サイトリニューアルに合わせて変更しました。

### 2005/11/16 {#u20051116}
* Appendix 1.「[カスタムタグの注意点](mayaa_customtag_diff.html)」からスクリプティング変数の違いについての記述を削除しました。(0.9.20 の修正に対応)

### 2005/11/14 {#u20051114}
* 1-1.「[Mayaa とは何か](about.html)」を追加しました。Mayaa の立ち位置、概要などです。
* 1-3.「[どのように使うのか](usebean.html)」を追加しました。チュートリアルへ進む前に Mayaa を使う雰囲気を見るページです。

### 2005/11/11 {#u20051111}
* 3-4.「[タグの属性のみを置き換える](echo.html)」で eval を使っていたところを Number で説明するよう変更しました。
* 3-5.「[ひとつのタグに複数のプロセッサを対応付ける](with.html)」で eval を使っていたところを Number で説明するよう変更しました。
* 3-5.「[ひとつのタグに複数のプロセッサを対応付ける](with.html)」の param 利用例を改変しました。

### 2005/11/10 {#u20051110}
* 「[スクリプトで使える定義済みオブジェクト](/docs/defined-objects/.html)」を追加しました。

### 2005/10/29 {#u20051029}
* Appendix 3.「[その他の注意点](note.html)」の文字エンコーディングについての記述を修正しました。

### 2005/10/28 {#u20051028}
* Appendix 1.「[カスタムタグの注意点](mayaa_customtag_diff.html)」を追加しました。
* 「[Mayaa core プロセッサ リファレンス](/docs/processors/)」を追加しました。
* Appendix 2.「[JavaScript Tips](mayaa_rhino_tips.html)」を追加しました。
* Appendix 3.「[その他の注意点](note.html)」を追加しました。

### 2005/10/25 {#u20051025}
* 項目調整のため 3 章の節番号をずらしました。
* 3-1 「すべてのページで共通の設定をする」を追加しました。
* 3-2 「描画前後にスクリプトを実行する」を追加しました。
* 3-3 「タグ描画のタイミングでスクリプトを実行する」を追加しました。
* 3-4 「タグの属性のみを置き換える」を追加しました。
* 3-5 「ひとつのタグに複数のプロセッサを対応付ける」を追加しました。
* 3-6 「もうひとつのコンポーネント記述方法」を追加しました。
* 3-7 「id 指定の代わりに XPath を使う」を追加しました。
* 3-8 「指定したタグを出力しないようにする」を追加しました。
* 3-9 「指定した id を処理対象外にする」を追加しました。
* 3-10 「コメントを書く」を追加しました。

### 2005/10/04 {#u20051004}
* 「rendered="true"」が「replace="false"」に変更されたことに対応して変更しました。

### 2005/09/30 {#u20050930}
* 2-8 のレイアウト一箇所の例で name 属性を使うよう変更しました。
* 2-7 の doBase の説明に追加しました。
* 3-3 にコンポーネント指定簡略表記の注意点を追加しました。
* 3-4 「タグの属性のみを置き換える」を追加しました。(作成中)
* 3-5 「id 指定の代わりに XPath を使う」を追加しました。(作成中)

### 2005/09/28 {#u20050928}
* 0.9.14 に合わせ、2-1 のパラメータを参照する説明を "param.パラメータ名" で参照するよう変更しました。合わせてサンプルも変更しました。
* 0.9.14 に合わせ、2-7 のコンポーネントに変数を渡す説明を "binding.変数名" で参照するよう変更しました。合わせてサンプルも変更しました。


<div class="navi">[↑このページのトップへ](#top)</div>
<div class="navi-prev"></div>
<div class="navi-next"></div>
<div class="navi-end">&nbsp;</div>
