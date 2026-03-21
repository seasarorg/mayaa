# Issue #110: `${}` 自動エスケープ機能の仕様

| 項目 | ステータス |
|------|-----------|
| 文書ステータス | draft |
| 実装ステータス | 未実装 |

## 概要

Mayaa テンプレートにおいて、`${}` で出力する値に対して **XSS（クロスサイトスクリプティング）脆弱性を自動で防ぐ** ためのエスケープ処理を提供します。

埋め込み箇所（HTML 本体、属性値、JavaScript 内など）を自動判別し、それぞれのコンテキストに適切なエスケープ方法を適用します。

---

## 1. 埋め込み箇所別のエスケープ方法（自動判別）

### 1.1 HTML 本体内での出力

**テンプレート例**:
```html
<p>${ user.name }</p>
<div>${ product.description }</div>
```

**実行例**（自動エスケープ有効時）:

| 入力値 | 出力結果 |
|--------|---------|
| `"Tom & Jerry"` | `Tom &amp; Jerry` |
| `"<b>bold</b>"` | `&lt;b&gt;bold&lt;/b&gt;` |
| `"<script>alert('xss')</script>"` | `&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;` |

**適用されるエスケープ**: HTML エンティティ参照
- `<` → `&lt;`
- `>` → `&gt;`
- `&` → `&amp;`

---

### 1.2 HTML 属性値内での出力

**テンプレート例**:
```html
<a href="${ url }">Link</a>
<img alt="${ description }" />
<input value="${ userInput }" />
```

**実行例**（自動エスケープ有効時）:

| 入力値 | 出力結果 |
|--------|---------|
| `"https://example.com?x=1&y=2"` | `href="https://example.com?x=1&amp;y=2"` |
| `'data"hack'` | `alt="data&quot;hack"` |
| `'<img src=x onerror=alert(1)>'` | `value="&lt;img src=x onerror=alert(1)&gt;"` |

**適用されるエスケープ**: HTML エンティティ参照 + クォートエスケープ
- `"` → `&quot;`
- `'` → `&#39;` または `&apos;`
- `<` `>` `&` → 上記に同じ

---

### 1.3 JavaScript 内での出力

**テンプレート例**:
```html
<script>
  var name = '${ user.name }';
  var message = "${ msg }";
  var data = ${ dataAsJson };
</script>
```

**実行例**（自動エスケープ有効時）:

| 入力値 | 出力結果 |
|--------|---------|
| `"O'Reilly"` (シングルクォート内) | `var name = 'O\'Reilly'` |
| `"Say \"Hi\""` (ダブルクォート内) | `var message = "Say \"Hi\""` |
| `"Line1\nLine2"` (改行) | `var msg = "Line1\nLine2"` |
| `{"x": 1, "y": null}` (JSON形式) | `var data = {"x": 1, "y": null}` |

**適用されるエスケープ**: JavaScript 文字列エスケープ
- シングルクォート内: `'` → `\'`
- ダブルクォート内: `"` → `\"`
- 改行文字: `\n` → `\\n`
- タブ文字: `\t` → `\\t`
- JSON 形式: RFC 8259 準拠（特別なエスケープ処理なし）

---

### 1.4 CSS 内での出力

**テンプレート例**:
```html
<style>
  .message { color: ${ hexColor }; }
  .item { content: "${ label }"; }
</style>
```

**実行例**（自動エスケープ有効時）:

| 入力値 | 出力結果 |
|--------|---------|
| `"#FF0000"` | `.message { color: #FF0000; }` |
| `"red"` | `.message { color: red; }` |
| `"'; background: url(x)" ` | `.item { content: "\'; background: url(x)"; }` |

**適用されるエスケープ**: CSS 値エスケープ（コンテキスト依存）
- CSS プロパティ値: バリデーション＋エスケープ
- CSS 文字列内: シングル/ダブルクォートをエスケープ

---

### 1.5 textarea/pre タグ内での出力

**テンプレート例**:
```html
<textarea>${ comment }</textarea>
<pre>${ code }</pre>
```

**実行例**（自動エスケープ有効時）:

| 入力値 | 出力結果 |
|--------|---------|
| `"Hello & goodbye"` | `<textarea>Hello &amp; goodbye</textarea>` |
| `"<tag>content</tag>"` | `<pre>&lt;tag&gt;content&lt;/tag&gt;</pre>` |
| `"Line 1\nLine 2"` | `<textarea>Line 1\nLine 2</textarea>` |

