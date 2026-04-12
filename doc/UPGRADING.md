# Mayaa アップグレードガイド

このドキュメントでは、Mayaaの旧バージョンから新しいバージョンへアップグレードする際の注意事項と手順を説明します。

## 目次
- [1.3.x から 2.0.0 へのアップグレード](#13x-から-200-へのアップグレード)
  - [Java 21 / Jakarta EE 10 への移行（必須要件変更）](#java-21--jakarta-ee-10-への移行必須要件変更)
  - [依存ライブラリの変更](#依存ライブラリの変更)
  - [`DefaultLayoutTemplateBuilder.setupExtends` のシグネチャ変更（非互換）](#defaultlayouttemplatebuildersetupextends-のシグネチャ変更非互換)
  - [`ParentSpecificationResolver` の親仕様 `beforeRender` / `afterRender` 実行サポート（動作変更）](#parentspecificationresolver-の親仕様-beforerender--afterrender-実行サポート動作変更)
  - [自動エスケープ機能（Issue #110）導入時の互換性注意点](#自動エスケープ機能issue-110導入時の互換性注意点)
  - [Scope マクロの追加（`MAYAA_SCOPE` 等）](#scope-マクロの追加mayaa_scope-等)
  - [診断・プロファイリング機能の追加（新機能）](#診断プロファイリング機能の追加新機能)
- [1.2.x から 1.3.0 へのアップグレード](#12x-から-130-へのアップグレード)
- [1.1.x から 1.2 へのアップグレード](#11x-から-12-へのアップグレード)
- [1.1.32 以前から 1.1.33 以降へのアップグレード](#1132-以前から-1133-以降へのアップグレード)

---

## 1.3.x から 2.0.0 へのアップグレード

### Java 21 / Jakarta EE 10 への移行（必須要件変更）

**影響範囲:** すべての利用者

#### Java バージョン要件

| | 旧 (1.3.x) | 新 (2.0.0) |
|---|---|---|
| 最低必須 Java バージョン | Java 8 以上 | **Java 21 以上** |
| ビルドターゲット | Java 8 | Java 21 |

Java 21 未満の実行環境では動作しません。

#### Jakarta パッケージへの移行（非互換）

内部実装の `javax.*` パッケージ参照が `jakarta.*` に変更されました。

| 変更前 | 変更後 |
|---|---|
| `javax.servlet.*` | `jakarta.servlet.*` |
| `javax.servlet.jsp.*` | `jakarta.servlet.jsp.*` |

**影響する実行環境:**

| サーバー | 最低バージョン |
|---|---|
| Apache Tomcat | 10.x 以上 |
| WildFly | 27 以上 |
| Spring Boot | 3.x 以上 |

Tomcat 9 以下、WildFly 26 以下、Spring Boot 2.x は対象外です。

#### TLD / JSTL の移行

- JSTL の名前空間が `jakarta.tags.*` に変更されました
- TLD スキーマは Jakarta EE 9/10/11 向けのバンドルファイルを同梱しています
- JSTL の実装ライブラリは `jakarta.servlet.jsp.jstl`（`org.glassfish.web:jakarta.servlet.jsp.jstl:3.0.x`）が必要です

---

### 依存ライブラリの変更

#### 追加された依存

| ライブラリ | バージョン | 用途 |
|---|---|---|
| `com.github.ben-manes.caffeine:caffeine` | 2.9.3 | キャッシュ実装（commons-collections の代替） |
| `jakarta.platform:jakarta.jakartaee-api` | 10.0.0 | Jakarta EE 10 API（provided） |

#### 削除された依存

| ライブラリ | 理由 |
|---|---|
| `commons-collections:commons-collections` | Caffeine に置換 |

#### Xerces 依存の部分除去

PR #129 により、HTML テンプレートパーサーパイプラインから NekoHTML 固有部分を除く Xerces 依存を排除しました。

- `HtmlTemplateParser` は Xerces `AbstractSAXParser` の代わりに JDK SAX 標準実装を使用するよう変更
- `AdditionalSAXParser` は JDK `SAXParserFactory` ベースに変更
- **残存**: NekoHTML パーサースタック固有クラス（`NekoHtmlParser`, `TemplateScanner`, `AdditionalHandlerFilter`）は引き続き Xerces に依存しています

> **今後の予定:** `HtmlStandardScanner` ベースの新パーサーへ完全移行後に Xerces 依存を完全除去する予定です。

この変更は通常の利用では動作に影響しませんが、`TokenHandler` インターフェースを直接実装している場合は `errorReporter` 関連 API が削除されているため修正が必要です。

---

### `DefaultLayoutTemplateBuilder.setupExtends` のシグネチャ変更（非互換）

**影響範囲:** `DefaultLayoutTemplateBuilder` をサブクラス化して `setupExtends` または
`getMayaaNode` / `createMayaaNode` / `addExtends` をオーバーライドしている実装。

#### 変更の背景

旧実装では `setupExtends` がキャッシュ済みの `Page` インスタンスに
`m:extends` 属性を持つ mayaaNode を直接追加（ミューテート）していました。
Caffeine キャッシュ移行後、Page と Template のエビクションタイミングが独立したことで
「Page だけエビクションされた後に Template が再ビルドされず、mayaaNode が失われる」
問題が発生したため、設計を変更しました。

#### 新設計の概要

- **キャッシュ済みの `Page` インスタンスはイミュータブルとして扱う。**
  `setupExtends` は `Page` を直接ミューテートしなくなりました。
- デフォルトレイアウトのページ名は `TemplateImpl.setDynamicSuperPagePath(String)` を
  呼び出して **Template 自身** に保持します。
- 描画時に `RenderUtil` が Page の `.mayaa` 定義（`m:extends`）を優先し、
  定義がない場合のみ Template の動的設定を参照します。

#### サブクラスへの影響と対応方法

```java
// 旧: Page をミューテートして mayaaNode に m:extends を追加していた
@Override
protected void setupExtends(Template template) {
    Page page = template.getPage();
    SpecificationNode mayaaNode = getMayaaNode(page);
    if (isGenerateMayaaNode() && mayaaNode == null) {
        mayaaNode = createMayaaNode(page, template);   // Page に子ノード追加
        page.addChildNode(mayaaNode);
    }
    if (mayaaNode != null) {
        addExtends(page, mayaaNode);                   // Page の属性を書き換え
    }
}

// 新: Template に動的パスを設定する。Page は読み取り専用
@Override
protected void setupExtends(Template template) {
    // Page の .mayaa に m:extends が既にある場合は何もしない
    Page page = template.getPage();
    SpecificationNode mayaaNode = getMayaaNode(page);
    if (mayaaNode != null &&
            !StringUtil.isEmpty(SpecificationUtil.getAttributeValue(mayaaNode, QM_EXTENDS))) {
        return;  // .mayaa で明示定義済み → 優先される
    }

    // 動的なレイアウトパスを Template 側に設定
    String layoutPageName = resolveLayoutPageName(template);  // サブクラスで決定
    if (StringUtil.hasValue(layoutPageName) && template instanceof TemplateImpl) {
        ((TemplateImpl) template).setDynamicSuperPagePath(layoutPageName);
    }
}
```

`getMayaaNode(Page)` / `createMayaaNode(Page, Specification)` / `addExtends(Page, SpecificationNode)` は
`@Deprecated` になりました。新実装では使用しないでください。

---

### `ParentSpecificationResolver` の親仕様 `beforeRender` / `afterRender` 実行サポート（動作変更）

**影響範囲:** `ParentSpecificationResolver` を実装・使用しているアプリケーション

#### 1.3.x までの動き

`ParentSpecificationResolver` が親仕様チェーンを返す場合、返された親仕様の `beforeRender` / `afterRender` は**実行されていませんでした**。

#### 2.0.0 での変更

親仕様チェーンの `beforeRender` / `afterRender` が実行されるようになりました（PR #127 #131）。

実行順序は次のとおりです。

```
beforeRender: parent2（最外側）→ parent1 → page 自身（最内側）
afterRender:  page 自身（最内側）→ parent1 → parent2（最外側）
※ default.mayaa の beforeRender/afterRender はページ処理ごとではなく、リクエスト処理ごとに最外の一回のみ
```

この順序により、Rhino のスコープチェーン（内→外方向で変数を検索）に沿って、
`beforeRender` 内で宣言した変数を内側の仕様から外側の仕様の変数として参照できます。

> **注意:** 1.3.x では実行されていなかった `beforeRender` / `afterRender` が新たに呼ばれるようになります。`ParentSpecificationResolver` が返す親仕様に `beforeRender` / `afterRender` の処理が定義されている場合、動作が変わります。

---

### 自動エスケープ機能（Issue #110）導入時の互換性注意点

`autoEscapeEnabled=true` を有効化した場合、シングルクオート以外にも以下の影響が発生する可能性があります。

1. HTML本文・属性値で `<` `>` `&` が自動エスケープされる
2. クオートの扱いは出力コンテキストで異なる
    - HTML本文（HTML_BODY / TEXTAREA_PRE）: `"` と `'` はエスケープしない
    - HTML属性（HTML_ATTRIBUTE）: `"` と `'` をエスケープする（`&quot;`, `&#39;`）
3. 属性値中の `&` は HTML シリアライズ時に `&amp;` になるため、生HTML文字列や `outerHTML` の厳密比較結果が変わる（DOM属性値取得は通常影響軽微）
4. 既存の手動エスケープと併用すると二重エスケープになる場合がある
5. 既にエスケープ済み値の自動検出は誤検出/検出漏れの可能性がある
6. `${=...}` は常に非エスケープ出力のため、従来よりXSSリスクが顕在化しやすくなる

#### WriteProcessor の補足

`m:write` の `escapeXml=true` は、親要素の出力コンテキスト（HTML本文 / 属性 / script / style / textarea など）を自動判定してエスケープ方式を切り替えます。
特に `script` / `style` / `textarea` 配下で `m:write` を利用している場合は、出力文字列の変化（バックスラッシュエスケープやHTMLエスケープの差分）を確認してください。

段階導入を推奨します。

- まず `autoEscapeEnabled=false` のままでアップグレード
- 画面単位で `<!-- m:autoEscape="true" -->` を使って有効化対象を限定
- 文字列比較やスナップショットテストの期待値差分を確認
- 手動エスケープ箇所は `${=...}` を明示するか、実装を統一する

#### autoEscape 有効化後に `&` を含む属性値の比較が失敗する

**症状:**
- `href` や `src` の属性値を文字列比較すると期待値が一致しない
- 例: `?a=1&b=2` を期待していたが、出力HTML上は `?a=1&amp;b=2` になる

**原因:**
- HTML属性値として正しい形式にするため、`&` がエンティティ化される

**対応:**
1. 比較対象をDOM取得値に変更し、シリアライズ済みHTML文字列との直接比較を避ける

#### autoEscape 有効化後にクオート比較が失敗する

**症状:**
- HTML本文では `"` が `&quot;` にならない
- 属性値では `'` が `&#39;` になり、`"` が `&quot;` になる

**原因:**
- クオートのエスケープ方針が出力コンテキスト依存になっているため

**対応:**
1. 本文ノード比較と属性値比較を分けて期待値を管理する
2. `innerHTML` / `outerHTML` の厳密比較より、DOMの属性値取得やテキスト取得による比較を優先する

---

### Scope マクロの追加（`MAYAA_SCOPE` 等）

**影響範囲:** Mayaa スクリプトをテンプレートの `<script>` タグ内で記述している場合

PR #122 により、`<script>` タグ内での `${}` 記法の代替として Scope マクロが追加されました。
スクリプト内での記述は、Scope マクロを使うことで IDE（ESLint 等）の構文チェックを通した状態で記述できます。

#### 追加されたマクロ

| マクロ | 説明 |
|---|---|
| `MAYAA_SCOPE(expr)` | 式を評価した値をそのまま出力 |
| `MAYAA_SCOPE_AS_STRING(expr)` | 文字列として出力（クオート付き） |
| `MAYAA_SCOPE_WITH_STRINGIFY(expr)` | JSON.stringify した値を出力（配列・オブジェクト向け） |
| `MAYAA_SCOPE_RAW(expr)` | 非エスケープで出力（`${=...}` 相当） |

#### 使用例

```javascript
/* global MAYAA_SCOPE, MAYAA_SCOPE_AS_STRING, MAYAA_SCOPE_WITH_STRINGIFY,
   MAYAA_SCOPE_RAW: readonly */

var data = MAYAA_SCOPE_WITH_STRINGIFY(users);
var count = MAYAA_SCOPE(userList.length);
var name = MAYAA_SCOPE_AS_STRING(user.getName());
```

これらのマクロはサーバーサイドレンダリング時に評価結果に置き換えられます。静的表示時は通常の JavaScript 関数呼び出しとして扱われます。

> **破壊的変更はありません。** 既存の `${}` 記法は引き続き動作します。

---

### 診断・プロファイリング機能の追加（新機能）

**影響範囲:** 運用監視・パフォーマンス調査を行う場合

PR #130 により、Specification のビルド・レンダリング処理を計測・可視化するプロファイリング基盤が追加されました。

#### `MayaaProfileServlet`（管理エンドポイント）

デフォルトで `/mayaa-admin/profile` エンドポイントが追加されます。

- JSON 形式でビルド・レンダリング時間、警告・エラーイベントを返します
- デフォルトではループバックアドレス（127.0.0.1、::1）からのアクセスのみ許可されます
- `allowedCidr` init-param で許可 IP 帯域を変更できます

```xml
<!-- web.xml への登録例（自動登録済み） -->
<servlet>
    <servlet-name>MayaaProfileServlet</servlet-name>
    <servlet-class>org.seasar.mayaa.impl.management.MayaaProfileServlet</servlet-class>
    <init-param>
        <param-name>allowedCidr</param-name>
        <param-value>127.0.0.1/8,::1/128,192.168.0.0/16</param-value>
    </init-param>
</servlet>
```

#### `mayaa-profile` CLI ツール

`tools/mayaa-profile` コマンドで実行中の Mayaa サーバーに接続し、ビルド・レンダリング状況をリアルタイムで確認できます。

```
mayaa-profile [--url http://localhost:8080/mayaa-admin/profile]
```

- デフォルト接続先: `http://localhost:8080/mayaa-admin/profile`
- キー操作: `1`–`9` でカラムソート、`/` でテキストフィルタ、`E` でエラーフィルタ

#### メモリへの影響

`SpecificationProfileRegistry` に計測データを保持します。

- 最大 2,000 件を LRU で管理
- 1件あたり約 1.1 KB（エラーなし）〜最大 9 KB（エラー 20 件上限時）
- 最悪ケースで約 18 MB のヒープ増加

#### ログ集約

ビルド・レンダリング中の診断ログがロガー名 `org.seasar.mayaa.impl.management.SpecificationProfileRegistry` に集約されました。
不要な場合はこのロガーのレベル設定で出力を制御してください。

## 1.2.x から 1.3.0 へのアップグレード

### システム要件の変更

#### Java バージョン
**重要: Java 8 以上が必須となりました**

- **旧:** Java 7 以上
- **新:** Java 8 以上

Java 8 未満の環境では動作しません。アップグレード前に実行環境のJavaバージョンを確認してください。

#### Servlet API
Servlet API 2.4 ~ 4.0 の web.xml に対応しています。

### HTMLパーサの変更

#### 1. XML宣言後の改行
XML宣言の直後に余分な改行が含まれなくなりました。

**影響範囲:** テンプレートの厳密な出力形式に依存している場合

**対応:** 出力結果を確認し、必要に応じてテンプレートを調整してください。

#### 2. 空文字指定(Empty attribute)の出力形式
HTML仕様に合わせて、空属性の出力形式が変更されました。

**変更前:**
```html
<input type="checkbox" disabled="">
```

**変更後:**
```html
<input type="checkbox" disabled>
```

**影響範囲:** 出力HTMLの形式に依存するJavaScriptやCSSセレクタがある場合

**対応:** 必要に応じてJavaScript/CSSを確認・修正してください。

#### 3. Void Element への対応強化
`source`、`embed` などの Void Element（閉じタグを伴わないタグ）に対応しました。

#### 4. 文字コード検出の改善
- xml宣言やmetaタグでcharset変更を検知した時に、指定された文字コードで再読み込みするようになりました
- `<meta charset="UTF-8">` 形式の記述にも対応しました

#### 5. 新しいHTMLパーサ（Experimental）
HTML Living Standardの定義に近い新しいHTMLパーサが実装されました（デフォルトは従来のNekoHTML版）。

**有効化方法:**
`WEB-INF/org.seasar.mayaa.provider.ServiceProvider` ファイルで以下のように設定:

```xml
<templateBuilder useNewParser="true" />
```

**新パーサの機能:**
- Unquoted attribute value のパース対応
- `@attr` や `:attr` といった属性名の利用が可能（Vue等のフロントエンドフレームワーク対応）
- HTML Living Standard準拠の動作

**注意:** 新パーサは実験的機能です。十分なテストを行ってから本番環境で使用してください。

### システム環境・運用関連

#### 1. JMX による Specification 無効化
JMX経由でSpecificationを無効化する操作が追加されました。

詳細は [doc/MANAGEMENT.md](./MANAGEMENT.md) を参照してください。

#### 2. プロセッサダンプの拡張
プロセッサダンプでTemplateのプロセッサ、オリジナルノード、Pageの内容が追加表示されるようになりました。

### 設定関連の変更

#### 1. WebInfSourceScanner の新設
WEB-INF配下を明示指定でスキャンできる `WebInfSourceScanner` が新設されました。

**影響:** ビルトインの設定は `WebInfSourceScanner` を使用するように変更されました。

#### 2. WebXMLTaglibSourceScanner の除外
ビルトインのライブラリマネージャの読み込みで `WebXMLTaglibSourceScanner` を使用しないようになりました。

**理由:** web.xmlのServletバージョンをあげた時に追従しきれないため

**対応:** 明確にweb.xmlのタグライブラリ定義を使用したい場合は、`WEB-INF/org.seasar.mayaa.provider.ServiceProvider` で `WebXMLTaglibSourceScanner` を明示的に含めてください。

#### 3. ResourceScanner の除外指定
全てのJarをスキャンしないように除外指定できるようになりました。

#### 4. ファクトリクラスの読み込み順序変更オプション
各ファクトリクラス、ServiceProviderの生成時の順序を逆順に変更できるようになりました。

**有効化方法:**
web.xml にて MayaaServlet に対する初期化パラメータ(`init-param`)で `enableBackwardOrderLoading` を `true` に設定:

```xml
<servlet>
    <servlet-name>mayaa</servlet-name>
    <servlet-class>org.seasar.mayaa.impl.MayaaServlet</servlet-class>
    <init-param>
        <param-name>enableBackwardOrderLoading</param-name>
        <param-value>true</param-value>
    </init-param>
</servlet>
```

**注意:** `class`属性で指定されたクラスにインタフェースクラスを1つ引数にとるコンストラクタが定義されている場合は、互換性のため元の順序で生成されます。

#### 5. ServiceProvider定義の class 属性が任意に
ServiceProvider定義XMLの`class`属性を省略できるようになりました。

**変更前:** class属性は必須
**変更後:** 省略された場合はビルトインの設定ファイルと同じ実装をデフォルトとして使用

実装を変更したい場合のみ、従来通り`class`属性で指定します。

**デフォルト実装クラス一覧:**

| エレメント名 | インタフェース | デフォルト実装クラス |
|------------|---------------|---------------------|
| provider   | ServiceProvider | ServiceProviderImpl |
| engine     | Engine | EngineImpl |
| parentSpecificationResolver | ParentSpecificationResolver | ParentSpecificationResolverImpl |
| scriptEnvironment | ScriptEnvironment | ScriptEnvironmentImpl |
| templateBuilder | TemplateBuilder | TemplateBuilderImpl |
| templateAttributeReader | TemplateAttributeReader | TemplateAttributeReaderImpl |
| specificationBuilder | SpecificationBuilder | SpecificationBuilderImpl |
| pathAdjuster | PathAdjuster | PathAdjusterImpl |
| libraryManager | LibraryManager | LibraryManagerImpl |

### 依存ライブラリのアップグレード

以下のライブラリが更新されました:

- **xercesImpl**: 2.12.0 → 2.12.2（XMLパーサのバグ修正とセキュリティ改善）

### バグ修正

#### 1. balanceTag 無効時の DOCTYPE 問題
balanceTag を無効にした時にDOCTYPEがheadタグ内に余分に付加される問題が修正されました。

#### 2. MetaInfSourceScanner の ignore パラメータ
ignore パラメータが効いていない問題が修正されました。

#### 3. 独自プロセッサー使用時のエラー
独自プロセッサーを使用した際にエラーが発生する場合があった問題が修正されました。

#### 4. TemplateProcessor.notifyBeginRender
実質的に呼ばれていなかった問題が修正されました。

### アップグレード手順

1. **Java バージョンの確認と更新**
   - 実行環境がJava 8以上であることを確認
   - Java 7以下の場合は、Java 8以上にアップグレード

2. **Maven/Gradle の依存関係更新**

   **Maven の場合:**
   ```xml
   <dependency>
       <groupId>com.github.seasarorg.mayaa</groupId>
       <artifactId>mayaa</artifactId>
       <version>1.3.0</version>
   </dependency>
   ```

   **Gradle の場合:**
   ```groovy
   implementation 'com.github.seasarorg.mayaa:mayaa:1.3.0'
   ```

3. **設定ファイルの確認**
   - カスタムの ServiceProvider 設定がある場合、新しい仕様に合わせて確認
   - 必要に応じて `class` 属性を削除して簡素化を検討

4. **テンプレートの検証**
   - HTMLパーサの変更により出力が変わる可能性があるため、テスト環境で十分に検証
   - 特に以下を重点的に確認:
     - XML宣言を使用しているページ
     - 空属性を使用しているHTML要素
     - Void Elementの使用箇所

5. **アプリケーションのテスト**
   - 開発環境で全機能をテスト
   - パフォーマンステストの実施
   - 本番環境への展開前にステージング環境で最終確認

6. **本番環境への展開**
   - バックアップの取得
   - メンテナンス時間帯での展開を推奨
   - ロールバック手順の準備

### トラブルシューティング

#### HTMLの出力形式が変わった

**症状:**
- XML宣言後に余分な改行が入っていた出力が、改行なしになった
- `<input disabled="">` が `<input disabled>` に変わった

**原因:**
- HTMLパーサの改善により、より仕様準拠の出力になりました

**対応:**
1. 出力形式の変化を確認
2. JavaScriptやCSSで属性セレクタ `[disabled=""]` を使用している場合は `[disabled]` に変更
3. 厳密な文字列比較をしている場合は調整

**例:**
```javascript
// 変更前
if (input.getAttribute('disabled') === '') { ... }

// 変更後
if (input.hasAttribute('disabled')) { ... }
```

2. スナップショットテストの期待値を更新する
3. 必要であればその箇所のみ `${=...}` に切り替え、非エスケープ出力を明示する

#### autoEscape 有効化後に二重エスケープが発生する

**症状:**
- `&amp;lt;` のような出力になる

**原因:**
- 既存の手動エスケープと自動エスケープが重複した

**対応:**
1. 手動エスケープ済み値は `${=...}` に統一して出力する
2. 問題ページだけ `<!-- m:autoEscape="false" -->` で一時的に無効化する

#### 既にエスケープ済みの値が誤検出/検出漏れされる

**症状:**
- エスケープ済みのはずなのに再エスケープされる、または未エスケープ値がスキップされる

**原因:**
- 自動検出は「一部のパターン一致」で判定するため、独自エスケープ規則と相性が悪い場合がある

**対応:**
1. 独自エスケープ util の戻り値を `${=...}` で明示的に出力する
2. 判定に依存しないよう、エスケープ実装の統一ルールを決める

#### `${=...}` 利用箇所で想定外の生HTMLが出力される

**症状:**
- 画面にタグがそのまま描画される、またはXSS監査で指摘される

**原因:**
- `${=...}` は常に非エスケープ出力であり、`autoEscapeEnabled` の設定に関係なくバイパスされる

**対応:**
1. 信頼できるデータのみ `${=...}` を利用する
2. ユーザー入力は `${...}` に戻し、自動エスケープを適用する
3. `${=...}` の利用箇所を監査対象として一覧管理する

#### web.xml のタグライブラリ定義が認識されない

**症状:**
- 以前は認識されていたweb.xmlのタグライブラリ定義が読み込まれない

**原因:**
- `WebXMLTaglibSourceScanner` がビルトイン設定から除外されました

**対応:**
`WEB-INF/org.seasar.mayaa.provider.ServiceProvider` に以下を追加:

```xml
<libraryManager>
    <scanner class="org.seasar.mayaa.impl.builder.library.scanner.WebXMLTaglibSourceScanner"/>
    <!-- その他のスキャナー設定... -->
</libraryManager>
```

#### Jakarta系TLD読み込み時にXSD解決WARNが出る

**症状:**
- `Entity not resolved locally ... https://jakarta.ee/xml/ns/jakartaee/...` が起動ログに出る

**原因:**
- TLDのXSD検証が有効な場合、外部スキーマ解決が発生するため

**対応:**
`WEB-INF/org.seasar.mayaa.provider.ServiceProvider` の `TLDDefinitionBuilder` に以下を設定:

```xml
<libraryManager>
    <builder class="org.seasar.mayaa.impl.builder.library.TLDDefinitionBuilder">
        <parameter name="tldValidation" value="false"/>
        <parameter name="tldXmlSchema" value="false"/>
    </builder>
</libraryManager>
```

#### 起動時に設定ファイルのエラーが発生する

**症状:**
- アプリケーション起動時に ServiceProvider や ファクトリ設定のエラーが発生

**原因:**
- 設定ファイルの書式チェックが厳密になりました
- 以前は警告のみだったエラーが起動を停止するようになりました

**対応:**
1. エラーログを確認し、問題の設定ファイルを特定
2. XML構文エラーや未定義の属性を修正
3. 必要に応じて `class` 属性を省略して簡素化

**例:**
```xml
<!-- エラー: 不正な属性 -->
<templateBuilder unknownAttribute="value" />

<!-- 修正: 不要な属性を削除、または class 属性を省略 -->
<templateBuilder />
```

#### 新パーサ使用時の問題

**症状:**
- `useNewParser="true"` を設定したら既存テンプレートでエラーが発生

**原因:**
- 新パーサはHTML Living Standard準拠のため、従来の動作と異なる場合があります

**対応:**
1. **段階的な導入**: まず開発環境でテスト
2. **問題の特定**: エラーログから問題のテンプレートを特定
3. **テンプレート修正**: 必要に応じてマークアップを修正
4. **フォールバック**: 問題が多い場合は `useNewParser="false"` に戻す

**よくある問題:**
- Unquoted attribute が誤って解釈される
- タグの省略規則が異なる
- 名前空間の扱いが異なる

#### Java 7 環境での起動失敗

**症状:**
- `java.lang.UnsupportedClassVersionError` が発生

**原因:**
- Mayaa 1.3.0 は Java 8 以上が必須です

**対応:**
1. Java バージョンを確認: `java -version`
2. Java 8 以上にアップグレード
3. JAVA_HOME 環境変数を確認
4. アプリケーションサーバーの Java バージョンを確認

#### 独自プロセッサーが動作しない

**症状:**
- カスタムプロセッサーを使用するとエラーが発生

**原因:**
- プロセッサーの呼び出しロジックが修正されました

**対応:**
1. プロセッサーの実装が `TemplateProcessor` インターフェースに準拠しているか確認
2. `notifyBeginRender` メソッドが正しく実装されているか確認
3. エラーログから具体的な問題を特定
4. 必要に応じてプロセッサーの実装を更新

---

## 1.1.x から 1.2 へのアップグレード

### システム要件の変更

#### Java バージョン
**Java 7 以上が必須となりました**

- **旧:** Java 6 以上（推測）
- **新:** Java 7 以上

#### Servlet API
Servlet 3.1 および 4.0 の web.xml に対応しました。

### 新機能

#### 1. Mayaaバージョンの参照
スクリプトから Mayaa のバージョンを参照できるようになりました。

```
${org.seasar.mayaa.impl.Version.MAYAA_VERSION}
```

#### 2. JMX によるモニタリング
JMX経由でMayaaの内部状態をモニタリングできるようになりました。

詳細は [Management Wiki](https://github.com/seasarorg/mayaa/wiki/Management) を参照してください。

#### 3. スクリプトキャッシュの設定
スクリプトのキャッシュの強制保持個数を指定できるようになりました。

`WEB-INF/org.seasar.mayaa.provider.ServiceProvider` にて設定:
```xml
<scriptEnvironment cacheSize="128" />
```

### 依存ライブラリのアップグレード
以下のライブラリが更新されました:
- commons-beanutils: 1.8.3 → 1.9.4
- commons-collections: 3.1 → 3.2.2
- commons-logging: 1.0.4 → 1.2
- xerces:xercesImpl: 2.7.1 → 2.12.0

### バグ修正

#### スクリプトキャッシュの競合問題
複数スレッド下でスクリプトキャッシュの競合が発生する問題が解消されました。

#### URLエンコード問題
URLエンコードされる文字を含むsystemIDのファイル実体が参照できない問題が修正されました。

### アップグレード手順

1. **Java バージョンの確認**
   - Java 7 以上であることを確認

2. **依存関係の更新**
   ```xml
   <dependency>
       <groupId>com.github.seasarorg.mayaa</groupId>
       <artifactId>mayaa</artifactId>
       <version>1.2</version>
   </dependency>
   ```

3. **スクリプトキャッシュの設定確認**
   - 必要に応じて `cacheSize` パラメータを設定

4. **テストと展開**
   - 開発環境でテスト
   - 本番環境へ展開

---

## 1.1.32 以前から 1.1.33 以降へのアップグレード

### Maven リポジトリの変更

**重要: Maven Central Repository への移行**

#### groupId と artifactId の変更

**1.1.33以降:**
- **Repository:** Maven Central Repository
- **groupId:** `com.github.seasarorg.mayaa`
- **artifactId:** `mayaa`

**1.1.32以前:**
- **Repository:** http://maven.seasar.org/maven2/
- **groupId:** `org.seasar.mayaa`
- **artifactId:** `mayaa`

### 新機能
- Servlet API 3.1 に対応

### バグ修正
- ファイルが存在しない場合のタイムスタンプチェックの問題を修正
- Mayaa の forward 時の page スコープのキャッシュ問題を修正
- HttpSession の無効判定を修正
- ページオブジェクトのキャッシュの同一判定を修正

### アップグレード手順

#### Maven の場合

**pom.xml の変更:**

```xml
<!-- 変更前 (1.1.32以前) -->
<repositories>
    <repository>
        <id>seasar</id>
        <url>http://maven.seasar.org/maven2/</url>
    </repository>
</repositories>

<dependency>
    <groupId>org.seasar.mayaa</groupId>
    <artifactId>mayaa</artifactId>
    <version>1.1.32</version>
</dependency>
```

```xml
<!-- 変更後 (1.1.33以降) -->
<!-- repositories の定義は不要（Maven Centralを使用） -->

<dependency>
    <groupId>com.github.seasarorg.mayaa</groupId>
    <artifactId>mayaa</artifactId>
    <version>1.1.33</version> <!-- または最新バージョン -->
</dependency>
```

#### Gradle の場合

```groovy
// 変更前 (1.1.32以前)
repositories {
    maven { url 'http://maven.seasar.org/maven2/' }
}

dependencies {
    implementation 'org.seasar.mayaa:mayaa:1.1.32'
}
```

```groovy
// 変更後 (1.1.33以降)
repositories {
    mavenCentral()
}

dependencies {
    implementation 'com.github.seasarorg.mayaa:mayaa:1.1.33' // または最新バージョン
}
```

---

## 参考情報

### ドキュメント
- [CHANGELOG](../CHANGELOG.md) - 全バージョンの変更履歴
- [互換性](./COMPATIBILITY.md) - 互換性情報
- [運用・管理](./MANAGEMENT.md) - 運用管理情報
- [パーサ仕様](./PARSER.md) - パーサの詳細仕様
- [テスト](./TESTING.md) - テスト方法

### サポート
- [GitHub Issues](https://github.com/seasarorg/mayaa/issues) - 問題報告・機能提案
- [Mayaa-User ML](https://www.seasar.org/mailman/listinfo/mayaa-user) - 利用に関する質問

### リリース情報
- [Maven Central Repository](https://search.maven.org/artifact/com.github.seasarorg.mayaa/mayaa)
- [GitHub Releases](https://github.com/seasarorg/mayaa/releases)
