# Mayaaの監視および管理

## JMX

MayaaではJMX(Java Management Extensions)を経由して内部状態の監視や一部の動作を変更することができる。ObjectNameのドメイン名は org.seasar.mayaa である。

### 定義されているオブジェクト

| ObjectName                                                 | 説明 |
|------------------------------------------------------------|-----|
| org.seasar.mayaa:type=CacheControl,name=specificationCache | Page および Template のビルド結果のキャッシュ |
| org.seasar.mayaa:type=CacheControl,name=CompiledScript     | スクリプトのコンパイル結果のキャッシュ |
| org.seasar.mayaa:type=CacheControl,name=SourceCompiledScript | ソーススクリプトコンパイル結果のキャッシュ |
| org.seasar.mayaa:type=CacheControl,name=JspTagPool         | JSPタグライブラリのインスタンスプール |
| org.seasar.mayaa:type=CacheControl,name=PrefixAwareName    | 名前空間付きの属性名のキャッシュ |
| org.seasar.mayaa:type=MayaaEngine                          | Mayaa全体の挙動 |

#### 属性・操作 (type=CacheControl)

実装インタフェース: org.seasar.mayaa.management.CacheControlMXBean

| 属性名       | 説明                                         |  変更可  |
|-------------|----------------------------------------------|---------|
| ClassName   | キャッシュの実装クラス名。                        |   |
| CurrentSize | 現在キャッシュとして保持されているオブジェクト数。    |   |
| RequestCount | キャッシュ参照回数。                            |   |
| HitCount    | キャッシュヒットした回数。                        |   |
| HitRate     | キャッシュヒット率。                              |   |
| MissCount   | キャッシュミスした回数。                          |   |
| MissRate    | キャッシュミス率。                                |   |
| LoadSuccessCount | ロード成功回数。                             |   |
| LoadFailureCount | ロード失敗回数。                             |   |
| TotalLoadTime | ロード時間合計(ナノ秒)。                        |   |
| EvictionCount | 追い出し回数。                                |   |
| StatsEnabled | 統計取得が有効かどうか。                         |   |
| MaximumSizeManageable | 最大保持数の動的変更に対応しているか。     |   |
| RetainSize  | キャッシュから追い出さずに保持する最大オブジェクト数。 | O |

| 操作名       | 説明                                         |
|-------------|----------------------------------------------|
| invalidateAll | 全エントリを破棄する。 |

#### 属性・操作 (type=CacheControl)

実装インタフェース: org.seasar.mayaa.management.CacheControlMXBean

| 属性名         | 説明                              |  変更可  |
|---------------|-----------------------------------|--------|
| DebugEnabled  | デバッグ設定                        |  O     |
| DumpEnabled   | ダンプ設定                          |  O     |
| Version       | バージョン(POMのバージョン)           |        |

| 操作名       | 説明                                         |
|-------------|----------------------------------------------|
| （なし）     | |

### JMXの有効化

JMXを有効にする場合は、JVMオプションとして下記のシステムプロパティを設定して起動します。

### 同一ホスト内からのアクセスのみを受け付ける場合
```sh
-Dcom.sun.management.jmxremote \
-Dcom.sun.management.jmxremote.port=9012
-Dcom.sun.management.jmxremote.rmi.port=9012 \
-Dcom.sun.management.jmxremote.ssl=false \
-Dcom.sun.management.jmxremote.authenticate=false \
```

### 異なるホストからのアクセスを受け付ける場合
```sh
-Dcom.sun.management.jmxremote \
-Dcom.sun.management.jmxremote.port=9012
-Dcom.sun.management.jmxremote.rmi.port=9012 \
-Dcom.sun.management.jmxremote.ssl=false \
-Dcom.sun.management.jmxremote.local.only=false \
-Dcom.sun.management.jmxremote.authenticate=false \
-Djava.rmi.server.hostname=${自ホストのホスト名またはIPアドレス} \
```
