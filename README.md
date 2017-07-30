# Mayaa

http://mayaa.seasar.org/

MayaaはHTMLをベースとしたテンプレートによるプログラマとデザイナの作業分担を強く意識したWEBフロントサービスエンジンです。

## システム動作仕様環境

JavaVM
:   Java2 バージョン1.4.x以降

コンテナ
:   Servlet API 2.3以降

## Maven2からの利用

https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22mayaa%22

groupId
:   com.github.seasarorg.mayaa

artifactId
:   mayaa

### 1.1.32以前のMaven2からの利用

seasar.orgのMaven2レポジトリ
:   http://maven.seasar.org/maven2/

seasar.orgのMaven2-SNAPSHOTレポジトリ
:   http://maven.seasar.org/maven2-snapshot/

groupId
:   org.seasar.mayaa

artifactId
:   mayaa


## 1.1.34 (2017-07-30)

- mayaaファイルに ${} で書いた変数名がそのまま展開される場合がある不具合を修正しました。
- テンプレートに書かれたインデントが詰められる不具合を修正しました。

## 1.1.33 (2017-03-25)

- MavenについてはMaven Central Repositoryへの公開に変更しました。
    - https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22mayaa%22
    - groupId: com.github.seasarorg.mayaa
    - artifactId: mayaa
- ファイルが存在しない場合、タイムスタンプチェックが無効なときにチェックし続ける問題を修正しました。
- Mayaaのforwardをしたさいにpageスコープのキャッシュが以前のままになっている問題を修正しました。
- Servlet API 3.1に対応しました。
- HttpSessionの無効判定を修正しました。
- ページオブジェクトのキャッシュの同一判定を修正しました。
