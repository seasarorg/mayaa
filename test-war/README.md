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

### WildFly利用時の注意（ロガー設定）
- `quay.io/wildfly/wildfly:32.0.1.Final-jdk21` を使う場合、`./wildfly/standalone.conf` で
  `org.jboss.logmanager` と `wildfly-common` を `-Xbootclasspath/a` に含める必要があります。
- JMX有効化（`-Dcom.sun.management.jmxremote*`）時はロガー初期化が早期に走るため、上記設定がないと
  `WFLYLOG0078` や `ClassNotFoundException: org.jboss.logmanager.*` で起動失敗します。
- Tomcat用の `./tomcat/setenv.sh` にはこの設定は不要です（WildFly専用の対応）。

## パフォーマンス測定
JMeterによるパフォーマンス測定を行う。
docker-compose により起動したウェブアプリケーションに対して負荷をかける

### 実行方法
mvn にて実行することができる。
```
mvn clean
mvn package -U
docker-compose up
mvn jmeter:configure@configuration jmeter:jmeter@jmeter-tests jmeter:results@jmeter-check-results
```

通常は `target/jmeter/reports/performance/index.html` にレポートが生成される。

ただし、テスト内容によっては JMeter Dashboard 生成時に例外が発生し、HTMLレポートのみ生成されない場合がある。
その場合でも `target/jmeter/results/*.csv` が生成され、`jmeter:results` が成功していれば計測データ自体は取得できているため、CSVとJFRを主結果として扱う。

`mvn jmeter:jmeter` 単体では `configure` が先に実行されないため失敗する。上記コマンドのように
`configure` と `jmeter`（必要に応じて `results`）を同時に実行する。

### 注意点
`mvn jmeter:jmeter` を一度実行すると結果のレポートが生成されるため、再実行するにはクリーンするか、
`target/jmeter/reports/` 配下のディレクトリを削除する。

### 実行〜JFR取得まで一発で行う
`test-war/run-jmeter-with-jfr.sh` を使うと、以下をまとめて実行できる。

1. Spring Boot用JARのビルド
2. JFR有効でアプリ起動
3. JMeter実行
4. アプリ停止とJFRファイル保存

```
chmod +x test-war/run-jmeter-with-jfr.sh
test-war/run-jmeter-with-jfr.sh
```

必要に応じて環境変数で上書きできる。

- `MAYAA_VERSION` (default: `2.0.0-SNAPSHOT`)
- `APP_MODE` (default: `spring-boot-run`)
  - `spring-boot-run`: `/tests/index.html` を含むテストコンテンツが見えるため推奨
  - `jar`: 実行可能JARで起動（環境により `/tests/*` が見えない場合あり）
- `APP_PORT` (default: `8080`)
  - Spring Boot起動ポートとJMeter送信先ポートの両方に適用される
  - 実行開始時にポート使用中ならプリフライトで即失敗し、対象プロセス取り違えを防止
  - `APP_PORT=auto` で空きポートを自動選択（探索範囲: `18080-18140`）
- `JMX_HOSTNAME` (default: `127.0.0.1`)
- `JFR_SETTINGS` (default: `profile`)
- `SPRING_BOOT_RUN_JVM_ARGS` (default: `-Djava.awt.headless=true`)
  - `spring-boot.jvmArguments` として `spring-boot-maven-plugin` に渡される
  - デフォルトでは `spring-boot:run` の固定JDWPポート設定を無効化し、`Address already in use` を回避
  - 必要なら明示指定で追加JVM引数を渡せる
- `JFR_FILE` (default: `./flight_recording_<timestamp>.jfr`)
- `READY_PATHS` (default: `/index.html,/tests/index.html`)
- `READY_TIMEOUT_SEC` (default: `120`)
- `READY_MODE` (default: `http`)
  - `tcp`: `127.0.0.1:APP_PORT` のポート開通で起動判定
  - `http`: `READY_PATHS` のいずれかが `2xx/3xx` 応答で起動判定
- `JMETER_THREAD` (default: `20`)
- `JMETER_RAMPUP` (default: `20`)
- `JMETER_DURATION` (default: `180`)
- `JMETER_DELAY` (default: `15`)
- `PERF_MIX_BALANCED` (default: `5`)
- `PERF_MIX_CACHE_MISS` (default: `3`)
- `PERF_MIX_MEMORY` (default: `2`)
- `PERF_MIX_SYNC_HOT` (default: `0`)
  - 低CPU・小キャッシュ・高競合寄り。`Collections.synchronizedMap` のモニタ競合（同期待ち）を強調する。
- `PERF_MIX_SYNC_MISS` (default: `0`)
  - Sync-Hotよりミス率が高い条件。`get/put` 競合とミス時処理の混在で待機を増幅する。
- `PERF_MIX_MAYAA_COLD` (default: `0`)
  - 1ループごとにMayaaキャッシュをクリアし、パース/内部ツリー構築/スクリプト評価/レンダリングを再実行させる。

