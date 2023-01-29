# Mayaa

http://mayaa.seasar.org/

MayaaはHTMLをベースとしたテンプレートによるプログラマとデザイナの作業分担を強く意識したWEBフロントサービスエンジンです。

*Mayaa 1.2 よりJDK動作要件が Java7 以上になっています。*

## システム動作仕様環境
 * Java7 以降 
 * Servlet API 2.4 ~ 4.0 の web.xml

### 動作確認済み環境
 * Wildfly 20.0.1Final + Java 11.0.8
 * Wildfly 10.1.0Final + Java 1.8.0_191
 * Wildfly 8.1.0.Final + Java 1.7.0_201
 * Tomcat 8.5.59.0     + Java 1.8.0_272
 * Tomcat 7.0.106      + Java 1.8.0_272
 * Tomcat 6.0.53       + Java 1.7.0_131

## 利用方法
Mayaa はバージョン1.1.33以降は [Maven Central Repository](https://search.maven.org/artifact/com.github.seasarorg.mayaa/mayaa) から配布されています。
1.1.32以前は[Seasarプロジェクト](https://www.seasar.org)のMavenリポジトリから配布されています。

|バージョン  | groupId                    | artifactId | リポジトリ|
|----------|----------------------------|------------|-----|
|1.1.33 以降| com.github.seasarorg.mayaa | mayaa      |Maven Central Repository  |
|1.1.32 以前| org.seasar.mayaa           | mayaa      |http://maven.seasar.org/maven2/ |

1.1.33以降は mayaa-[version].war, mayaa-getting-started-[version].war および Eclipse Project ZIP のバイナリー配布は停止しています。

## サポート & フィードバック方法

Mayaa自体の問題や機能に関する提案や相談については
[Mayaaリポジトリ(GitHub)のIssue機能](https://github.com/seasarorg/mayaa/issues)にてお願いします。

利用に関するお問い合わせ & フィードバックは Mayaa-User ML（日本語）によって行います。
MLへの参加は下記URLページより購読登録してください。
MLへの投稿はSPAM防止のためML購読者に限定しています。MLの購読は制限しておりませんのでどなたでも参加できます。

ML購読登録： https://www.seasar.org/mailman/listinfo/mayaa-user

## 変更履歴
最近の変更履歴は下記の通りです。過去の変更点については [CHANGELOG.md](./CHANGELOG.md) を参照ください。

### Mayaa 1.2.1 : Not yet released

#### Changes
- [#80](https://github.com/seasarorg/mayaa/pull/80) - Mayaa動作要件の最低JavaバージョンをJava8としました。
- [#75](https://github.com/seasarorg/mayaa/issue/75) - xml宣言やmetaタグでcharset変更を検知した時に文字コードを指定して再読み込みするように変更しました。

#### Fixes
- [#75](https://github.com/seasarorg/mayaa/issues/75) - balanceTag を無効にするとDOCTYPEがheadタグ内に余分に付加される問題を修正しました。


#### Experimental
- [#77](https://github.com/seasarorg/mayaa/pull/77) -  NekoHTMLを使用せずHTML Living Standardの定義に近いHTMLパーサを実装しました。[詳細](#新パーサーについて)

#### Internal
- テストフレームワークをJUnit5に変更しました。

### Mayaa 1.2 : 2020-11-15

#### Changes
- [#16](https://github.com/seasarorg/mayaa/issues/16) - Mayaa動作要件の最低JavaバージョンをJava7としました。
- [#35](https://github.com/seasarorg/mayaa/pull/35) - Mayaaのバージョンを`${org.seasar.mayaa.impl.Version.MAYAA_VERSION}`で参照できるようにしました。
- [#32](https://github.com/seasarorg/mayaa/issues/32) - JMX経由でMayaaの内部状態をモニタリングできるようにしました。[詳細](https://github.com/seasarorg/mayaa/wiki/Management)
- [#50](https://github.com/seasarorg/mayaa/issues/50) - Serlvet 3.1 および 4.0 の web.xml に対応しました。
- [#15](https://github.com/seasarorg/mayaa/issues/15) - スクリプトのキャッシュの強制保持個数を指定できるようにしました。
- 依存ライブラリをアップグレードしました。
   * commons-beanutils:commons-beanutils:1.8.3 -> 1.9.4
   * commons-collections:commons-collections:3.1 -> 3.2.2
   * commons-logging:commons-logging:1.0.4 -> 1.2
   * xerces:xercesImpl:2.7.1 -> 2.12.0

#### Fixes
- [#14](https://github.com/seasarorg/mayaa/issues/14) - 複数スレッド下でスクリプトキャッシュの競合を解消するとともにキャッシュ保持数の制御を改善しました。
  org.seasar.mayaa.provider.ServiceProvider 内の scriptEnvironment のパラメータ名 cacheSize にて最小の保持数を設定します。（デフォルト値128）
- [#49](https://github.com/seasarorg/mayaa/pull/49) - URLエンコードされる文字を含むsystemIDのファイル実体が参照できない潜在的不具合に対応しました。


## 新パーサーについて

昨今のフロントエンドの仕様に追従できるようにNekoHTMLを使用せずHTML Living Standardの定義に近いHTMLパーサを独自実装しました。

WEB-INF/org.seasar.mayaa.provider.ServiceProvider の templateBuilder 設定でパラメータ名 `useNewParser` を `true` に設定すると有効になります。
```xml
    <templateBuilder class="org.seasar.mayaa.impl.builder.TemplateBuilderImpl">
        (snip)
        <parameter name="useNewParser" value="true"/>
```
未指定時は NekoHTMLパーサを使用します。
将来的にはこの新パーサをデフォルトにします。最終的にはNekoHTML版のパーサは廃止する計画です。

まだ実験的機能ですが下記の特徴があります。

* HTMLのタグバランシング機能を廃止
* Void Element を正しく処理できるようにした（一部のエレメントはNekoHTMLパーサでも対応した） 
* Empty attribute では属性名のみを出力する ( `<input disabled>` のように出力される )
* Unquoted attribute value をパースできるようにした( '=' 以後の最初の空白文字までを値とみなす)。出力は常に二重引用符で囲む。
* @click や :value などJavaScriptのUIライブラリで使用される属性の記法を出力する
