---
layout: base
title: エラー処理の設定
eleventyNavigation:
  key: エラー処理の設定
  parent: エンジンの設定
  order: 5
---
## {{ title }}

「[エンジン設定方法](/docs/settings/)」の手順で[例外発生時のページ](/docs/throwable/)を探す処理などの設定を変更できます。

### ErrorHandler の設定

`engine` の中に `errorHandler` 要素を書き、その子要素としてパラメータを設定します。


```xml {data-filename=org.seasar.mayaa.provider.ServiceProvider}
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE provider
    PUBLIC "-//The Seasar Foundation//DTD Mayaa Provider 1.0//EN"
    "http://mayaa.seasar.org/dtd/mayaa-provider_1_0.dtd">
<provider>
    <engine>
        <errorHandler>
            <parameter name="`folder`" value="`/`"/>
            <parameter name="`extension`" value="`html`"/>
        </errorHandler>
        <parameter name="..." value="..."/>
    </engine>
</provider>
```

`folder`
: 例外ページを置くフォルダを指定します。"/" で始まり "/" で終わるよう指定してください。デフォルトは "`/`" です。

`extension`
: 例外ページの拡張子を指定します。デフォルトは "`html`" です。

### ErrorHandler の処理を変更

`errorHandler` 要素の class 属性を指定することで、例外発生時の処理をするクラスを変更できます。ここで指定するクラスは `org.seasar.mayaa.engine.error.ErrorHandler` インターフェースを実装し、かつ引数無しのコンストラクタを持つ必要があります。デフォルトのクラスは `org.seasar.mayaa.impl.engine.error.TemplateErrorHandler` です。

```xml {data-filename=org.seasar.mayaa.provider.ServiceProvider}
※レイアウトの都合で改行しています。
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE provider
    PUBLIC "-//The Seasar Foundation//DTD Mayaa Provider 1.0//EN"
    "http://mayaa.seasar.org/dtd/mayaa-provider_1_0.dtd">
<provider>
    <engine>
        <errorHandler class="org.seasar.mayaa.impl.engine.error.TemplateErrorHandler">
            <parameter name="folder" value="/"/>
            <parameter name="extension" value="html"/>
        </errorHandler>
        <parameter name="..." value="..."/>
    </engine>
</provider>
```
