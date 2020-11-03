# Mayaa

http://mayaa.seasar.org/

MayaaはHTMLをベースとしたテンプレートによるプログラマとデザイナの作業分担を強く意識したWEBフロントサービスエンジンです。

## システム動作仕様環境
 * Java7 以降 
 * Servlet API 2.3 ~ 4.0 の web.xml

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
[Mayaaリポジトリ(Github)のIssue機能](https://github.com/seasarorg/mayaa/issues)にてお願いします。

利用に関するお問い合わせ & フィードバックは Mayaa-User ML（日本語）によって行います。
MLへの参加は下記URLページより購読登録してください。
MLへの投稿はSPAM防止のためML購読者に限定しています。MLの購読は制限しておりませんのでどなたでも参加できます。

ML購読登録： https://www.seasar.org/mailman/listinfo/mayaa-user

## 変更履歴
最近の変更履歴は下記の通りです。過去の変更点については [CHANGELOG.md](./CHANGELOG.md) を参照ください。

### Mayaa 1.1.34 : 2017-07-30
#### Fixes
- [#7](https://github.com/seasarorg/mayaa/issues/7) - mayaaファイルに ${} で書いた変数名がそのまま展開される場合がある不具合を修正しました。
- [#5](https://github.com/seasarorg/mayaa/issues/5) - テンプレートに書かれたインデントが詰められる不具合を修正しました。

### Mayaa 1.1.33 : 2017-03-25
#### Changes
- MavenについてはMaven Central Repositoryへの公開に変更しました。
  https://search.maven.org/artifact/com.github.seasarorg.mayaa/mayaa
  - groupId: com.github.seasarorg.mayaa
  - artifactId: mayaa
- [#2](https://github.com/seasarorg/mayaa/issues/2) - Servlet API 3.1に対応しました。

#### Fixes
- ファイルが存在しない場合、タイムスタンプチェックが無効なときにチェックし続ける問題を修正しました。
- Mayaaのforwardをしたさいにpageスコープのキャッシュが以前のままになっている問題を修正しました。
- HttpSessionの無効判定を修正しました。
- ページオブジェクトのキャッシュの同一判定を修正しました。
