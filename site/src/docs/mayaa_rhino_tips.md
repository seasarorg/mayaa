---
layout: base
title: Rhino JavaScript Tips
eleventyNavigation:
  key: Rhino JavaScript Tips
  parent: 高度な使い方
  order: 15
---
## Appendix 2. JavaScript Tips

<div class="toc">
<a id="toc" name="toc">目次</a>
<ul>
<li><a href="#javaclass">Java のクラスを使う</a></li>
<li><a href="#exception">例外を処理する</a></li>
<li><a href="#var">変数宣言時の注意</a></li>
</ul>
</div>

<h3><a id="javaclass" name="javaclass"></a>Java のクラスを使う <span class="to_toc">(<a href="#toc" title="目次へ">↑目次へ</a>)</span></h3>

Java のクラスは、`java.lang.String` や `java.util.ArrayList` のようにパッケージが "`java.`" で始まるクラスであれば、完全修飾名をそのまま書くだけで利用できます。しかしそれ以外のパッケージ ("`org.`" や "`com.`" など) の場合、先頭に "`Packages.`" を付ける必要があります。

```
例:
var list = new java.util.ArrayList();
var myClass = new Packages.org.example.MyClass();
```

<h3><a id="exception" name="exception"></a>例外を処理する <span class="to_toc">(<a href="#toc" title="目次へ">↑目次へ</a>)</span></h3>

JavaScript にも try ～ catch 構文はありますので、Java で発生した例外を JavaScript で処理することもできます。Java でアプリケーションを作るときには Java 内で処理するでしょうから、デバッグ時には役立つかもしれません。


```js
var caught = "";
try {
    var list = myLogic.findByName(name);
} catch (e) {
    caught = e;
}

<m:write m:id="error" value="${ caught }" />
```

### 変数宣言時の注意 {#var}

※1.1.9　でこの問題は解消しました。ただしコードの読みやすさやメモリ効率、変数参照時のパフォーマンスを確保するためには、適切な位置で var を宣言するべきということは変わりません。

JavaScript では、ローカル変数を宣言するには var を使います。[beforeRender](/docs/before_render/)や [execプロセッサ](/docs/exec/)でローカル変数を宣言した場合、この変数は描画が終わるまで有効です。

`beforeRender`や`exec`プロセッサはそれ自身のスコープを持たないためです。

```
var foo = "bar";
```
もし一度も宣言されていない変数名に対して `var` を使わずに変数へ代入した場合、グローバル変数とでもいうべき状態になってしまいます。そうなった変数は、(Mayaa では) Web アプリケーション内で共通に利用されます。

意図してこの状態にしたのでなければ、見つかりにくいバグとなってしまいますので、変数の宣言時は必ず `var` を使うようにし、そのスコープを意識するようにしてください。(初回アクセスとそれ以降とで動作が変わる、他のユーザの情報が見える、というような現象が起きる可能性があります)


```xml
<m:beforeRender><![CDATA[
    // この時点で foo が無い場合
    foo = "bar"; // Web アプリケーション内共通になってしまう
    var foo1 = "bar1"; // このページのレンダリングが終われば無くなる
    function greeting() {
        var message = "hello"; // この function を抜ければ無くなる
        java.lang.System.out.println(message);
    }
]]></m:beforeRender>

<m:write m:id="message" value="${ foo1 }" />
```
