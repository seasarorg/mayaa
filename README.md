# Mayaa

https://seasarorg.github.io/mayaa-site/

MayaaはHTMLをベースとしたテンプレートによるプログラマとデザイナの作業分担を強く意識したWEBフロントサービスエンジンです。

*Mayaa 1.3.0-alpha1 よりJDK動作要件が Java8 以上になっています。*

## システム動作仕様環境
 * Java8 以降 
 * Servlet API 2.4 ~ 4.0 の web.xml

### 動作確認済み環境
 * Wildfly 20.0.1Final + Java 11.0.8
 * Wildfly 10.1.0Final + Java 1.8.0_191
 * Tomcat 8.5.59.0     + Java 1.8.0_272
 * Tomcat 7.0.106      + Java 1.8.0_272

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
変更点については [CHANGELOG.md](./CHANGELOG.md) を参照ください。

