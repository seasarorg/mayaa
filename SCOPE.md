# 変数とスコープについて

外部仕様ドキュメント
http://mayaa.seasar.org/documentation/implicit_object.html

Mayaaではページ(*.mayaaa)をレンダリングする際にスクリプト（標準ではJavaScript）によるロジックの記述や
変数の定義および参照を行うことができる。
スクリプトから参照できる値としては、スクリプト内で宣言された変数だけでなく、Javaサーブレット仕様で規定されている
各種スコープの属性へもアクセスすることも可能である。

値へのアクセスはスコープという概念に基づいて管理されており、保管される期間や参照可能な範囲が異なっている。
Mayaaで定義されているスコープは一部はサーブレット仕様の定義と対応しているほか、処理しているリクエストの
パラメータやヘッダ情報などにアクセスするための独自のスコープが定義されている。

## スコープの種類

| スコープ | スクリプトでの名前空間名 |暗黙的な参照対象 |
|--------|----------------------|-----------|
| アプリケーションスコープ | application |○|
| セッションスコープ | session | ○|
| リクエストスコープ | request | ○|
| ページスコープ | page | ○|
| パラメータスコープ | param | ×|
| ヘッダスコープ | header | ×|
| コンポーネント引数スコープ| binding | ×|
| 標準スコープ | _（下線） | ×|

## JavaScriptのスコープとMayaaのスコープの関係
明示的にスコープ名を指定した変数の定義および参照のほか、スコープを明示しない暗黙的な変数の
定義および参照が可能である。
スコープは階層構造を持っており、暗黙的な定義および参照の場合は参照している箇所から一番近くて
参照範囲の狭いものからページスコープ、リクエストスコープ、セッションスコープ、
アプリケーションスコープの順に使用される。標準スコープ "_" はJSの予約語と同じ属性名の参照を
明示的に行うために使用される。

## Mayaaにおけるスコープの実装

Mayaaでのスコープ管理はスコープごとにそれぞれ `PageAttributeScope`, `RequestScope`,
`SessionScope`, `ApplicationScope` インタフェースを通じて操作される。各々の実装クラスでは
Rihno の `ScriptableObject` を継承して、`get(String name, Scriptable start)` を
オーバーライドすることでMayaaが管理しているServletのスコープ管理とRihnoの変数スコープを仲立ちしている。

## スコープからの変数の参照

`TextCompiledScriptImpl.execute()` メソッドによりノードに関連づいたJSを実行する。

Rihno内の処理で実行箇所に最も近いスコープである`PageAttributeScope`から順次親のスコープをたどり、
`NativeServiceCycle`というスコープ管理オブジェクトでServletの領域のスコープおよび
Javaオブジェクトのスコープ管理オブジェクトを参照する。
それでも見つからない場合は NativeObject でJS領域のオブジェクトを参照する。

JS内で変数を参照しようとしたとき、次の流れでスコープが特定される。
スコープの特定処理を実施しているのは `CycleUtil.findStandardAttributeScope(name)` である。

1. 実行Mayaaタグ内の PageAttributeScope
2. 実行Mayaaタグ内の PageAttributeScope
　　　　：
3. NativeServiceCycle
   変数名が ”page”, "request", "session", "application" ならそれぞれ 
   `PageAttributeScope`, `RequestScope`, `SessionScope`, `ApplicationScope` をスコープ管理オブジェクトとして返す。
   変数名が ”param”, "header", "binding", "_" ならそれぞれ 
   ScriptEnvironmentImplに初期化時に追加される ParamScope, HeaderScope, BindingScope, WalkerStandardScope を返す。
   いずれでもない時は RequestScope を返却する。

Rootのページスコープの属性として現在実行中のページスコープが `__current__` というキーで保管されている。
