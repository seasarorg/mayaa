---
layout: base
title: 関連プロジェクト
eleventyNavigation:
  key: Struts2 Mayaa Plugin
  title: Struts2 Mayaa Plugin(終了)
  parent: 関連プロジェクト
  order: 2
---
:::note alert
すでに公開を終了しています。<br>
最終バージョン： ver 1.0.1 (2007/8/13)
:::

#### Struts2 Mayaa Pluginとは
Struts2 Mayaa Pluginは1[Struts2](http://struts.apache.org/2.x/)からMayaaを利用するための実装を提供します。

(公開を終了しました)

最終バージョン： ver 1.0.1 (2007/8/13)

ソースコードは Githubにて公開されています。
[seasarorg/mayaa-struts2](https://github.com/seasarorg/mayaa-struts2)

<h4>利用方法(Struts2)</h4>
<p>
struts.xmlのpackage要素内にMayaaResultの定義を追加し、Mayaaを使用するActionにresultの定義を追加します。<br>
locationパラメータでMayaaが処理するHTMLファイルを指定することが出来ます。
locationパラメータを指定しない場合は「actionのname属性.html」というファイルを使用します。
</p>
<pre>
<code>struts.xml</code>
<span class="file">&lt;!DOCTYPE struts PUBLIC
    "-//Apache Software Foundation//DTD Struts Configuration 2.0//EN"
    "http://struts.apache.org/dtds/struts-2.0.dtd"&gt;
&lt;struts&gt;
  &lt;package name="default" extends="struts-default"&gt;
    &lt;result-types&gt;<strong>
      &lt;result-type class="org.seasar.mayaa.struts2.MayaaResult"
        name="mayaa" /&gt;</strong>
    &lt;/result-types&gt;

    &lt;action name="index"
      class="org.seasar.mayaa.struts2.example.action.TestAction"&gt;<strong>
      &lt;result name="success" type="mayaa"&gt;
        &lt;param name="location"&gt;test.html&lt;/param&gt;
      &lt;/result&gt;</strong>
    &lt;/action&gt;
  &lt;/package&gt;
&lt;/struts&gt;</span></pre>

<p>またStruts2 Mayaa Pluginで用意されているmayaa-defaultパッケージを継承すれば、MayaaResultの定義は不要になります</p>
<pre>
<code>struts.xml</code>
<span class="file">&lt;!DOCTYPE struts PUBLIC
    "-//Apache Software Foundation//DTD Struts Configuration 2.0//EN"
    "http://struts.apache.org/dtds/struts-2.0.dtd"&gt;
&lt;struts&gt;
  &lt;package name="default" extends="mayaa-default"&gt;
    &lt;action name="index"
      class="org.seasar.mayaa.struts2.example.action.TestAction"&gt;<strong>
      &lt;result name="success" type="mayaa"&gt;
        &lt;param name="location"&gt;test.html&lt;/param&gt;
      &lt;/result&gt;</strong>
    &lt;/action&gt;
  &lt;/package&gt;
&lt;/struts&gt;</span></pre>
<h4>利用方法(Mayaa)</h4>
<p>
Mayaa側の設定は不要です。JarファイルをWEB-INF/libに追加すれば、JarファイルのMETA-INF内の設定ファイルが自動で読み込まれるようになります。<br>
あとはMayaaファイルの属性部分で以下のように指定すればActionのプロパティなど、ValueStack経由でアクセスできるオブジェクトをMayaa側で使用できるようになります。
ValueStackのオブジェクトにアクセスする際のプレフィックスは「struts2」ですが、省略することも出来ます。
</p>
<pre>
<code>prefix指定時</code>
<span class="file">&lt;m:write value="&#36;{struts2.プロパティ名}" /&gt;</span>
<code>prefix省略時</code>
<span class="file">&lt;m:write value="&#36;{プロパティ名}" /&gt;</span>
</pre>

<h4>変更履歴</h4>
<h5><a id="ver1.0.1" name="ver1.0.1"></a>変更点 1.0.0 -> 1.0.1 (2007/08/13)</h5>
<ul>
<li class="fix">相対パス指定の場合にpathInfoの始まりが"/"でなかったのを修正しました</li>
<li class="fix">Engine#doServiceの後にCycleUtil#cycleFinalizeを呼び出していなかったのを修正しました</li>
</ul>