**適用されるエスケープ**: HTML エンティティエスケープ
- 目的: テキスト表示領域内でも HTML メタ文字を無効化
- 例: `<` `>` `&` は常にエスケープし、意図しない HTML 構文展開を防止

---

## 2. グローバル設定（有効化 / 無効化）

### 2.1 設定ファイルでの制御

`service-provider.dicon` に以下の設定を追加します：

```xml
<provider>
  <scriptEnvironment 
      class="org.seasar.mayaa.impl.cycle.script.rhino.ScriptEnvironmentImpl">
    
    <!-- 自動エスケープ機能の有効化 (デフォルト: false) -->
    <parameter name="autoEscapeEnabled" value="true|false" />
    
    <!-- 既存エスケープ済み値の検出レベル (デフォルト: normal)
         normal: HTML エンティティ、JSON/バックスラッシュエスケープのいずれか 1 つのパターンで判定
         strict: 異なるカテゴリから複数パターンの共存を要求（誤検知を減らす）
    -->
    <parameter name="escapeDetectionLevel" value="normal|strict" />
    
  </scriptEnvironment>
</provider>
```

### 2.2 設定値の説明

| パラメータ | 値 | 説明 |
|----------|-----|------|
| `autoEscapeEnabled` | `false` (初期値) | 自動エスケープ無効。従来の動作（エスケープなし） |
| `autoEscapeEnabled` | `true` | 自動エスケープ有効。コンテキストに応じてエスケープ適用 |
| `escapeDetectionLevel` | `normal` | 以下のいずれか 1 つのパターンで既にエスケープ済みと判定：<br/>- HTML エンティティ: `&lt;` `&gt;` `&quot;` `&#39;` `&amp;`<br/>- JSON/バックスラッシュ: `\"` `\\` `\n` `\t` `\r` `\b` `\f` `\/` `\'` `\uXXXX` |
| `escapeDetectionLevel` | `strict` | 複数パターンの共存を要求。例：`&lt;` AND `&quot;` など、異なるカテゴリから複数必要 |

※ Scope マクロはコンパイル前に `${...}` 系へリライトされるため、最終的なエスケープ挙動はリライト後の記法に従います。
（`MAYAA_SCOPE(...) -> ${...}`、`MAYAA_SCOPE_AS_STRING(...) -> ${String(...)}`、`MAYAA_SCOPE_RAW(...) -> ${=...}`）

---

## 3. ページ単位での制御

### 3.1 HTML メタコメントを用いた設定

HTML ファイルの先頭に、そのページでの自動エスケープ動作を指定できます：

```html
<!-- m:autoEscape="true" -->
<!DOCTYPE html>
<html>
  <head>
    <title>ページタイトル</title>
  </head>
  <body>
    <!-- このページでは自動エスケープが有効 -->
    <p>${ user.name }</p>
  </body>
</html>
```

**設置箇所**: HTML ファイルの最初（DOCTYPE 前推奨）

**書式**: `<!-- m:autoEscape="true|false" -->`

### 3.2 グローバル設定との優先度

ページ単位の設定 > グローバル設定

```
例:
- グローバル設定: autoEscapeEnabled = false
- ページ指定: <!-- m:autoEscape="true" -->
→ このページでは「true」が有効
```

---

## 4. 埋め込み箇所ごとの明示指定

### 4.0 前提（構文色付け / Linter について）

`${ ... }` および `${= ... }` の内部は、Mayaa テンプレート式として扱います。
この領域は JavaScript の静的解析対象外とし、構文色付けや Linter エラー非発生は要件に含めません。

そのため、非エスケープ明示には `${=...}` 記法を採用します。

ただし、`<script>` 内に限っては、構文色付け/Linter を有効化しやすい専用記法（4.3）をオプションで提供します。

### 4.1 デフォルト動作（自動判別）

```html
<!-- 通常: コンテキストに応じて自動エスケープ適用 -->
${ user.name }
```

### 4.2 非エスケープを明示的に指定: `${=...}` 記法

`${}` の中に `=` を記入することで、**エスケープを行わない** ことを明示します：

```html
<!-- 信頼できる HTML 内容を出力（エスケープなし） -->
${= user.htmlContent }

<!-- テンプレートメソッドの結果（エスケープなし） -->
${= template.render() }

<!-- 既に手動でエスケープ処理済みの値（エスケープなし） -->
${= sanitizer.clean(userInput) }
```

### 4.3 script 向け Scope マクロ記法（任意）

`<script>` 内で `${` 自体を使わず、構文色付け/Linter を効かせたい場合は次を使用します：

