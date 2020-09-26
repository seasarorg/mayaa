# Mayaaのテスト

## ユニットテスト

ユニットテストは src/test/java に配置され、Mavenの test フェーズにて
実行される。

```
mvn test
```

## ユニットテストカバレッジ
Mavenによって実行されたテストカバレッジをJacocoで計測している。`mvn test`実行後に
生成されるレストレポートを確認できる。出力先は `target/site/jacoco-ut/index.html` である。

## インテグレーションテスト

### 実行方法
Mavenの integration-test フェーズで動作確認用のHTMLを配置したMayaaServletを
Jetty を用いて立ち上げる。その状態で src/test/java/**/*ITCase を実行する。
起動ポートは 8999 である。
```
mvn integration-test
```

なお、IDEなどから個別にテストコードを実行する場合に下記のコマンドでJettyの起動のみ実施可能。
```
mvn jetty:run
```
## インテグレーションテストカバレッジ
Mavenによって実行されたテストカバレッジをJacocoで計測している。`mvn integration-test`実行後に
生成されるレストレポートを確認できる。出力先は `target/site/jacoco-it/index.html` である。

またユニットテストとインテグレーションテストのテストカバレッジを合算し他レポートは
`target/site/jacoco-merged/index.html` に出力される。


### テストコード

インテグレーションテストコードはサーバサイドで実行されるHTMLファイルおよびMayaaファイルと
サーバにアクセスして結果を検証するためのクライアントコードによって記述される。

種類        | 配置先           
-----------|------------------
クライアント | src/test/java     
HTML/Mayaa | src/integration-test/webapp


クラインアントコードはWebDriverを使ったUIテストフレームワークである
Selenideを用いてJUnit4のParameterizedテストとして記述している。

パッケージ: org.seasar.mayaa.test.integration 


個々のテストケースは ParameterizedテストのデータとしてSelenium IDE
により出力される検証コマンドと近い形式で表現されている。

各データはオブジェクト配列の3つの要素で表されており、0番目の要素から
それぞれ、シナリオ名、アクセス先URLのパス、検証コマンドの配列となっている。

現行のテストデータはこれまで記述されてきたテストシナリオ( /src/integratin-test/webapp/tests.slp )をJUnitコードに移植している。
過去はSelenium のブラウザ以内で実行するテストランナー用のHTMLを tests.slp から生成して実行していたが、今後は直接JUnitに記載すれば十分である。

詳細は WebDriverBase.Commandクラスおよびその継承クラス、これらのクラスの生成
メソッド(verifyTextメソッドなど)、runTestメソッドを参照のこと。

例
```java
{
    "no-injection",
    "/tests/engine/inject_no.html",
    new Command[] {
        verifyTitle("tests_1_01"),
        verifyText("//body/h1", "render test"),
        verifyText("//body/div[@class=\"main\"]/h2", "no inject"),
        verifyText("//div[@class=\"box\"]", "Plain HTML"),
        verifyElementPresent("//meta"),
        verifyElementPresent("//link")
    }
}
```

以上