---
layout: base
title: パス自動調整の設定
eleventyNavigation:
  key: パス自動調整の設定
  parent: エンジンの設定
  order: 9
---
##  パス自動調整の設定

「[エンジン設定方法](/docs/settings/)」の手順で、[パスの自動調整機能](/docs/path_adjust/)の設定を変更できます。デフォルト設定は下記の通りです。

```xml {data-filename=org.seasar.mayaa.provider.ServiceProvider}
※レイアウトの都合で改行しています。
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE provider
    PUBLIC "-//The Seasar Foundation//DTD Mayaa Provider 1.0//EN"
    "http://mayaa.seasar.org/dtd/mayaa-provider_1_0.dtd">
<provider>
    <pathAdjuster>
        <parameter name="enabled" value="true"/>
        <parameter name="force" value="false"/><!-- since 1.1.13 -->
    </pathAdjuster>
</provider>
```
設定可能なパラメータは 2 つです。


`enabled`
: パスの自動調整機能を有効にするかどうか設定します。`true` ならば有効です。デフォルトは `true` です。

`force`
: パスが "./" で始まっていない場合も自動調整するかどうかを設定します。`true` を設定すると "./" で始まっていないパスも自動調整します。デフォルトは "`false`" です。
