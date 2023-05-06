---
layout: base
title: 属性自動セット
eleventyNavigation:
  key: 属性自動セット
  parent: エンジンの設定
  order: 2
---
## 属性自動セット

### JSP カスタムタグに HTML テンプレートの属性をセットする

JSP カスタムタグのなかには HTML のタグを出力するものがあり、そのために HTML のタグと同じ属性を持つものがあります。そのような JSP カスタムタグの場合、HTML テンプレートと Mayaa ファイルの JSP カスタムタグとに同じ属性を書くことになり、数が多い場合には大変です。

Mayaa には、HTML テンプレートにある属性を自動的に JSP カスタムタグの属性としてセットする機能があります。完全自動では意図しない動作を引き起こすため、この機能はデフォルト設定で無効になっています。適切に設定することで、この機能を有効にできます。

この機能を有効にすると、TLD から取得できる情報を元に JSP カスタムタグの属性を調べ、Mayaa ファイル上で指定されていない属性には HTML テンプレート上の同名属性から値を取得してセットします。Mayaa ファイル上でセットしている属性には何もしません。

#### ■実行イメージ

実行時イメージは次のようになります。実行結果の強調部分は、Mayaa ファイルには書いていない属性です。(例として Struts の JSP カスタムタグを使用しています)

```html {data-filename=hello.html}
<html>
<body>
    <form id="greetingForm">
        <input id="greetingInput" name="greeting" value="Hello."
            size="20" maxlength="20">
    </form>
</body>
</html>
```

```xml {data-filename=hello.mayaa}
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org"
            xmlns:html="http://struts.apache.org/tags-html">
    <html:form m:id="greetingForm" action="/hello" />

    <!-- html:text には size, maxlength, styleId などの属性があります -->
    <html:text m:id="greetingInput" property="greeting" />
</m:mayaa>
```

```html
実行結果
<html>
<body>
    <form action="/mayaa/hello.do">
        <input id="greetingInput" name="greeting" value=""
             size="20" maxlength="20" >
    </form>
</body>
</html>
```

html:text タグには `size` 属性, `maxlength` 属性, `styleId` 属性を指定していませんが、出力はそれらを指定したかのようなものになっています。このような動作をさせるには、たとえば次のように設定します。

```xml {data-filename=org.seasar.mayaa.provider.ServiceProvider}
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE provider
    PUBLIC "-//The Seasar Foundation//DTD Mayaa Provider 1.0//EN"
    "http://mayaa.seasar.org/dtd/mayaa-provider_1_0.dtd">
<provider>

    <templateAttributeReader templateAttributeReader >
        <!-- 無視する属性の設定 (複数記述可能) -->
        <ignoreAttribute 
                 qName ="{http://struts.apache.org/tags-html}*"
                 attribute ="id" />
        <ignoreAttribute
                qName="{http://struts.apache.org/tags-html}text"
                attribute="name" />
        <ignoreAttribute
                qName="{http://struts.apache.org/tags-html}text"
                attribute="value" />

        <!-- 別名で置き換える属性の設定 (複数記述可能) -->
        <aliasAttribute 
                 qName ="{http://struts.apache.org/tags-html}*"
                 attribute ="styleId"
                 templateAttribute ="id" />

        <!-- 機能を有効にする設定 (デフォルトは false) -->
        <parameter name ="enabled" value ="true" />
    </templateAttributeReader >
</provider>
```

#### ■設定タグ・属性の説明

この機能の設定は  templateAttributeReader  タグでおこないます。`templateAttributeReader` タグのボディには  ignoreAttribute  タグ、`aliasAttribute` タグ、`parameter` タグを書くことができ、それぞれ複数、この順番で記述します。(`parameter` タグで設定する内容は `enabled` のひとつだけです)


#### ・属性自動セット機能を有効にする

```xml
例
<parameter   name ="enabled"  value ="true" />
```

機能を有効にするには、`templateAttributeReader` タグの子として  parameter  タグを書き、`name` 属性に `"enabled"`、`value` 属性に `"true"` を書きます。デフォルトは `"false"` になっています。
機能を有効にすると、すべての JSP カスタムタグの属性に対し、対応づけられた HTML テンプレート上のタグにある同名の属性が自動的にセットされるようになります。



#### 自動セットしない属性を指定する

```xml
例
<ignoreAttribute 
         qName ="{http://struts.apache.org/tags-html}text"
         attribute ="value" />
```
すべての属性を自動的にセットすると、JSP カスタムタグによっては正常に動作しない場合があります。たとえば例に挙げた Struts の `html:text` は、`property` 属性と `value` 属性を同時にセットすると `property` 属性の指定は無効になるため、`property` 属性を使いたい場合には HTML テンプレート側に `value` 属性を付けられないということになります。

この対策として、JSP カスタムタグの属性ごとに、HTML テンプレート上の属性を使わないよう設定できます。設定をするには、`templateAttributeReader` タグの子として  ignoreAttribute  タグを書き、`qName` 属性に設定する対象の JSP カスタムタグ名を、`attribute` 属性に自動セットの対象外とする JSP カスタムタグの属性名を書きます。

`qName` 属性の値にはブレース ("{", "}") で囲んだ `uri` (Mayaa ファイルの xmlns で指定するもの) と、それに続けて JSP カスタムタグ名を指定します。これは完全一致か前方一致を指定でき、前方一致の場合には末尾に "\*" を指定します。末尾以外の "\*" は特別な意味を持ちません。

`attribute` 属性の値には `qName` で指定した JSP カスタムタグの属性名を指定します。これは完全一致か前方一致を指定でき、前方一致の場合には末尾にワイルドカード ("*") を指定します。末尾以外の "*" は特別な意味を持ちません。


#### 自動セットするときに属性名を変える

```xml
例
<aliasAttribute 
         qName ="{http://struts.apache.org/tags-html}text"
         attribute ="styleId"
         templateAttribute ="id" />
```

JSP カスタムタグによっては、出力する HTML の属性名と JSP カスタムタグでの属性名が異なる場合があります (例: Struts では `styleId` 属性の値が `id` 属性の値として出力される)。

このような属性名が異なる場合の設定をするには、`templateAttributeReader` タグの子として  aliasAttribute  タグを書き、`qName` 属性に設定する対象の JSP カスタムタグ名を、`attribute` 属性に JSP カスタムタグの属性名を、`templateAttribute` 属性に HTML テンプレート上の属性名を書きます。
`qName` 属性、および `attribute` 属性は ignoreAttribute タグの同名属性と同じです。

`templateAttribute` 属性の値には `attribute` 属性で指定した JSP カスタムタグの属性に対応する HTML テンプレート上の属性の名前を指定します。上記の例では、`text` カスタムタグの `styleId` 属性に、HTML テンプレート上の `id` 属性の値がセットされることになります。`templateAttribute` 属性にはワイルドカードを使うことはできません。
