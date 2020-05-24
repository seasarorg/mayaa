# Mayaa

http://mayaa.seasar.org/

MayaaはHTMLをベースとしたテンプレートによるプログラマとデザイナの作業分担を強く意識したWEBフロントサービスエンジンです。

## システム動作仕様環境
 * Java バージョン1.4.x以降 
 * Servlet API 2.3以降 

1.1.32まではシステムの動作は以下の環境で確認されています。
その他の環境については、適宜フィードバックをお願いいたします。

* Java： J2SDK 1.4.2_18, 1.5.0_18, 1.6.0_20 (Windows)
* コンテナ： Tomcat5.5.27, Tomcat6.0.26

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
