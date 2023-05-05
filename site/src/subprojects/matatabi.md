---
layout: base
title: matatabi
eleventyNavigation:
  key: matatabi)
  title: matatabi(終了)
  parent: 関連プロジェクト
  order: 1
---
:::note alert
すでに公開を終了しています。<br>
最終バージョン： ver 1.2.7 (2014/2/20)
:::

#### matatabiとは

matatabiはMayaaファイルやHTMLファイルの編集を支援するEclipseプラグインです。
動作にはEclipse3.3以上とWTP2.0以上が必要となります。

(公開を終了しました)

最終バージョン： ver 1.2.7 (2014/2/20)

ソースコードは Githubにて公開されています。
https://github.com/seasarorg/mayaa-matatabi

#### 機能
<ul>
	<li>エディタ機能
		<ul>
			<li>Mayaaで用意されているプロセッサの入力補完</li>
			<li>HTML,Mayaaファイルでのid属性の値の補完</li>
			<li>Mayaaファイルでのタグライブラリの補完</li>
			<li>Mayaaファイルでのタグライブラリの名前空間の補完</li>
			<li>HTML,Mayaaファイルでのm:id属性入力支援</li>
			<li>HTMLファイルでm:id付きのspanタグ入力支援</li>
			<li>HTML,MayaaファイルとActionクラスの切り替え</li>
			<li>HTMLファイルを外部ブラウザで開く</li>
			<li>Mayaaファイルの静的チェック</li>
		</ul>
	</li>
	<li>自動生成機能
		<ul>
			<li>Mayaaファイルの雛形生成機能</li>
			<li>既存のMayaaファイルへの雛形追加機能</li>
		</ul>
	</li>
</ul>

<h4>利用方法</h4>
<p>同梱のヘルプを参照してください</p>

<h4>変更履歴</h4>
<h5><a id="matatabi_ver1.2.7" name="matatabi_ver1.2.7"></a>変更点 1.2.6 -> 1.2.7 (2009/04/16)</h5>
<ul>
<li class="fix">xhtmlでデフォルト名前空間がある場合にエラーが出ていたのを修正しました</li>
</ul>

<h5><a id="matatabi_ver1.2.6" name="matatabi_ver1.2.6"></a>変更点 1.2.5 -> 1.2.6 (2008/09/23)</h5>
<ul>
<li class="other">Eclipse3.4とWTP3.0の組み合わせでも動作するようにしました</li>
</ul>

<h5><a id="matatabi_ver1.2.5" name="matatabi_ver1.2.5"></a>変更点 1.2.4 -> 1.2.5 (2008/07/27)</h5>
<ul>
<li class="fix">Javaプロジェクトでプロジェクト設定ページが表示されていないのを修正しました</li>
<li class="fix">JavaSE6でコンパイルされていたのをJavaSE5でコンパイルするように修正しました</li>
</ul>

<h5><a id="matatabi_ver1.2.4" name="matatabi_ver1.2.4"></a>変更点 1.2.3 -> 1.2.4 (2008/07/02)</h5>
<ul>
<li class="fix">プロジェクト以外のプロパティ画面でMatatabiの設定ページが表示されていたのを修正しました</li>
<li class="fix">HTMLファイルのid属性値の補完でdefault.mayaaに定義しているidが表示されていなかったのを修正しました</li>
<li class="fix">HTMLファイルのid属性値の補完がMayaaの名前空間でしか動いていなかったのを修正しました</li>
<li class="fix">HTMLファイルのタグ入力補完で設定にかかわらず大文字が使用されていたのを修正しました</li>
</ul>

<h5><a id="matatabi_ver1.2.3" name="matatabi_ver1.2.3"></a>変更点 1.2.2 -> 1.2.3 (2008/04/06)</h5>
<ul>
<li class="enhance">エディタ切り替え機能でActionクラスが無い場合に新規作成出来るようにしました</li>
<li class="enhance">エディタ切り替え機能でアンダースコアやハイフンを使ったファイル名に対応しました</li>
<li class="other">右クリックメニューを廃止しました</li>
</ul>

<h5><a id="matatabi_ver1.2.2" name="matatabi_ver1.2.2"></a>変更点 1.2.1 -> 1.2.2 (2008/03/16)</h5>
<ul>
<li class="enhance">自動生成の雛形設定で処理対象のタグの指定方法をXPathで行うように変更しました</li>
</ul>

