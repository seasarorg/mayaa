# Mayaa

http://mayaa.seasar.org/

MayaaはHTMLをベースとしたテンプレートによるプログラマとデザイナの作業分担を強く意識したWEBフロントサービスエンジンです。

## システム動作仕様環境

JavaVM
:   Java2 バージョン1.4.x以降

コンテナ
:   Servlet API 2.3以降

## Maven2からの利用

seasar.orgのMaven2レポジトリ
:   http://maven.seasar.org/maven2/

seasar.orgのMaven2-SNAPSHOTレポジトリ
:   http://maven.seasar.org/maven2-snapshot/

groupId
:   org.seasar.mayaa

artifactId
:   mayaa


## 1.1.33 (2017-xx-xx)

- ファイルが存在しない場合、タイムスタンプチェックが無効なときにチェックし続ける問題を修正しました。
- Mayaaのforwardをしたさいにpageスコープのキャッシュが以前のままになっている問題を修正しました。
- Servlet API 3.1に対応しました。
- HttpSessionの無効判定を修正しました。
- ページオブジェクトのキャッシュの同一判定を修正しました。
