---
layout: base
title: 自動ビルド
eleventyNavigation:
  key: 自動ビルド
  parent: エンジンの設定
  order: 3
---

## 自動ビルド

標準設定の Mayaa は、ページへ最初にアクセスされたときに、そのページのビルド処理 (<a href="#note1"> ※ </a>) をおこないます。この処理にはある程度の時間がかかるため、最初にアクセスしたときとそれ以降では画面が表示されるまでの時間に大きな差ができてしまいます。初回アクセス時に時間がかかるということが好ましくない場合もあるでしょう。

自動ビルド機能は、このビルド処理を初回アクセス時ではなくアプリケーション起動時に自動的におこなう機能です。自動ビルド処理は Web コンテナを起動してアプリケーションがロードされたあと、別スレッドで実行されます。自動ビルド対象となるファイルを検索し、それらのファイルに対して順番にビルド処理をおこないます。

自動ビルド処理を実行しているときはすでに通常どおりアクセスできる状態になっているため、まだビルドされていないページへアクセスがあった場合には通常のビルド処理の後でページが描画されることになります。

ページをビルドしたときの警告メッセージやエラーメッセージは、自動ビルド時も通常どおりログに出力されます。また、ビルドにかかった時間もログに出力されます。


<a id="note1" name="note1"></a> ※  ビルド処理：HTML ファイル、Mayaa ファイルをパースして描画可能な状態にすること

### 自動ビルドを有効にする

自動ビルドを有効にするには、<a href="/docs/settings/">ServiceProvider</a> ファイルで設定します。標準の設定は下記のようになっています。"`autoBuild`" パラメータを "`true`" に設定することで自動ビルドが有効になります。

```xml {data-filename=org.seasar.mayaa.provider.ServiceProvider}
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE provider
    PUBLIC "-//The Seasar Foundation//DTD Mayaa Provider 1.0//EN"
    "http://mayaa.seasar.org/dtd/mayaa-provider_1_0.dtd">
<provider>
    <engine>
        <parameter name="`autoBuild`" value="`false`"/>
        <parameter name="`autoBuild.repeat`" value="`false`"/>
        <parameter name="`autoBuild.wait`" value="`60`"/>
        <parameter name="`autoBuild.fileNameFilters`" value="`.html`"/>
        <parameter name="`autoBuild.renderMate`" value="`false`"/>
    </engine>
</provider>
```

#### ■設定項目について

`autoBuild`
: 自動ビルドを有効にするかどうかを設定します。"`true`" なら有効、"`false`" なら無効です。デフォルトは "`false`" です。

`autoBuild.repeat`
: 自動ビルドを定期的に実行するかどうかを設定します。"`true`" なら定期的に実行、"`false`" なら初回のみ実行します。デフォルトは "`false`" です。

`autoBuild.wait`
: 自動ビルドを定期的に実行するときの間隔を秒数で設定します。デフォルトは "`60`" です。対象となる全てのページをビルドしたあと、指定した秒数待機します。

`autoBuild.fileNameFilters`
: 自動ビルド対象とするファイルを選択します。デフォルトは "`.html`" です。セミコロン (;) で区切ることで複数の条件を指定できます。条件の指定方法は２種類あり、拡張子指定と正規表現指定が使えます。ピリオドで始まる英数字 (大文字小文字を区別しない) の場合は拡張子判定、それ以外は正規表現と見なします。正規表現はファイル名のみとマッチングし、マッチした場合に処理対象とします。(例: "`.html;^(sample|howto)_.+\.htm$`")

`autoBuild.renderMate`
: 自動ビルドした後で描画をするかどうかを設定します。"`true`" なら有効、"`false`" なら無効です。デフォルトは "`false`" です。有効にした場合、描画にかかった時間をログに出力します。事前に Servlet などでの処理が必要なページの場合は描画エラーになりますのでご注意ください。
