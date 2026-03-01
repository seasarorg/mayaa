# Mayaa アップグレードガイド

このドキュメントでは、Mayaaの旧バージョンから新しいバージョンへアップグレードする際の注意事項と手順を説明します。

## 目次
- [1.2.x から 1.3.0 へのアップグレード](#12x-から-130-へのアップグレード)
- [1.1.x から 1.2 へのアップグレード](#11x-から-12-へのアップグレード)
- [1.1.32 以前から 1.1.33 以降へのアップグレード](#1132-以前から-1133-以降へのアップグレード)

---

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
