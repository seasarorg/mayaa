# 性能改善計画（JFR分析ベース）

最終更新: 2026-03-01
対象記録: `flight_recording_1270019012.jfr`（60秒）

## 背景
- 現在はキャッシュ改善を優先中。
- 本計画は「キャッシュ改善が一区切り後」に着手する性能改善のバックログ。
- ただし、キャッシュ改善と整合する低リスク項目は先行で反映する。

## JFR要約（今回の観測）
- GC
  - 発生: 3回
  - 最大停止: 18.0ms
  - 結論: GCは主要ボトルネックではない。
- 待機イベント
  - `jdk.SocketRead` が RMI/JMX スレッドに集中（待機時間が大きい）
  - `jdk.ThreadPark` が `http-nio-8080-exec-*` で長時間観測
  - スタック上は `ForkJoinPool.managedBlock` 経由が目立つ
- 割り当て（アプリ関連）
  - `QName/Specification/Namespace` 周辺の呼び出し頻度が高い
  - 文字列化・hashCode・正規表現/文字列処理が目立つ
  - Rhino (`org.mozilla.javascript`) の文字列化経路も観測

## 改善方針（優先度）

### P0: 計測ノイズ分離
1. JMX/RMI監視負荷を切った条件で再計測（ベースライン）
2. 監視あり/なしの比較を残し、アプリ固有の待機時間を分離

### P1: 同期待機の削減
1. リクエストスレッドでの `ForkJoinPool` 待ち（`managedBlock`）を調査
2. `join/get` の同期待機を減らし、必要に応じて専用 `ExecutorService` へ分離
3. 可能な範囲で非同期チェーンを維持（最終同期点を後段へ寄せる）

### P2: オブジェクト生成/ハッシュ計算コストの削減
1. `QName/Specification/Namespace` 周辺の `hashCode`/文字列化の再計算抑制
2. regexの再コンパイル回避（`Pattern`再利用）
3. XMLエスケープや `StringBuilder` 利用箇所の不要変換削減
4. 割り当てホットスポット（型） byte[], String, char[], Object[] が多い。アプリ寄りでは org.mozilla.javascript.Context, org.seasar.mayaa.impl.cycle.script.rhino.PageAttributeScope, org.seasar.mayaa.impl.engine.specification.NamespaceImpl が目立つ。

### P3: Rhino実行経路の整理
1. `numberToString` 等の変換多発箇所の呼び出し削減
2. 動的クラス生成・反射経路の再評価（必要箇所のみ）

## 実施候補（キャッシュ改善に関連し低リスク）
- `NamespaceImpl` に `hashCode()` を実装（`equals` と整合）
  - `equals` をオーバーライドしているため、ハッシュベース利用時の整合性担保が目的。
- `QNameImpl.hashCode()` を `Objects.hash(...)` から軽量計算へ変更（アロケーション抑制）
- 上記に対応する回帰テストを追加

※本計測ブランチでは、まず計測基盤・再現性確保を優先し、上記のコード最適化は分離して扱う。

## 次回（キャッシュ改善後）の実施手順
1. JMX最小構成で同条件JMeterを再実行し、JFRを再取得
2. `ThreadPark` の待機元（どの `join/get` か）をコード上で突合
3. P1の最小パッチを作成（機能変更なし）
4. もう一度JFR比較（待機時間・スループット・割り当て）

## 完了条件（Done）
- ベースライン比で、以下のいずれかを達成
  - `http-nio-8080-exec-*` の累積 `ThreadPark` 時間を 20%以上削減
  - 同一負荷でスループット向上（または p95 応答時間改善）
- 変更は既存テストと互換性を維持すること