定常計測向けのプリセット（10コア開発マシン想定）:
- ウォームアップ: `15s`（`JMETER_DELAY`）
- 負荷立ち上げ: `20s`（`JMETER_RAMPUP`）
- 計測区間: `180s`（`JMETER_DURATION`）
- 同時実行: `20 threads`（`JMETER_THREAD`）

負荷ボトルネック検出用ページ（`/tests/index.html` にリンク済み）:
- `perf/hotspot.html?...cacheSize=128&keyRange=256...` : キャッシュヒット寄り（基準測定）
- `perf/hotspot.html?...cacheSize=64&keyRange=4096...` : キャッシュミス混在
- `perf/hotspot.html?...payloadKb=512...` : メモリプレッシャー寄り

主な調整パラメータ（クエリ）:
- `cpu`: 1リクエストあたりCPU負荷ループ量
- `payloadKb`: キャッシュミス時に生成・保持するペイロードサイズ
- `cacheSize`: LRUキャッシュ保持件数
- `keyRange`: キー空間（`cacheSize` より十分大きいほどミス率上昇）
- `rows`: レンダリング時の出力行数

JMeterでは上記3プリセットを固定比率で追加実行:
- Balanced: `50%`（5回）
- Cache-Miss Mixed: `30%`（3回）
- Memory Pressure: `20%`（2回）
- Sync-Hot: `0%`（0回、デフォルト無効）
- Sync-Miss: `0%`（0回、デフォルト無効）
- Mayaa-Cold: `0%`（0回、デフォルト無効）

注: 現在の `performance.jmx` は計測の再現性を優先し、`/tests/` 全リンク巡回は無効化している。
意図的にエラーを返す検証ページを含めないことで、JMeterダッシュボード（HTML）生成の安定性を上げている。

比率を変更したい場合は Maven プロパティで上書き可能:
- `perfMixBalanced` (default: `5`)
- `perfMixCacheMiss` (default: `3`)
- `perfMixMemory` (default: `2`)

`run-jmeter-with-jfr.sh` では環境変数で同じ値を指定可能:
- `PERF_MIX_BALANCED=6 PERF_MIX_CACHE_MISS=2 PERF_MIX_MEMORY=2 ./test-war/run-jmeter-with-jfr.sh`
- `PERF_MIX_SYNC_HOT=4 PERF_MIX_SYNC_MISS=4 ./test-war/run-jmeter-with-jfr.sh`
- `PERF_MIX_MAYAA_COLD=4 ./test-war/run-jmeter-with-jfr.sh`

### 並列リクエストの同期/ブロック検出シナリオ

同期待ち（`BLOCKED` / monitor contention）を観測したい場合は、軽量リクエストを高並列で連続投入して、CPU計算よりも共有ロック競合が前面に出る条件を使う。

推奨実行例（Spring Boot実行 + JFR取得）:

```
JMETER_THREAD=96 \
JMETER_RAMPUP=20 \
JMETER_DURATION=240 \
JMETER_DELAY=10 \
PERF_MIX_BALANCED=0 \
PERF_MIX_CACHE_MISS=0 \
PERF_MIX_MEMORY=0 \
PERF_MIX_SYNC_HOT=6 \
PERF_MIX_SYNC_MISS=4 \
./test-war/run-jmeter-with-jfr.sh
```

JFRでの確認観点:
- `jdk.JavaMonitorEnter` の待機時間・発生回数が増えているか
- `http-nio-*-exec-*` スレッドの `BLOCKED` 期間が増えているか
- 同時に `ThreadPark` が増える場合は、アプリ内の待機（`join/get` 等）との重なりを疑う

### Mayaa内部工程（パース/ツリー変換/レンダリング）を狙うコールドシナリオ

`PERF_MIX_MAYAA_COLD` は 1ループで次を実行する:
- `/tests/perf/cache-reset.html?namePattern=.*` でMayaaのCacheControl MBeanを一括 `invalidateAll`
- `/tests/processor/forEachRecursive.html`
- `/tests/component/recursive.html`
- `/tests/engine/script.html`

これにより、温まったキャッシュ依存を外して Mayaa 内部処理の再構築コストを計測しやすくする。

推奨実行例（Mayaa内部工程のブロック検知用）:

```
JMETER_THREAD=48 \
JMETER_RAMPUP=20 \
JMETER_DURATION=180 \
JMETER_DELAY=10 \
PERF_MIX_BALANCED=0 \
PERF_MIX_CACHE_MISS=0 \
PERF_MIX_MEMORY=0 \
PERF_MIX_SYNC_HOT=0 \
PERF_MIX_SYNC_MISS=0 \
PERF_MIX_MAYAA_COLD=6 \
./test-war/run-jmeter-with-jfr.sh
```

JFRでの追加確認観点（Mayaa内部に寄せる）:
- `org.seasar.mayaa.impl.builder` / `org.seasar.mayaa.impl.engine` / `org.seasar.mayaa.impl.cycle.script` を含むスタックで `JavaMonitorEnter` を確認
- 比較対象として `PERF_MIX_MAYAA_COLD=0`（他条件同一）を1本取り、差分で判定