<h5><a id="matatabi_ver1.2.1" name="matatabi_ver1.2.1"></a>変更点 1.2.0 -> 1.2.1 (2008/02/16)</h5>
<ul>
<li class="enhance">[MAYAA-44] Actionクラスのパッケージ（サフィックス）を指定できるようにしました</li>
<li class="fix">[MAYAA-43] Mayaaエディターで編集メニューが使用できなくなっていたのを修正しました</li>
<li class="fix">[MAYAA-45] Webルートパスの下にディレクトリがあった場合にHTMLとMayaa間の移動ができなかったのを修正しました</li>
<li class="fix">メニューの項目や設定を整理しました</li>
</ul>

<h5><a id="matatabi_ver1.2.0" name="matatabi_ver1.2.0"></a>変更点 1.1.1 -> 1.2.0 (2007/09/12)</h5>
<ul>
<li class="enhance">HTMLファイルでのm:idの属性値の補完に対応しました</li>
<li class="enhance">HTML,Mayaaファイルでのm:id属性の入力を支援する機能を追加しました</li>
<li class="enhance">HTMLファイルでのm:id属性付きspanタグの入力を支援する機能を追加しました</li>
<li class="enhance"><a href="../documentation//docs/template_suffix/">テンプレート切り替え機能</a>に対応しました</li>
<li class="enhance">mayaaタグに対する属性の補完に対応しました</li>
<li class="enhance">Validator機能のON/OFFを設定ページに追加しました</li>
<li class="enhance">テンプレートファイルのm:idのみを処理対象にする設定を設定ページに追加しました</li>
<li class="fix">バリデータでエラーになった箇所はそれ以降のバリデーションをしないように修正しました</li>
<li class="fix">バリデータのエラーレベルを「エラー」にした際、設定値が消えていたのを修正しました</li>
<li class="fix">フルビルドの際にエラーマーカーを消していなかったのを修正しました</li>
</ul>

<h5><a id="matatabi_ver1.1.1" name="matatabi_ver1.1.1"></a>変更点 1.1.0 -> 1.1.1 (2007/07/09)</h5>
<ul>
<li class="enhance">HTMLエディタと同じ機能をWTPのWebPageEditorで使用できるようにしました</li>
<li class="fix">HTMLのid属性の取得方法が誤っていたのを修正しました</li>
<li class="fix">プロジェクト直下のファイルの場合に補完機能でエラーが出ていたのを修正しました</li>
</ul>

<h5><a id="matatabi_ver1.1.0" name="matatabi_ver1.1.0"></a>変更点 1.0.0 -> 1.1.0 (2007/07/05)</h5>
<ul>
<li class="enhance">HTML,MayaaファイルとActionクラスのエディタ切り替え機能を追加しました</li>
<li class="fix">タグライブラリの名前空間の入力補完を変更しました</li>
<li class="fix">編集途中のMayaaファイルで入力補完を行う際にエラーが発生する場合があったため修正しました</li>
</ul>

<h5><a id="matatabi_ver1.0.0" name="matatabi_ver1.0.0"></a>変更点 0.5.0 -> 1.0.0 (2007/07/03)</h5>
<ul>
<li class="enhance">タグライブラリの入力補完機能を追加しました</li>
<li class="enhance">タグライブラリの名前空間の入力補完機能を追加しました</li>
</ul>

<h5><a id="matatabi_ver0.5.0" name="matatabi_ver0.5.0"></a>変更点 0.4.0 -> 0.5.0 (2007/05/28)</h5>
<ul>
<li class="enhance">自動生成するMayaaファイルの名前空間を指定できるようにしました</li>
<li class="enhance">自動生成するMayaaファイルの変換ルールを指定できるようにしました</li>
<li class="fix">default.mayaaファイルがない場合に自動生成機能でエラーが出ていたのを修正しました</li>
<li class="fix">プロジェクト直下のファイルの場合に自動生成機能でエラーが出ていたのを修正しました</li>
<li class="fix">属性値の補完機能が動いていなかったのを修正しました</li>
<li class="fix">HTMLファイルを開く機能が動いていなかったのを修正しました</li>
</ul>

<h5><a id="matatabi_ver0.4.0" name="matatabi_ver0.4.0"></a>変更点 0.3.0 -> 0.4.0 (2007/05/24)</h5>
<ul>
<li class="enhance">プロジェクトごとにmatatabiを使用するか選べるようにしました</li>
<li class="enhance">Mayaaファイルの静的チェック機能を追加しました</li>
<li class="enhance">Mayaaファイルを自動生成する際に、確認のダイアログを表示するようにしました</li>
</ul>

