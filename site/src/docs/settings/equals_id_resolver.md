---
layout: base
title: HTML名前空間のid属性を無視する
eleventyNavigation:
  key: HTML名前空間のid属性を無視する
  parent: エンジンの設定
  order: 6
---
## HTML名前空間のid属性を無視する

HTML/XHTML の id 属性は、CSS や JavaScript でも使用しますので、Mayaa のバインディングのため id との使いわけが難しい場合があります。HTML/XHTML の id 属性をバインディングに使わず、Mayaa 名前空間の id 属性だけをバインディングに使うよう設定すれば、そのような煩雑さから解放されます。(その代わり、すべてのテンプレートに Mayaa の名前空間宣言 (`xmlns:m="http://mayaa.seasar.org"`) をする必要があります)

HTML/XHTML の id 属性をバインディングに使わないよう設定するには、「<a href="/docs/settings/">エンジン設定方法</a>」と同様に ServiceProvider ファイルを置き、その中で `templateBuilder` の設定をします。

### templateBuilder の設定

`templateBuilder` の設定は標準状態で下記のようになっています。テンプレートの id 属性と Mayaa ファイルの id 属性との一致を処理しているのは `EqualsIDInjectionResolver` です。

```xml {data-filename=org.seasar.mayaa.provider.ServiceProvider}
※レイアウトの都合で改行しています。
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE provider
    PUBLIC "-//The Seasar Foundation//DTD Mayaa Provider 1.0//EN"
    "http://mayaa.seasar.org/dtd/mayaa-provider_1_0.dtd">
<provider>
    <templateBuilder
            class="org.seasar.mayaa.impl.builder.TemplateBuilderImpl">
        <resolver class="org.seasar.mayaa.impl.
                builder.injection.MetaValuesSetter"/>
        <resolver class="org.seasar.mayaa.impl.
                builder.injection.ReplaceSetter"/>
        <resolver class="org.seasar.mayaa.impl.
                builder.injection.RenderedSetter"/>
        <resolver class="org.seasar.mayaa.impl.
                builder.injection.InsertSetter"/>
        <resolver class="org.seasar.mayaa.impl.
                builder.injection.InjectAttributeInjectionResolver"/>
        <resolver class="org.seasar.mayaa.impl.
                builder.injection.EqualsIDInjectionResolver">
            <parameter name="reportUnresolvedID" value="true"/>
            <parameter name="reportDuplicatedID" value="true"/>
            <parameter name="addAttribute"
                    value="{http://www.w3.org/TR/html4}id"/>
            <parameter name="addAttribute"
                    value="{http://www.w3.org/1999/xhtml}id"/>
        </resolver>
        <resolver class="org.seasar.mayaa.impl.builder.
                injection.XPathMatchesInjectionResolver"/>
        <parameter name="outputTemplateWhitespace" value="true"/>
        <parameter name="outputMayaaWhitespace" value="false"/>
        <parameter name="optimize" value="true"/>
    </templateBuilder>
</provider>
```

`EqualsIDInjectionResolver` のパラメータは 3 つです。

`reportUnresolvedID`
: テンプレート側にバインディング対象の id 属性があり、対応する id 属性が Mayaa ファイルにない場合にメッセージをログ出力するかどうかを設定します。ログは commons-logging の WARN レベルで出力します。`true` を設定すると出力し、`false` に設定すると出力しません。デフォルトは "`true`" です。

`reportDuplicatedID`
: Mayaa ファイルに同じ値を持つ id 属性が複数定義されている場合にメッセージをログ出力するかどうかを設定します。複数定義されている場合は最初に見つかったものを使うため、それ以外は使われません。ログは commons-logging の WARN レベルで出力します。`true` を設定すると出力し、`false` に設定すると出力しません。デフォルトは "`true`" です。

`addAttribute`
: Mayaa ファイルの id 属性と対応させるテンプレート側の属性を追加します。属性は " **{名前空間URI}属性名** " の形式で指定します。デフォルトは HTML の id "`{http://www.w3.org/TR/html4}id`" と XHTML の id "`{http://www.w3.org/1999/xhtml}id`" です。(Mayaa の内部では、HTML の名前空間 URI を "http://www.w3.org/TR/html4" として扱っています)

#### ■HTML の id と XHTML の id を処理対象から外す

`EqualsIDInjectionResolver` に HTML の id 属性と XHTML の id 属性を追加しないように設定すれば、バインディングに使う属性を Mayaa 名前空間の id 属性のみになります。(Mayaa 名前空間の id 属性は必ず対象になります)

既存の設定を上書きするため、デフォルトの設定をそのまま定義しなおして、属性の追加をしないようコメントアウトします。


```xml {data-filename=org.seasar.mayaa.provider.ServiceProvider}
※レイアウトの都合で改行しています。
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE provider
    PUBLIC "-//The Seasar Foundation//DTD Mayaa Provider 1.0//EN"
    "http://mayaa.seasar.org/dtd/mayaa-provider_1_0.dtd">
<provider>
    <templateBuilder
            class="org.seasar.mayaa.impl.builder.TemplateBuilderImpl">
        <resolver class="org.seasar.mayaa.impl.
                builder.injection.MetaValuesSetter"/>
        <resolver class="org.seasar.mayaa.impl.
                builder.injection.ReplaceSetter"/>
        <resolver class="org.seasar.mayaa.impl.
                builder.injection.RenderedSetter"/>
        <resolver class="org.seasar.mayaa.impl.
                builder.injection.InsertSetter"/>
        <resolver class="org.seasar.mayaa.impl.
                builder.injection.InjectAttributeInjectionResolver"/>
        <resolver class="org.seasar.mayaa.impl.
                builder.injection.EqualsIDInjectionResolver">
            <parameter name="reportUnresolvedID" value="true"/>
            <parameter name="reportDuplicatedID" value="true"/>
            <!-- HTMLのidとXHTMLのidを追加しないようコメントアウト
            <parameter name="addAttribute"
                    value="{http://www.w3.org/TR/html4}id"/>
            <parameter name="addAttribute"
                    value="{http://www.w3.org/1999/xhtml}id"/>
            -->
        </resolver>
        <resolver class="org.seasar.mayaa.impl.builder.
                injection.XPathMatchesInjectionResolver"/>
        <parameter name="outputTemplateWhitespace" value="true"/>
        <parameter name="outputMayaaWhitespace" value="false"/>
        <parameter name="optimize" value="true"/>
    </templateBuilder>
</provider>
```

これで Mayaa 名前空間の id 属性 (`xmlns:m="http://mayaa.seasar.org"`, `m:id`) のみが有効になります。

必要に応じて、Tapestry の jwcid のような独自属性を追加することもできます。(HTML の名前空間を使う場合は HTML/XHTML の使いわけにご注意ください)
