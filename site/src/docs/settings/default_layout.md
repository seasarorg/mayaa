---
layout: base
title: デフォルトレイアウト
tags: settings
eleventyNavigation:
  key: デフォルトレイアウト
  parent: エンジンの設定
  order: 8
---

## デフォルトレイアウト

<a href="layout.html">レイアウト共有機能</a>を使うためには、対象となるすべての Mayaa ファイルの `m:mayaa` 要素に `m:extends` 属性を書く必要があります。多数のページがあり、全部にこの設定をするのは手間がかかります。また、レイアウトを適用するためだけの Mayaa ファイルが必要になることもあるかもしれません。

全部のページに共通で設定する場合、エンジン設定をすることで `m:extends` 属性を書くことなくレイアウト共有機能を利用できます。明示的に `m:extends` 属性が書かれているページには適用されません。手順は下記の通りです。

1. <a href="#setting">templateBuilder の設定</a>
1. <a href="#defaultlayout">デフォルトレイアウト用のページを作成</a>
1. <a href="#exclude">適用したくないページの定義</a>

### templateBuilder の設定 {#setting}

`templateBuilder` の設定を変更し、`TemplateBuilderImpl` の代わりに `DefaultLayoutTemplateBuilder` を使うようにし、デフォルトレイアウト用の追加パラメータを設定します。


```xml {data-filename=org.seasar.mayaa.provider.ServiceProvider}
※レイアウトの都合で改行しています。
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE provider
    PUBLIC "-//The Seasar Foundation//DTD Mayaa Provider 1.0//EN"
    "http://mayaa.seasar.org/dtd/mayaa-provider_1_0.dtd">
<provider>
    <templateBuilder
            class="org.seasar.mayaa.impl.builder.
                            DefaultLayoutTemplateBuilder">
        (<a href="equals_id_resolver.html">標準の `TemplateBuilderImpl` と同じ部分</a>は省略)
        <parameter name="defaultLayoutPageName"
                             value="/defaultlayout.html"/>
        <parameter name="generateMayaaNode" value="true"/>
    </templateBuilder>
</provider>
```

`DefaultLayoutTemplateBuilder` のデフォルトレイアウト用パラメータは 2 つです。その他は `TemplateBuilderImpl` と同じパラメータを使えます。


`defaultLayoutPageName`
: デフォルトレイアウトとして使用するテンプレートの名前を "/" 始まりで指定します。Mayaa 起動時に指定したファイルが見つからない場合、デフォルトレイアウト機能は有効になりません。デフォルトは "/defaultlayout.html" です。

`generateMayaaNode`
: Mayaa ファイルのない、テンプレートのみのページにもレイアウトを適用するかどうかを設定します。テンプレートのみのページの場合、`doRender` は <a href="/docs/default/">`default.mayaa` で定義します</a>。`true` を設定するとテンプレートのみのページにも適用し、`false` に設定すると適用しません。デフォルトは "`true`" です。

### デフォルトレイアウト用のページを作成 {#defaultlayout}

デフォルトレイアウトを使う場合も、各ページ自体は明示的に<a href="layout.html">レイアウト共有機能</a>を使う場合と同じです。Mayaa ファイルのない、テンプレートのみのページにデフォルトレイアウトを適用する場合には、<a href="/docs/default/">`default.mayaa`</a> で `doRender` プロセッサを定義する形になります。

### 適用したくないページの定義 {#exclude}

明示的にレイアウト定義をしている場合、つまり Mayaa タグに `m:extends` 属性を記述している場合はデフォルトレイアウトは適用されません。

一部のページのみデフォルトレイアウトを使いたくない場合、そのページには明示的に `m:extends` 属性を記述してください。値が空の場合、レイアウト共有機能は無効です。