```js
MAYAA_SCOPE(expression)     // 自動エスケープ
MAYAA_SCOPE_AS_STRING(expression) // String(...) へ変換して出力
MAYAA_SCOPE_RAW(expression) // 非エスケープ（${=...} 相当）
```

**例**:
```html
<script>
  var name = MAYAA_SCOPE(user.name);
  var enabled = MAYAA_SCOPE_AS_STRING(feature.enabled);
  var users = MAYAA_SCOPE_RAW(json.stringify(userList));
  var count = MAYAA_SCOPE(userList.length);
</script>
```

**出力イメージ**:

```js
MAYAA_SCOPE(true)                // -> true
MAYAA_SCOPE_AS_STRING(true)      // -> true

MAYAA_SCOPE(1)                   // -> 1
MAYAA_SCOPE_AS_STRING(1)         // -> 1

MAYAA_SCOPE("文字列")           // -> "文字列"
MAYAA_SCOPE_AS_STRING("文字列") // -> 文字列
```

**動作**:
- 編集時/静的解析時: 通常の JavaScript 関数呼び出しとして扱われるため、IDE の構文色付け/Linter を適用できる
- コンパイル前: Scope マクロを次のルールでテキストリライトする
  1. `MAYAA_SCOPE(expr)` → `${expr}`
  2. `MAYAA_SCOPE_AS_STRING(expr)` → `${String(expr)}`
  3. `MAYAA_SCOPE_RAW(expr)` → `${=expr}`
- レンダリング時: リライト後の `${...}` / `${=...}` の通常ルールで評価される
- `MAYAA_SCOPE_AS_STRING(...)` は「常に文字列リテラル化」ではなく、`${String(...)}` として評価される

**`MAYAA_SCOPE_AS_STRING(...)` の補足**:
- `MAYAA_SCOPE_AS_STRING(expr)` は `${String(expr)}` にリライトされる
- そのため値は `String(...)` の評価結果として扱われ、追加で二重引用符を付与する専用処理は行わない

**運用ルール**:
- `<script>` 内のみで使用する
- 実装はコンパイル前リライト方式であるため、最終挙動は `${...}` / `${=...}` の既存仕様に従う
- 引数は Mayaa 式として記述（例: `user.name`, `json.stringify(obj)`）
- `MAYAA_SCOPE_AS_STRING(...)` は文字列化したいフラグ値・数値・ID などに使う
- `MAYAA_SCOPE_RAW(...)` は信頼できる値のみに限定
- Linter で `MAYAA_SCOPE` / `MAYAA_SCOPE_AS_STRING` / `MAYAA_SCOPE_RAW` を既知グローバルに登録（例: ESLint `globals: { MAYAA_SCOPE: "readonly", MAYAA_SCOPE_AS_STRING: "readonly", MAYAA_SCOPE_RAW: "readonly" }`）

---

## 5. 記法の使い分けガイド

### 5.1 推奨される使い方

| 用途 | 記法 | 説明 | 例 |
|------|------|------|----|
| **ユーザー入力** | `${ value }` | 自動エスケープで保護（推奨） | `${ form.name }` |
| **先に手動エスケープ済み** | `${= escaped }` | スキップ指定 | `${= util.escape(input) }` |
| **テンプレート出力** | `${= template.render() }` | 信頼できるテンプレート | `${= layout.getContent() }` |
| **JSON/JavaScript 値** | `${= json }` | JS エスケープ対象外 | `${= dataModel }` |
| **script 内で JS 値を出力** | `MAYAA_SCOPE(...)` | コンパイル前に `${...}` へリライトし、以後は `${...}` の通常ルールで評価 | `var count = MAYAA_SCOPE(totalCount);` |
| **script 内で文字列化して出力** | `MAYAA_SCOPE_AS_STRING(...)` | コンパイル前に `${String(...)}` へリライトし、`String(...)` 結果を `${...}` として評価 | `var id = MAYAA_SCOPE_AS_STRING(user.id);` |
| **script 内で非エスケープ値を出力** | `MAYAA_SCOPE_RAW(...)` | コンパイル前に `${=...}` へリライトし、非エスケープ出力 | `var x = MAYAA_SCOPE_RAW(jsonString);` |

※ `MAYAA_SCOPE(...)` / `MAYAA_SCOPE_AS_STRING(...)` / `MAYAA_SCOPE_RAW(...)` は、いずれも通常の JavaScript 関数呼び出しとして記述できるため、同様に構文色付け/Linter の対象になります。

### 5.2 実装例