対照実験として `PERF_MIX_BALANCED=10`（他0）も同じ `JMETER_THREAD` で1本取得すると、同期待ち由来の差分を比較しやすい。

JMeter本体バージョンは `test-war/pom.xml` で `5.6.3` を指定している（Java 21 環境でのレポート生成安定化のため）。

計測成功の確認ポイント:
- JFRファイルが生成され、サイズが 0 より大きい（`flight_recording_*.jfr`）
- JMeter のCSV結果が生成される（`target/jmeter/results/*.csv`）
- HTMLレポートが未生成でも、上記2点が満たされていれば再分析は可能


## Spring Bootでのテスト起動

Spring Boot組込Tomcatを使用してテスト用アプリケーションを起動することもできます。

### 重要: 実行経路差分に関する注意
- `test-war` は `com.github.seasarorg.mayaa:mayaa:${mayaa.version}` を依存として参照します。
- ルート `mayaa` の変更を反映して確認する場合は、先にプロジェクトルートで `mvn -DskipTests install` を実行してください。
- `src/test/resources` 配下（例: `it-case/html-transform/jsp-tags`）は Spring Boot 実行JARには含まれないため、動作確認は `src/integration-test/webapp/tests/...` のURLを使ってください。

### 起動方法
プロジェクトルートから以下のコマンドを実行します：

#### 方法1: spring-boot-maven-plugin を使用
```
mvn -f test-war/pom.xml spring-boot:run -Pspring-boot -Dmayaa.version=2.0.0-SNAPSHOT

```

#### 方法2: JARファイルをビルドして実行
```
mvn -DskipTests install
mvn -f test-war/pom.xml clean package -Pspring-boot -DskipTests -Dmayaa.version=2.0.0-SNAPSHOT
cd test-war
java -jar target/mayaa-package-test-2.0.0-SNAPSHOT.jar
```

アプリケーションはデフォルトでポート8080で起動します。
ブラウザで `http://localhost:8080` にアクセスしてアプリケーションが正常に動作することを確認できます。

ポート番号を変更する場合は以下のように指定します：

```
java -jar target/mayaa-package-test-2.0.0-SNAPSHOT.jar --server.port=18080
```

### 注意点
- Spring Boot起動時は docker-compose による起動とは異なり、単一のコンテナで実行されます
- `-Pspring-boot` プロファイルの指定が必須です
- デバッグポートは `8787` で有効です（`test-war/pom.xml` の `spring-boot-maven-plugin` 設定）
- JMXポートは `9012` で有効です（`test-war/pom.xml` の `spring-boot-maven-plugin` 設定）
- パフォーマンス測定はdocker-composeを使用してください

### JMX接続方法（spring-boot:run / java -jar 共通）

1. アプリケーションを起動する（上記のいずれかの方法）。
2. JMXクライアント（JMC / jconsole）を起動する。
3. 接続先に以下を指定する。

```
service:jmx:rmi:///jndi/rmi://127.0.0.1:9012/jmxrmi
```

4. `org.seasar.mayaa:type=CacheControl,name=*` を開き、各キャッシュの属性を確認する。
  - 例: `specificationCache`, `CompiledScript`, `SourceCompiledScript`, `JspTagPool`, `PrefixAwareName`

## Spring Boot用 docker-compose

`mvn spring-boot:run` をコンテナ内で実行する compose ファイルとして、
`docker-compose-spring-boot.yaml` を用意しています。
この compose は `test-war/pom.xml` の `spring-boot-maven-plugin` (`jvmArguments`) でJMX設定を渡すため、
`9012` のJMX接続先は Spring Boot アプリJVMです。

### 事前準備
```
mvn -DskipTests install
```

### 起動
```
cd test-war
docker compose -f docker-compose-spring-boot.yaml up
```

### 接続先
- HTTP: `http://localhost:8080`
- JMX: `service:jmx:rmi:///jndi/rmi://localhost:9012/jmxrmi`
- デバッグ: `localhost:8787`

### JMX接続手順（docker-compose-spring-boot）

1. compose 起動後、`http://localhost:8080/tests/index.html` にアクセスしてアプリ起動を確認する。
2. JMXクライアント（JMC / jconsole）から以下へ接続する。

```
service:jmx:rmi:///jndi/rmi://127.0.0.1:9012/jmxrmi
```

3. `org.seasar.mayaa:type=CacheControl,name=*` の MBean が表示されることを確認する。

### 環境変数（必要に応じて上書き）
- `MAYAA_VERSION`: 参照する `mayaa` バージョン（既定: `2.0.0-SNAPSHOT`）
- `JMX_HOSTNAME`: JMXのRMIホスト名（`-Djmx.hostname` に渡す。既定: `127.0.0.1`）
- `APP_ARGS`: `spring-boot:run` への追加引数
