---
layout: base
title: ビルド結果保存
eleventyNavigation:
  key: ビルド結果保存
  parent: エンジンの設定
  order: 2
---

## ビルド結果保存

Mayaa ではページをビルドした結果、描画するために必要な情報をオブジェクトとして作成します。このオブジェクトはガーベジコレクションによって解放されることがあり、解放された場合には描画する前にもう一度ビルドすることになります。タグが多いページなど、ビルドに長めの時間がかかる場合もありますので、内容が変わらないページを何度もビルドすることは好ましくありません。


この問題を軽減するため、ビルドした結果をシリアライズしてファイルにキャッシュし、2 回目以降のビルド時間を短縮することができます。この機能を有効にした 2 回目以降のビルドでは、ビルド元となるファイルとキャッシュファイルとのタイムスタンプを比較し、キャッシュの方が新しい場合には再ビルドする代わりにキャッシュを読み込みます。


タグの数が多くてビルドに時間がかかる場合や、ページ数が多くてメモリを圧迫する場合などに有効です。



### ビルド結果キャッシュを有効にする

ビルド結果キャッシュを有効にするには、[ServiceProvider](/docs/settings/) ファイルで設定します。標準の設定は下記のようになっています。"`pageSerialize`" パラメータを "`true`" に設定することでビルド結果キャッシュが有効になります。

ビルド結果をシリアライズしたファイルは、アプリケーションの WEB-INF/.mayaaSpecCache フォルダの中に作成されます。この位置への読み書きができない場合、ビルド結果キャッシュは動作しませんのでご注意ください。


```xml {data-filename=org.seasar.mayaa.provider.ServiceProvider}
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE provider
    PUBLIC "-//The Seasar Foundation//DTD Mayaa Provider 1.0//EN"
    "http://mayaa.seasar.org/dtd/mayaa-provider_1_0.dtd">
<provider>
    <engine>
        <parameter name="`pageSerialize`" value="`false`"/>
    </engine>
</provider>
```

#### ■設定項目について

設定項目は下記の 1 項目のみです。

`pageSerialize`
: ビルド結果キャッシュを有効にするかどうかを設定します。"`true`" なら有効、"`false`" なら無効です。デフォルトは "`false`" です。
