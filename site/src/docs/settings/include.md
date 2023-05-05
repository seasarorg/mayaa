---
layout: base
title: SSI includeの置き換え
eleventyNavigation:
  key: SSI includeの置き換え
  parent: エンジンの設定
  order: 13
---


## {{ title }}

[TemplateBuilder](/docs/settings/#templatebuilder)のパラメータ `replaceSSIInclude` を true に設定することで、SSI Include の記述を insert">`insert` プロセッサ</a>に置き換える機能を有効にできます。
SSI Include を Mayaa を通さずに動作させるには、Dreamweaver の Include が有効なバージョンか、または Apache などの Web サーバを通して閲覧します。デザイナーによるデザイン時に SSI Include を使うことで、コンポーネント部分を別ファイルにしたまま作業を進められます。

この置き換え機能は、おおまかに次の２つの機能に分かれます。

* SSI Include の記述を `insert` プロセッサに置き換える。
* include される側のページ全体を無名の{% proc "m:doRender" %}プロセッサで囲む。

### SSI Include の記述と変換

SSI Include は、例えば以下のような形で記述します。パス指定の属性には `file` と `virtual` があり、それぞれ挙動の異なるものですが、Mayaa では同等に扱います。

```html
<!--#include virtual="include.html" -->
```
特定の記述をすることで、`insert` プロセッサの属性に値を指定することもできます。例えば次のような記述をしたとします。

```html
<!--#include virtual="include$suffix.html?foo=bar&amp;bar=#fragment" -->
```


このとき、次のような記述をしたように変換されます。

```html
<m:insert
    path="include.html"
    name="fragment"
    auto:foo="bar"
    auto:bar="" />
```

* パスの suffix は無視されます。<br># の後ろに書いた fragment は name の指定と見なします。
* パラメータはそのままパラメータになります。(名前空間は独自のものを使います)

なお、include 対象のファイルはコンポーネントとなるため、全体をひとつのタグで囲んでいる必要があり、また `doRender` プロセッサで囲まれた領域を持つ必要があります。この制限を緩和するのが、自動的に `doRender` プロセッサで囲む機能です。

### 自動的に doRender プロセッサで囲む

本来の SSI Include で include する対象の形式は自由ですが、Mayaa の場合には `insert` プロセッサとなるため、いくつかの制限があります。

* 全体をひとつのタグで囲んでいること。
* `doRender` プロセッサで囲まれた領域を持つこと。

この置き換え機能を有効にしている場合、次の条件を満たすことでこの制限を回避できます。

* 拡張子が `inc` (`*.inc`) であること。
* `*.inc` の MIME Type が HTML と同じであること。(web.xml で mime-mapping をする)

これらを満たすとき、`*.inc` ファイルをビルドするときに全体を自動的に `doRender` プロセッサで囲みます。`name` 属性は空文字列です。

ただし、`*.inc` ファイルに何のタグにも囲まれていない文字列がある場合、実装上の都合でその文字列は無視されます。例えば次のようなファイルの場合、最後の "baz" は無視されます。必ず何らかのタグで囲んでください。

```html
<span>foo</span>
<div>bar</div>
<del>baz</del>
```

このようにタグを並べただけの形式のファイルを使えることは、主にヘッダ (meta タグ、CSS や JavaScript など) を部品化する際に役立つでしょう。

なお、`*.inc` ファイルを通常の `insert` プロセッサから呼び出すことももちろん可能です。
