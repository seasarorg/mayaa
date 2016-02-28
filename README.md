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


## 1.1.32 (2013-08-12)

- (rev.3538) 高負荷時にスクリプトのキャッシュ取得で無限ループになる場合がある問題を修正しました。


## 1.1.31 (2013-04-07)

- (rev.3537) 高負荷時にビルド結果取得に失敗する場合がある問題を修正しました。
- (rev.3538) Webコンテナを使わない場合などのビルド時にNullPointerExceptionが発生する場合がある問題を修正しました。
- (rev.3539) 1.1.30でFileGeneratorを使う処理が失敗するようになった問題を修正しました。

