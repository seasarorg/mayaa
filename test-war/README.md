# テスト用のWARプロジェクト

Mayaaのアーティファクトのプリケーションコンテナによる起動確認、
打鍵よる結合テストのために docker-compose ファイルを用意している。

## docker-compose.yaml

mayaa-*.jar を使用したWARが各コンテナで期待通り起動し、動作に問題がないかを
確認するためのDocker Composeファイルである。

デバッグポート(8787ポート)とJMX(9012ポート)を有効化してある。.envファイルを
作成して docker-compose up で動作する。

対応しているコンテナは Tomcat と Wildfly であり、使用するコンテナを image 属性の
コメントを切り替えて使用する。各コンテナの起動に使用する設定ファイルはそれぞれ
./tomcat/ ./wildfly/ に格納し、Dockerにボリュームとしてマウントしている。

### docker起動の準備
docker-compose.yaml と同じディレクトリに .env ファイルを作成して下記の値を設定する。

  変数名        | 説明
 --------------|----------------------------
  WARFILE      | 配置するWARファイルのパス
  JMX_HOSTNAME | java.rmi.server.hostname に指定する値(Datadogを使用する場合はweb,　使用しない場合は0.0.0.0)
  DD_API_KEY   | Datadogを用いる場合のAPIキー(使用しない場合はdd-agent配下をコメントアウトする)

(.envファイルの例)
```
WARFILE=./target/mayaa-package-test-1.0.war
JMX_HOSTNAME=0.0.0.0
DD_API_KEY=XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
```

### 起動
```
docker-compose up
```

## パフォーマンス測定
JMeterによるパフォーマンス測定を行う。
docker-compose により起動したウェブアプリケーションに対して負荷をかける

### 実行方法
mvn にて実行することができる。
```
mvn clean
mvn package -U
docker-compose up
mvn jmeter:jmeter
```

`target/jmeter/reports/performance/index.html` にレポートが生成される。

### 注意点
`mvn jmeter:jmeter` を一度実行すると結果のレポートが生成されるため、再実行するにはクリーンするか、
`target/jmeter/reports/` 配下のディレクトリを削除する。


## Spring Bootでのテスト起動

Spring Boot組込Tomcatを使用してテスト用アプリケーションを起動することもできます。

### 起動方法
プロジェクトルートから以下のコマンドを実行します：

#### 方法1: spring-boot-maven-plugin を使用
```
mvn -f test-war/pom.xml spring-boot:run -Pspring-boot -Dmayaa.version=1.3.0
```

#### 方法2: JARファイルをビルドして実行
```
mvn -f test-war/pom.xml clean package -Pspring-boot -DskipTests -Dmayaa.version=1.3.0
cd test-war
java -jar target/mayaa-package-test-1.3.0.jar
```

アプリケーションはデフォルトでポート8080で起動します。
ブラウザで `http://localhost:8080` にアクセスしてアプリケーションが正常に動作することを確認できます。

ポート番号を変更する場合は以下のように指定します：

```
java -jar target/mayaa-package-test-1.3.0.jar --server.port=18080
```

### 注意点
- Spring Boot起動時は docker-compose による起動とは異なり、単一のコンテナで実行されます
- `-Pspring-boot` プロファイルの指定が必須です
- デバッグポートやJMXの設定は行われていません
- パフォーマンス測定はdocker-composeを使用してください