```html
<!-- 安全な実装例（自動エスケープ有効時） -->
<div class="message">
  <!-- ユーザー入力は自動エスケープ -->
  <p>${ user.getMessage() }</p>
  
  <!-- 信頼できる HTML は明示指定 -->
  <div>${= cms.getContent() }</div>
  
  <!-- JavaScript は自動判別（script 内なら JSON エスケープ） -->
  <script>
    /* global MAYAA_SCOPE, MAYAA_SCOPE_AS_STRING, MAYAA_SCOPE_RAW: readonly */
    
    // 静的解析を通したい場合は Scope マクロ記法を使う
    var data = MAYAA_SCOPE_RAW(json.stringify(users));
    var count = MAYAA_SCOPE(userList.length);
    var featureFlag = MAYAA_SCOPE_AS_STRING(feature.enabled);
  </script>
</div>
```

### 5.3 ESLint 設定例

Scope マクロ記法を使う場合、プロジェクト全体で `MAYAA_SCOPE` / `MAYAA_SCOPE_AS_STRING` / `MAYAA_SCOPE_RAW` を定義済みグローバルとして登録することで、Linter エラーを回避できます：

**.eslintrc.json**:
```json
{
  "env": {
    "browser": true,
    "es2021": true
  },
  "globals": {
    "MAYAA_SCOPE": "readonly",
    "MAYAA_SCOPE_AS_STRING": "readonly",
    "MAYAA_SCOPE_RAW": "readonly"
  },
  "rules": {
    "no-undef": "error"
  }
}
```

または、個別の `<script>` ブロックにコメントで指定：

```html
<script>
  /* global MAYAA_SCOPE, MAYAA_SCOPE_AS_STRING, MAYAA_SCOPE_RAW: readonly */
  
  var result = MAYAA_SCOPE_RAW(dataModel);
  var label = MAYAA_SCOPE_AS_STRING(status.label);
</script>
```

---

## 6. 既存コードの互換性

### 6.1 自動エスケープ: OFF（デフォルト）

自動エスケープ機能は **デフォルトで OFF** です。既存のテンプレートコードは影響を受けません：

```xml
<!-- デフォルト設定 -->
<parameter name="autoEscapeEnabled" value="false" />
```

### 6.2 自動エスケープ: ON にしても影響なし

自動エスケープを ON にした場合でも、既に手動でエスケープ処理されている値は **自動的に検出されてスキップ** されます（多重エスケープを防止）：

```html
<!-- 既存コード1: HTML エスケープ済み -->
<p>${ util.htmlEscape(userInput) }</p>
<!-- util.htmlEscape() の戻り値が "&lt;" 等を含む
     → 自動検出: 既にエスケープ済み
     → スキップ: 多重エスケープなし ✓ -->

<!-- 既存コード2: JSON エスケープ済み -->
<script>
  var data = ${ json.stringify(obj) };
</script>
<!-- stringify() の戻り値が "\"" や "\\" を含む
     → 自動検出: JSON エスケープ済み
     → スキップ: 多重エスケープなし ✓ -->

<!-- 既存コード3: バックスラッシュエスケープ済み -->
<p>${ util.escapeBackslash(text) }</p>
<!-- escapeBackslash() の戻り値が "\'" や "\n" を含む
     → 自動検出: バックスラッシュエスケープ済み
     → スキップ: 多重エスケープなし ✓ -->
```

---

## 7. 段階的な導入

### 7.1 推奨される導入ステップ

**Step 1: テスト環境で有効化**
```xml
<parameter name="autoEscapeEnabled" value="true" />
```
既存テストを実行。互換性を確認。

**Step 2: 新規ページで使用**
新規ページでのみ `<!-- m:autoEscape="true" -->` を指定。

**Step 3: 既存ページへの展開**
問題なければ、段階的に既存ページへ適用。

**Step 4: グローバル有効化**
全ページで機能が動くことを確認して、グローバル設定を ON に変更。

---

## 8. トラブルシューティング

### Q: エスケープが二重になった（`&amp;lt;` のように見える）

**A: 原因と対処**

- **原因**: 手動エスケープと自動エスケープの重複
- **対処 1**: `<!-- m:autoEscape="false" -->` でそのページのみ OFF
- **対処 2**: `${=...}` で該当箇所をスキップ指定
- **対処 3**: 既存のエスケープ util を `util.escape()` に統一して自動判別に任せる

### Q: エスケープされるべき値がされていない

**A: 原因と対処**

- **原因**: 自動エスケープ OFF、またはコンテキスト判別ミス
- **対処**: `autoEscapeEnabled = true` を確認し、ページ単位設定も確認

