---
layout: base
title: 例外発生時のページ
eleventyNavigation:
  key: 例外発生時のページ
  subtitle: エラーページをカスタムする
  parent: 高度な使い方
  order: 14
---

## 例外発生時のページ

Mayaa の描画中に例外が発生した場合、Mayaa のエラーページが表示されます。このとき、"例外のクラス名.html" というファイル名のテンプレートを用意することで、例外ごとのエラーページを表示させることができます。

発生した例外のクラス名を持つテンプレートが存在しない場合、例外の親クラスのクラス名で再度テンプレートを探します。
Mayaa の jar ファイルには `/META-INF/java.lang.Throwable.html` というファイルが含まれているため、
テンプレートを用意されていない例外はこのファイルをテンプレートとして使ってエラーページを表示します。
これが Mayaa の標準エラーページです。ただしエラーページの描画中に例外が発生した場合は、
ステータス 500 の Internal Server Error になります。

(※1.1.23以降) Mayaa 標準のエラーページには debug モードがあり、`application.debug` が `null` 以外の場合に debug モードが有効になります。debug モードでは、例外の情報と共に各スコープの内容などを表示します。(※1.1.22以前では標準で debug モードでした)

Mayaa 標準のエラーページを表示しないようにするには、コンテキストルートに `java.lang.Throwable.html` というファイル名のエラーテンプレートを作成してください。また、発生した例外オブジェクトを参照するには、スクリプトで `handledError` 変数を利用できます。

※「Mayaa の描画中」とは、おおむね forward してから Mayaa 内の処理が終わるまで、と考えてください。


### 具体的な例

存在しないページにアクセスがあった場合を考えてみましょう。`*.html` に対して MayaaServlet をマッピングしているとき、
存在しない URL (仮に http://localhost:8080/mayaa/not_exists.html とします) へアクセスがあっても Mayaaに
処理が渡ります。`not_exists.html` というテンプレートファイルが存在しない場合、
`org.seasar.mayaa.impl.engine.PageNotFoundException` が発生します。

#### 例外に対応するページを作る

ここでの例外クラス名は `org.seasar.mayaa.impl.engine.PageNotFoundException` ですので、コンテキストルートに `org.seasar.mayaa.impl.engine.PageNotFoundException.html` という名前のテンプレートファイルを作成します。

```
コンテキストルート
  - org.seasar.mayaa.impl.engine.PageNotFoundException.html
```

```html
org.seasar.mayaa.impl.engine.PageNotFoundException.html
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>404</title>
</head>
<body>
    指定されたページが見つかりません。
</body>
</html>
```

ブラウザで http://localhost:8080/mayaa/not_exists.html にアクセスしてみましょう。

```
実行結果 (改行などは実際の実行結果と異なります)
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>404</title>
</head>
<body>
    指定されたページが見つかりません。
</body>
</html>
```

先ほど作成したテンプレートの内容が表示されますね。

#### 動的な描画をする

このエラーページは通常のテンプレートと同じ扱いですので、動的な値を含めることができます。先ほどのページにリクエストされたパス名を含めてみましょう。

```
コンテキストルート
  - org.seasar.mayaa.impl.engine.PageNotFoundException.html
```

```html {data-filename=org.seasar.mayaa.impl.engine.PageNotFoundException.html}
< class="file"><html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>404</title>
</head>
<body>
    指定されたページ
        "<span id="pageName">/dummy.html</span>"
        が見つかりません。
</body>
</html>
```
```xml
<?xml version="1.0" encoding="UTF-8"?>
<m:mayaa xmlns:m="http://mayaa.seasar.org" noCache="true">
    <m:write m:id="pageName" value="${ request.requestedPath }" />
</m:mayaa>

ブラウザで http://localhost:8080/mayaa/not_exists.html にアクセスしてみましょう。

```
実行結果 (改行などは実際の実行結果と異なります)
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>404</title>
</head>
<body>
    指定されたページ
        /not_exists.html
        が見つかりません。
</body>
</html>
```

リクエストされたパス名が表示されますね。


### 例外発生時の処理をカスタマイズする

エンジン設定によってエラーページのパス、拡張子、あるいは処理をまるごと変更することができます。
詳しくは[エラー処理の設定](/docs/settings/error_handler/)をご覧ください。