### Q: JSON データが正しくパースされない

**A: 原因と対処**

- **原因**: JavaScript 内で JSON 値がエスケープされた（`\"` `\\` `\n` など）
- **対処 1**: `script` 内では Scope マクロ記法を優先
- **対処 2**: `${=...}` で指定（スキップ）してエスケープ処理を回避
- **対処 3**: JSON エスケープ済みと自動検出されれば、自動的にスキップ（多重エスケープ防止）
- **補足**: `${...}` 内部の JS 構文色付け/Linter 連携は要件外

```html
<!-- ✓ 推奨: JSON は信頼できるデータなのでスキップ指定 -->
<script>
  var data = MAYAA_SCOPE_RAW(json.stringify(users));
</script>

<!-- ✓ OK: JSON エスケープ（\"や\\）が含まれていれば自動検出スキップ -->
<script>
  var data = MAYAA_SCOPE(sanitizer.toJson(users));  // 内部で JSON エスケープ済み
</script>
```

### Q: 既にエスケープ済みの値が誤検出/検出漏れされる

**A: 原因と対処**

- **対処1: 誤検知が多い** → `escapeDetectionLevel` を `strict` に変更
  - `normal` では `\"` だけで判定 → 偽陽性の可能性
  - `strict` では `\"` AND 別カテゴリ（`&lt;` など）を要求 → 厳密化

```xml
<!-- より安全な判定 -->
<parameter name="escapeDetectionLevel" value="strict" />
```

- **検出漏れがある** → `${=...}` で明示指定
  - ローカルカスタム エスケープ関数を使用している場合、独自パターンは自動検出できない
  - その場合は `${=...}` で明示的にスキップ指定

```html
<!-- カスタム escape 関数の結果 -->
<p>${= customEscaper.process(input) }</p>
```

---

## 9. エスケープ検出の詳細（実装者向け）

### 9.1 検出対象パターン一覧

自動エスケープをスキップする判定で使用される、既にエスケープ済みと判断するパターン：

**HTML エンティティ**:
- `&lt;` `&gt;` `&quot;` `&#39;` `&apos;` `&amp;`
- 数値文字参照: `&#NNN;` `&#xHHH;`

**JSON / バックスラッシュエスケープ**:
- JSON エスケープ: `\"` `\\` `\n` `\t` `\r` `\b` `\f` `\/`
- Unicode エスケープ: `\uXXXX`
- シングルクォートエスケープ: `\'`
- その他バックスラッシュ: `\0` `\x` など

### 9.2 検出ロジック

**normal モード**（デフォルト）:
```
値にこれらのパターンのうち「いずれか 1 つ以上」が含まれれば、既にエスケープ済みと判定
→ 多重エスケープを最小化するが、誤検知の可能性あり
```

**strict モード**:
```
異なるカテゴリから複数パターンの共存を要求
例: ("&lt;" OR "&quot;") AND ("\"" OR "\\") が共存 → 既にエスケープ済み
→ 誤検知を大幅に削減。代わりに検出漏れの可能性がやや増加
```

### 9.3 推奨される運用

1. **新規プロジェクト**: `normal` モード（問題が発生したら `strict` へ）
2. **既存大規模プロジェクト**: `strict` モード（誤検知回避）
3. **統一ルール確立後**: 手動エスケープ箇所に `${=...}` を明示 → 検出パターンに依存しない

### 9.4 Scope マクロのリライト対象（実装固定）

`MAYAA_SCOPE(...)` / `MAYAA_SCOPE_AS_STRING(...)` / `MAYAA_SCOPE_RAW(...)` は、`ScriptUtil.compile(text)` の入力文字列に対してコンパイル前にリライトする。

- リライトルール:
  - `MAYAA_SCOPE(expr)` → `${expr}`
  - `MAYAA_SCOPE_AS_STRING(expr)` → `${String(expr)}`
  - `MAYAA_SCOPE_RAW(expr)` → `${=expr}`
- 運用上の推奨対象: `<script> ... </script>` の本文テキスト
- 外部 JavaScript ファイル（`src="..."`）は Mayaa のテンプレートコンパイル対象外のため、本リライト対象外

この仕様は「macro を早期に通常記法へ寄せる」ことを目的とし、以後の評価・エスケープは `${...}` / `${=...}` の既存仕様に統一する。

---

## 10. 今後の拡張

- [ ] データベースクエリ内での SQL インジェクション防止
- [ ] URL クエリパラメータの自動エンコーディング
- [ ] 独自エスケープルールのプラグイン化
