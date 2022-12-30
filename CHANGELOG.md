# 変更点

## Mayaa 1.2.1 : Not yet released

### Changes
- [#80](https://github.com/seasarorg/mayaa/pull/80) - Mayaa動作要件の最低JavaバージョンをJava8としました。
- [#75](https://github.com/seasarorg/mayaa/issue/75) - xml宣言やmetaタグでcharset変更を検知した時に文字コードを指定して再読み込みするように変更しました。

### Fixes
- [#75](https://github.com/seasarorg/mayaa/issues/75) - balanceTag を無効にするとDOCTYPEがheadタグ内に余分に付加される問題を修正しました。


### Experimental
- [#77](https://github.com/seasarorg/mayaa/pull/77) -  NekoHTMLを使用せずHTML Living Standardの定義に近いHTMLパーサを実装しました（デフォルトはNekoHTML版）。

### Internal
- テストフレームワークをJUnit5に変更しました。

## Mayaa 1.2 : 2020-11-15

### Changes
- [#16](https://github.com/seasarorg/mayaa/issues/16) - Mayaa動作要件の最低JavaバージョンをJava7としました。
- [#35](https://github.com/seasarorg/mayaa/pull/35) - Mayaaのバージョンを`${org.seasar.mayaa.impl.Version.MAYAA_VERSION}`で参照できるようにしました。
- [#32](https://github.com/seasarorg/mayaa/issues/32) - JMX経由でMayaaの内部状態をモニタリングできるようにしました。[詳細](https://github.com/seasarorg/mayaa/wiki/Management)
- [#50](https://github.com/seasarorg/mayaa/issues/50) - Serlvet 3.1 および 4.0 の web.xml に対応しました。
- [#15](https://github.com/seasarorg/mayaa/issues/15) - スクリプトのキャッシュの強制保持個数を指定できるようにしました。
- 依存ライブラリをアップグレードしました。
   * commons-beanutils:commons-beanutils:1.8.3 -> 1.9.4
   * commons-collections:commons-collections:3.1 -> 3.2.2
   * commons-logging:commons-logging:1.0.4 -> 1.2
   * xerces:xercesImpl:2.7.1 -> 2.12.0

### Fixes
- [#14](https://github.com/seasarorg/mayaa/issues/14) - 複数スレッド下でスクリプトキャッシュの競合を解消するとともにキャッシュ保持数の制御を改善しました。
  org.seasar.mayaa.provider.ServiceProvider 内の scriptEnvironment のパラメータ名 cacheSize にて最小の保持数を設定します。（デフォルト値128）
- [#49](https://github.com/seasarorg/mayaa/pull/49) - URLエンコードされる文字を含むsystemIDのファイル実体が参照できない潜在的不具合に対応しました。

## Mayaa 1.1.34 : 2017-07-30
### Fixes
- [#7](https://github.com/seasarorg/mayaa/issues/7) - mayaaファイルに ${} で書いた変数名がそのまま展開される場合がある不具合を修正しました。
- [#5](https://github.com/seasarorg/mayaa/issues/5) - テンプレートに書かれたインデントが詰められる不具合を修正しました。

## Mayaa 1.1.33 : 2017-03-25
### Changes
- MavenについてはMaven Central Repositoryへの公開に変更しました。
  https://search.maven.org/artifact/com.github.seasarorg.mayaa/mayaa
  - groupId: com.github.seasarorg.mayaa
  - artifactId: mayaa
- [#2](https://github.com/seasarorg/mayaa/issues/2) - Servlet API 3.1に対応しました。

### Fixes
- ファイルが存在しない場合、タイムスタンプチェックが無効なときにチェックし続ける問題を修正しました。
- Mayaaのforwardをしたさいにpageスコープのキャッシュが以前のままになっている問題を修正しました。
- HttpSessionの無効判定を修正しました。
- ページオブジェクトのキャッシュの同一判定を修正しました。

## Mayaa 1.1.32 : 2013-08-12
### Fixes
- (rev.3538) 高負荷時にスクリプトのキャッシュ取得で無限ループになる場合がある問題を修正しました。

## Mayaa 1.1.31 : 2013-04-07
### Fixes
- (rev.3537) 高負荷時にビルド結果取得に失敗する場合がある問題を修正しました。
- (rev.3538) Webコンテナを使わない場合などのビルド時にNullPointerExceptionが発生する場合がある問題を修正しました。
- (rev.3539) 1.1.30でFileGeneratorを使う処理が失敗するようになった問題を修正しました。

## Mayaa 1.1.30 : 2013-01-19
### Fixes
- (rev.3509) [[JIRA:MAYAA-80](https://www.seasar.org/issues/browse/MAYAA-80)] -  Tomcat7上でload関数を使用した際に毎回コンパイルになってPermGenを使用していた問題を修正しました。


## Mayaa 1.1.29 : 2012-04-07

### Changes
- (rev.3509) [[JIRA:MAYAA-78](https://www.seasar.org/issues/browse/MAYAA-78)] -  commons-beanutilsを1.7.0から1.8.3に更新しました。
- (rev.3513) TemplateBuilderImplにbalanceTagパラメータを追加しました。
 falseにするとタグのバランス修正をしなくなります。
 その場合、テンプレート作成者側で正しいことを保証する必要があります。
- (rev.3524) [[JIRA:MAYAA-79](https://www.seasar.org/issues/browse/MAYAA-79)] -  Servlet 3.0のweb.xmlに対応しました。
  ([mayaa-user:933] でパッチをいただきました)


## Mayaa 1.1.28 : 2010-12-12
### Changes
- (rev.3494) [[JIRA:MAYAA-76](https://www.seasar.org/issues/browse/MAYAA-76)] -  テンプレートに対する.mayaaファイル、および親mayaaファイルを探す処理を拡張可能にしました。
 org.seasar.mayaa.provider.ServiceProviderファイルのparentSpecificationResolverを
 差し替えることで独自の処理に入れ換えられます。
- (rev.3496) XML/XHTMLの出力時のxml宣言で?の前の空白をtrimして出力するようにしました。

### Fixes
- (rev.3491) [[JIRA:MAYAA-75](https://www.seasar.org/issues/browse/MAYAA-75)] -  動作中にdefault.mayaaのbeforeRenderを書き換えても反映されない問題を修正しました。
- (rev.3500) [[JIRA:MAYAA-77](https://www.seasar.org/issues/browse/MAYAA-77)] -  ボディを処理する必要のあるタグの場合に子のプロセッサでflushすると親よりも先に子が出力されてしまう問題を修正しました。

## Mayaa 1.1.27 : 2009-10-30
### Changes
- (rev.3458) `MayaaApplicationFilter`で例外ページの処理をできるようにしました。
 Strutsなどのフレームワークで発生した例外を対象とし、Mayaaのエラーページ処理に
 回せるようにします。web.xmlで`MayaaApplicationFilter`を設定し、パラメータ `handleException` に`true`を設定すると有効になります。
- (rev.3459) MayaaServletにdebugオプションを追加し、debugがtrueの場合のみ、
  デフォルトのエラーページ java.lang.Throwable.html でデバッグ情報を出力するようにしました。
  また、MayaaServletのパラメータにセットしない場合、システムプロパティ`org.seasar.mayaa.debug`で同様の設定ができます。
- (rev.3460) MayaaServletのdebugオプションが有効なときをデバッグモードとし、スクリプトで`${isDebugMode()}`とすることで参照できるようにしました。
- (rev.3469) テンプレートのデフォルト名前空間をmime-typeから判断するようにしました。
 ("xhtml"を含むならXHTML、それ以外ならHTML。明示すればそちらを利用)
- (rev.3474) [[JIRA:MAYAA-73](https://www.seasar.org/issues/browse/MAYAA-73)] -  `m:noCache="true"`のときにレスポンスのCache-Control
 ヘッダの値を`ServiceProvider`ファイルで設定できるようにしました。互換性のため、デフォルト値はこれまでどおり"no-cache"ですが、
 Firefoxブラウザで戻るボタンのときにキャッシュを使わせないようにするには"no-cache, no-store"にする必要があります。
 キャッシュしないという動作になるため、挙動がすこし変わる場合があります。
- (rev.3476) `MayaaApplicationFilter`で例外をキャッチしてエラーページへ遷移する際に
 レンダリング中と同様に`ClientAbortException`であれば無視するようにしました。

## Bug Fixes
- (rev.3471) [[JIRA:MAYAA-72](https://www.seasar.org/issues/browse/MAYAA-72)] -  テンプレートファイル置き換え時に解放されない
  オブジェクトが発生する問題を修正しました。


## Mayaa 1.1.26 : 2009-06-09
### Fixes
- (rev.3454) [[JIRA:MAYAA-70](https://www.seasar.org/issues/browse/MAYAA-70)] -  メインページとレイアウトページの階層が異なる場合に
 insertプロセッサの動的相対パス解決で失敗する問題を修正しました。(1.1.25で発生)

## Mayaa 1.1.25 : 2009-06-07
### Changes
- (rev.3423) 同梱するRhinoのバージョンを1.7-r2にアップデートしました。
- (rev.3432) [[JIRA:MAYAA-64](https://www.seasar.org/issues/browse/MAYAA-64)] -  SSI Includeの記述を`m:insert`に置き換える処理を実装しました。
- (rev.3437) SSI Includeを`m:insert`に置き換える処理が有効なとき、拡張子".inc"
  のファイルは自動的に全体を`m:doRender(name="")`で囲むようにしました。
  ただしmime-mappingで".inc"をHTMLかXHTMLとして設定する必要があります。
- (rev.3445) [[JIRA:MAYAA-68](https://www.seasar.org/issues/browse/MAYAA-68)] -  attributeプロセッサに、&をエスケープするか
  どうかを決める`escapeAmp`属性を追加しました。デフォルトは`true`です。

### Fixes
- (rev.3431) [[JIRA:MAYAA-58](https://www.seasar.org/issues/browse/MAYAA-58)] -  AutoBuildでコンテキストパスが"/"以外の場合に
  相対パスの絶対パス化処理が正しく動作しない問題を修正しました。
- (rev.3433) [[JIRA:MAYAA-62](https://www.seasar.org/issues/browse/MAYAA-62)] -  deserializeした際、.mayaaの無いページの場合に
  テンプレートを再ビルドしてしまう問題を修正しました。
- (rev.3440) [[JIRA:MAYAA-67](https://www.seasar.org/issues/browse/MAYAA-67)] -  スクリプトでoriginalNodeのtoString()を実行したときに
  無限ループが発生する問題を修正しました。
- (rev.3447) [[JIRA:MAYAA-69](https://www.seasar.org/issues/browse/MAYAA-69)] -  カスタムタグからコンポーネントを使う側の方に
  親を探しに行くよう修正しました。

## Mayaa 1.1.24 : 2009-03-23
### Changes
- (rev.3392) [[JIRA:MAYAA-60](https://www.seasar.org/issues/browse/MAYAA-60)] -  Servlet 2.5, JSP 2.1 の xsd を利用するファイルを
  パースできるようにしました。JSP 2.1 の tld をパースできなかった問題が解決します。


## Mayaa 1.1.23 : 2008-12-25
### Changes
- (rev.3387) [[JIRA:MAYAA-59](https://www.seasar.org/issues/browse/MAYAA-59)] -  デフォルトのエラーページ (java.lang.Throwable.html)
  のエラーメッセージ部分でタグをエスケープするよう修正しました。
- (rev.3387) 標準のエラーページを変更し、通常はアプリケーションサーバの
  エラーページになるようにしました。application スコープの debug 属性を
  null 以外に設定すると、これまでどおりのエラーページを表示します。
  (application スコープは ServletContext の属性と同じ)


## Mayaa 1.1.22 : 2008-11-22
### Changes
- (rev.3383) [[JIRA:MAYAA-56](https://www.seasar.org/issues/browse/MAYAA-56)] -  ServiceProviderのtemplateBuilder設定に、パラメータとして
  テンプレートをパースするときのデフォルト文字セットを指定できるようにしました。
  デフォルト値はこれまで通りUTF-8です。


## Mayaa 1.1.21 : 2008-11-08
### Fixes
- (rev.3380) [[JIRA:MAYAA-55](https://www.seasar.org/issues/browse/MAYAA-55)] -  スクリプトにおいて、requestスコープやsessionスコープの
  属性に対して「foo = 1」のようにスコープを明示せずに値をセットしようとした場合に
  例外が発生する問題を修正しました。

## Mayaa 1.1.20 : 2008-10-29
### Fixes
- (rev.3377) [[JIRA:MAYAA-54](https://www.seasar.org/issues/browse/MAYAA-54)] -  .mayaaファイルのルート直下にHTMLを直接記述した場合
  ビルド結果キャッシュが正常に作成されない問題を修正しました。

## Mayaa 1.1.19 : 2008-09-28
### Changes
- (rev.3361) [[JIRA:MAYAA-50](https://www.seasar.org/issues/browse/MAYAA-50)] -  SimpleTagのgetJspBody()が動作するよう実装しました。
- (rev.3363) [[JIRA:MAYAA-51](https://www.seasar.org/issues/browse/MAYAA-51)] -  tldのbody-contentでemptyが指定されている場合、
  SKIP_BODYと同じ処理になるようにしました。(これまではdoStartTagの戻り値次第)

### Fixes
- (rev.3370) [[JIRA:MAYAA-52](https://www.seasar.org/issues/browse/MAYAA-52)] -  replace="false"としたプロセッサにm:attributeが書けていた問題を修正しました。

## Mayaa 1.1.18 : 2008-06-18
### Fixes
- (rev.3342) [[JIRA:MAYAA-48](https://www.seasar.org/issues/browse/MAYAA-48)] -  SimpleTagの場合に親タグの情報を取得できない問題を修正しました。

## Mayaa 1.1.17 : 2008-03-02
### Fixes
- (rev.3269) [[JIRA:MAYAA-46](https://www.seasar.org/issues/browse/MAYAA-46)] -  TagExtraInfoのgetVariableInfoがnullを返した
  ときにNullPointerExceptionが発生する問題を修正しました。

## Mayaa 1.1.16 : 2007-12-01
### Fixes
- (rev.3244) [[JIRA:MAYAA-42](https://www.seasar.org/issues/browse/MAYAA-42)] -  テンプレートがXMLの場合でもHTMLの閉じタグ調整が
  有効になってしまう問題を修正しました。

## Mayaa 1.1.15 : 2007-09-30
### Fixes
- (rev.3232) [[JIRA:MAYAA-40](https://www.seasar.org/issues/browse/MAYAA-40)] -  テンプレートにコメントがあるとき、xpath="//*" で
  バインディングしようとするとNullPointerExceptionが発生する問題を修正しました。
- (rev.3233) [[JIRA:MAYAA-41](https://www.seasar.org/issues/browse/MAYAA-41)] -  SimpleTagにDynamicAttributeを使えない問題を修正しました。


## Mayaa 1.1.14 : 2007-09-06
### Changes
- (rev.3191) [[JIRA:MAYAA-38](https://www.seasar.org/issues/browse/MAYAA-38)] -  DynaBeanのプロパティもJavaScriptから通常のJavaBeans
  と同じようにアクセスできるよう対応しました。
### Fixes
- (rev.3204) [[JIRA:MAYAA-39](https://www.seasar.org/issues/browse/MAYAA-39)] -  Pageスコープにあるjava.lang.Numberのオブジェクトに
  スクリプトで加算をおこなうと文字列加算になってしまう問題を修正しました。

## Mayaa 1.1.13 : 2007-08-19
### Changes
- (rev.3161) [[JIRA:MAYAA-32](https://www.seasar.org/issues/browse/MAYAA-32)] -  m:mayaaタグにcacheControl属性を追加しました。
  cacheControl属性には静的な文字列のみ記述でき、記述した文字列がそのまま
  Cache-Controlレスポンスヘッダの値としてセットされます。
  noCache="true"とcacheControl属性とを両方セットした場合、noCacheの場合の
  レスポンスヘッダのうちCache-Controlヘッダの値のみcacheControl属性の値が優先されます。
- (rev.3185) [[JIRA:MAYAA-36](https://www.seasar.org/issues/browse/MAYAA-36)] -  XHTMLのemptyタグの閉じ記述で、"/>"の前に空白を含めるように変更しました。
  [HTML互換性ガイドライン](http://www.w3.org/TR/2000/REC-xhtml1-20000126/#guidelines)
- (rev.3188) [[JIRA:MAYAA-37](https://www.seasar.org/issues/browse/MAYAA-37)] -  パスの自動調整で、"./"で始まっているパス以外も
  対象とするオプションを追加しました。エンジン設定でPathAdjusterImplの"force"
  パラメータをtrueにすることで有効になります。

### Fixes
- (rev.3163) [[JIRA:MAYAA-33](https://www.seasar.org/issues/browse/MAYAA-33)] -  MayaaServlet 以外からの呼び出しで request 情報が
  誤っている場合、ページ名が内部で扱うものと一致しないことがある問題に対応しました。

## Mayaa 1.1.12 : 2007-08-05
### Changes
- (rev.3155) テンプレートのmetaタグでcontent-typeを指定し、かつcharsetを
  指定しない場合、レスポンスヘッダにはcharset=UTF-8付きで返すよう変更しました。
- (rev.3158) [[JIRA:MAYAA-31](https://www.seasar.org/issues/browse/MAYAA-31)] - エンジン設定のengineのパラメータに"convertCharset"を
  追加しました。デフォルトはfalseです。この値をtrueにすることで、テンプレートの
  metaタグでcharset=Windows-31とした場合でもcharset=Shift_JISにして出力します。
  また同様にHTTPレスポンスヘッダもcharset=Shift_JISになります。

## Mayaa 1.1.11 : 2007-07-15
### Changes
- (rev.3108) テンプレートでcharsetを指定していない場合、レスポンスにはcharsetをUTF-8とするよう変更しました。
- (rev.3115) /WEB-INF/を含むパスへのforwardを許すように修正しました。

## Mayaa 1.1.10 : 2007-05-23
### Changes
- (rev.3017) デフォルトでレイアウトを適用できる機能を追加しました。
- (rev.3027) execプロセッサのsrc属性を使って読み込んだスクリプトを実行する際の
  スコープがタグのスコープと同一になるよう変更しました。
  これによりexecで宣言した変数は、execのボディ限定ではなexecと同じ階層にあるプロセッサや
  スクリプトで参照できるようになります。
  なお、script属性でスクリプトを実行する場合は、1.1.0-beta6よりこの状態になっており、
  ドキュメントが誤っていました。
- (rev.3036) [[JIRA:MAYAA-27](https://www.seasar.org/issues/browse/MAYAA-27)] -  m:id属性やm:xpath属性は、m:mayaaノード直下のノードでのみ有効となるよう変更しました。
  m:mayaaノード直下でない場合は警告ログが出ます。
  ネストしている場合の誤解が多いことへの対策です。

### Fixes
- (rev.3032) [[JIRA:MAYAA-9](https://www.seasar.org/issues/browse/MAYAA-9)] -  pageスコープにNumber型オブジェクトをセットすると型情報が失われてしまう問題を修正しました。


## Mayaa 1.1.9 : 2007-04-30
### Deprecates
- (rev.2983) InformalPropertyAcceptableに型指定メソッド名を変更しました。古いメソッド名はdeprecatedとしました。

### Changes
- (rev.2984) VirtualPropertyAcceptable#addVirtualProperty()の第2引数を
  java.lang.Objectからjava.io.Serializableに変更しました。
  また、VirtualPropertyに型定義が必須となるよう変更しました。
- (rev.2984) InsertProcessorでVirtualPropertyを使えるよう変更しました。
  これにより、コンポーネントをMLDに定義する際にrequired指定などができるようになりました。
  ただし、VirtualPropertyもInformalPropertyとして扱います。
- (rev.2989) [[JIRA:MAYAA-26](https://www.seasar.org/issues/browse/MAYAA-26)] -  Mayaaファイル内のCDATAはノードと見なさないよう変更しました。
  ボディとして直接 ${} を書いた場合、その内側に CDATA 宣言をできるようになりました。(例: `${ <![CDATA[ '<...>' ]]> }` → `'<...>'`)
- (rev.3001) var 無指定で宣言される変数のスコープをJavaScript Nativeルートではなく、最も外側のpageスコープとしました。
  これまでのように、JavaScript Nativeルートを指定したい場合（共通的なもの）は、`__global__` という予約語のスコープを使うことができます。
### Fixes
- (rev.2946) [[JIRA:MAYAA-25](https://www.seasar.org/issues/browse/MAYAA-25)] -  Deserialize時にresolvePrefixで mayaa.seasar.orgの
  namespaceの親参照でNullPointerExceptionが発生する問題に対応しました。
- (rev.3006) var 無指定対応で、委譲オブジェクトに存在するが自身に存在しないものを
  取得しようとして連鎖しStackOverFlowになる不具合を修正しました。

## Mayaa 1.1.8 : 2007-04-08
### Changes
- (rev.2934) プロセッサの必須属性を空文字として定義していた場合は、属性の定義が
  ない場合と同じ扱いのエラーとしました。
- (rev.2938) attributeプロセッサのvalueを指定しないことで属性値なしの出力を
  可能にしました。旧仕様のHTMLの CHECKED SELECTED や、SGML用途に使用できます。
  ただし非推奨機能としていますのでデフォルトで有効になりません。
  この機能を有効にするには、標準プロセッサ定義である mayaa.mld を直接編集して、
  attributeのvalue属性の`required="true"`を除外する必要があります。
- (rev.2939) 論理値の属性に対して、論理値とならない設定がされている場合に
  ビルドエラーとしました。
- (rev.2940) writeプロセッサでvalue属性以外に、ボディで書き表すことができるようにしました。
    両方指定した場合は valueが優先されます。
- (rev.2940) writeプロセッサでvalue属性内から、bodyTextというwriteプロセッサ専用の値が利用できます。
    bodyTextを参照すると、writeプロセッサのボディ内容が得られます。
    writeプロセッサがボディを持たない場合は、対象のHTMLテンプレート内容となります。
- (rev.2941) insertプロセッサのpathの指定にスクリプトが使えるようになりました。
  これにより実行時に動的にコンポーネントを差し替えることが可能になります。

### Fixes
- (rev.2928) エラーページで`ArrayIndexOutOfBoundsException`が発生する問題を修正しました。
- (rev.2946) [[JIRA:MAYAA-23](https://www.seasar.org/issues/browse/MAYAA-23)] -  echoでの属性参照、JSP カスタムタグでの自動属性セットで
  テンプレートの特殊文字エスケープが解決されない問題を修正しました。
- (rev.2952) 未定義の識別子を参照すると例外になる問題を解消しました。
  これまでは、`if (識別子 == undefined)` といった式が例外が起きないようにするには、
  `beforeRender`などで、`var 識別子;` のように予め定義する必要がありました。
  クライアントJavaScriptと同様の扱いとなるように、NativeJavaScriptエンジンの層でも
  見つからない識別子に対しては、`var 識別子 = undefined` としたものと同等の扱いとしました。
- (rev.2957) 識別子が予約語とぶつかってしまう場合にその識別子を参照するための
  スコープを用意しました。スコープ名は "`_`" で、`_['class']` のように利用します。
  参照順は 現在位置からpageまで、request、session、applicationです。
  `StandardScope`を変更している場合はそれに準じます。
- (rev.2966) `XPathMatchesInjectionResolver`があるだけでビルドにかかる時間を軽減しました。

## Mayaa 1.1.7 : 2007-03-08

### Deprecates
- (rev.2903) APIの`TemplateProcessor#kill()`, `NodeTreeWalker#kill()` を deprecated にしました。
  同様の処理をおこないたい場合は`Object#finalize()`で実装してください。

### Changes
- (rev.2903) ビルド結果キャッシュのファイル名規則を変更しました。
  ディレクトリ名の区切りを.(ピリオド)にして単一のファイル名にしていたところを、`(バッククォート)で区切るようにしました。
- (rev.2911) /default.mayaaを利用するリクエスト(/default.htmlへのアクセス)
  があった場合、内部エラーではなくPageNotFoundExceptionが発生するようにしました。この際、リクエストがあったことをdebugレベルでログ出力します。
  (/default.mayaaの名前は設定に準じます)

### Fixes
- (rev.2878) [[JIRA:MAYAA-17](https://www.seasar.org/issues/browse/MAYAA-17)] -  echoをバインディングしたタグでパスの自動調整が効かない問題を修正しました。
- (rev.2889) [[JIRA:MAYAA-19](https://www.seasar.org/issues/browse/MAYAA-19)] -  xpathのfunctionが使用できない問題を修正しました。
- (rev.2896) XHTMLの場合かつreplace="false"かm:attributeのいずれかを
  使っていた場合に、EMPTY なタグでも閉じタグを出力していた問題を修正しました。
- (rev.2898) [[JIRA:MAYAA-18](https://www.seasar.org/issues/browse/MAYAA-18)] -  echo, elementを複数使う場合のcomponent再帰
  呼び出しで上手くレンダリングされない問題を修正しました。
- (rev.2900) htmlタグやbodyタグのないテンプレートの場合、出力にhtmlタグ
  などが自動的に追加されてしまう問題を修正しました。
- (rev.2903) [[JIRA:MAYAA-21](https://www.seasar.org/issues/browse/MAYAA-21)] -  Specification#kill()が呼ばれるタイミングで
  deadlockが発生する問題を修正しました。
- (rev.2903) [[JIRA:MAYAA-22](https://www.seasar.org/issues/browse/MAYAA-22)] -  GCが頻繁に発生する状況でIllegalStateExceptionが発生する問題を修正しました。

## Mayaa 1.1.6 : 2007-01-14
### Changes
- (rev.2804) warを展開しない状態でもweb.xmlのtaglib-locationが有効になるようにしました。(/WEB-INF/foo.tldの形式)
- (rev.2807) commons-loggingを1.1から1.0.4に戻しました。1.1はLog4Jと一緒に使うと
  undeploy時にNullPointerExceptionが発生することがあります。
- (rev.2811) escapeXmlの処理でシングルクォーテーションをエスケープしないよう変更しました。
- (rev.2819) 自動ビルド時にコンテキストパスが必要となる場合があるため、
  設定で指定できるようにしました。ServiceProviderファイルのEngine設定の
  "autoBuild.contextPath"パラメータです。デフォルトは"/"。
- (rev.2827) 同梱するJaxenのバージョンを1.1にアップデートしました。
- (rev.2831) AutoPageBuilderのファイル名フィルタで、正規表現の場合に
  ファイル名のみとマッチングしていたところをフルパスとマッチングするよう変更しました。
- (rev.2837) Engineを明示的に破棄するメソッド"destroy()"を追加しました。
  destroy()が呼ばれた後のEngineの動作は保証しません。
- (rev.2838) コマンドラインから実行してレンダリング結果ファイルを生成する
  FileGeneratorを追加しました。
- (rev.2852) グローバル関数"throwJava"を追加しました。レンダリング中に
  スクリプトからJavaの例外を投げたい場合に使用します。
  (ServiceCycle#throwJava(Throwable)を追加)

### Fixes
- (rev.2810) InsertProcessorでpathに"/"始まりでも"./"始まりでもないパスを
  指定した場合にページキャッシュが効かない問題を修正しました。
- (rev.2869) m:replace="false"としたテンプレート側タグのid属性にスクリプトを指定
  していた場合に属性が出力されない問題を修正しました。

## Mayaa 1.1.5 : 2006-12-07
### Changes
- (rev.2789) 同梱するRhinoのバージョンを1.6-r5にアップデートしました。
### Fixes
- (rev.2790) MayaaServletのdestroyで例外が発生する問題を修正しました。



## Mayaa 1.1.4 : 2006-12-05
### Changes
- (rev.2767) 1.1.3での変更、"Mayaaのjarを複数Webアプリで共有した場合でも正常に
  動作するよう修正" の内容を削除しました。WAS6.1 で正常に動作しない場合があるため
  です。また、mayaa-webwork2 が動作しない問題も解決します。
- (rev.2769) 書き込み可能なSourceDescriptorを定義できるようにしました。
- (rev.2771) 同梱するJaxenのバージョンを1.1-beta-11にアップデートしました。
- (rev.2772) ClassLoader#getResourceAsStream()をURLConnectionに変更しました。
- (rev.2772) URLConnectionのキャッシュを使うかどうかをシステムプロパティで
  設定できるようにしました。デフォルトはfalse(キャッシュを使わない)です。
  プロパティ名は"org.seasar.mayaa.useURLCache"、IOUtilでのみ使用しています。
  設定方法は変更する可能性があります。
- (rev.2776) 標準のエラー画面に表示する情報を増やしました。
- (rev.2781) 標準のエラー画面をMETA-INF/java.lang.Throwable.htmlのみにしました。

### Fixes
- (rev.2775) [[JIRA:MAYAA-8](https://www.seasar.org/issues/browse/MAYAA-8)] -  WebSphereでundeploy時にMayaaのjarファイルが削除されない
  問題を修正しました。

## Mayaa 1.1.3 : 2006-11-23
### Changes
- (rev.2726) Mayaaのjarを複数Webアプリで共有した場合でも正常に動作するよう修正しました。
- (rev.2731) テンプレートに文字セット指定がされていない場合、UTF-8と見なすように変更しました。
- (rev.2754)JavaScriptからJavaに数値を渡すとき、型情報があるならそれを維持するようにしました。

### Fixes
- (rev.2739) PathAdjusterが適用されない場合があるのを修正しました。
- (rev.2741)[[JIRA:MAYAA-7](https://www.seasar.org/issues/browse/MAYAA-7)] -  Jarの内容を参照するとき、URLConnectionのキャッシュを
  使わないよう修正しました。Mayaaを含むwarをdeploy解除した場合などに、Mayaaが
  参照するリソース(tldなど)を含むjarファイルが削除されない問題の対処となります。
- (rev.2758)[[JIRA:MAYAA-13](https://www.seasar.org/issues/browse/MAYAA-13)] -  レイアウト共有機能で、レイアウト側のhtml, mayaaを更新
  しても反映されない問題を修正しました。


## Mayaa 1.1.2 : 2006-09-27
### Changes
- (rev.2710) ソースコードの文字セットをUTF-8に変更しました。
- (rev.2719) jaxenを1.1-beta-10にしました。
- (rev.2720) エンジン設定にrequestedSuffixEnabledを追加しました。
  デフォルトはfalseで、trueにするとsuffix指定のアクセス(例:index$ja.html)が
  有効になります。suffix指定アクセスはm:templateSuffixの指定よりも優先されます。
  1.1.1まではデフォルトで有効な状態でした。アップグレードの際はご注意ください。

### Fixes
- (rev.2715)[[JIRA:MAYAA-6](https://www.seasar.org/issues/browse/MAYAA-6)] -  elementプロセッサ、echoプロセッサ、およびテンプレート上
  の属性において、「属性がnullの場合に出力しない」という処理で空文字列も出力
  しないようになっていた問題を修正しました。([mayaa-user:281])


## Mayaa 1.1.1 : 2006-09-02
### Changes
- (rev.2693) SpecificationCacheのGCログをINFOレベルからDEBUGレベルに変更しました。
### Fixes
- (rev.2659) ページのビルド結果をserializeする途中でWebアプリケーションを
  終了させたときに例外が出る問題を修正しました。
- (rev.2683)[[JIRA:MAYAA-4](https://www.seasar.org/issues/browse/MAYAA-4)] -  m:injectでインジェクションしたもののnamespaceを持つ
  属性を出力しないよう修正しました。
- (rev.2685) セッションが既に無効化されている場合に例外が発生する問題を修正しました。

## Mayaa 1.1.0 : 2006-08-13
### Changes
- １回のリクエスト中で同じページに一定回数以上forwardした場合に例外を投げる
 ようにしました。この例外はエラーハンドラで扱えません。
 ServiceProvider設定でEngineのパラメータに"forwardLimit"を追加し、
 値に上限回数を指定してください。デフォルトは"10"です。
- [[JIRA:MAYAA-1](https://www.seasar.org/issues/browse/MAYAA-1)] -  ビルドした結果をログに出力する機能を追加しました。デフォルトは無効です。
 ServiceProvider設定でEngineのパラメータに"dumpEnabled"を追加し、
 値に"true"または"false"を指定してください。
 現状ではテンプレート機能、コンポーネント機能には対応していません。
- JSP2.0のDynamic Attributeをサポートしました。
- Maven2 から利用できるようになりました。
   groupId:org.seasar.mayaa / artifactId:mayaa

### Fixes
- パスの自動調整機能でinputタグのsrc属性が対象になっていなかったのを修正しました。
 その他いくつかの属性も対象に追加しました。
- XMLをテンプレートにした場合にデフォルト名前空間指定で例外が出る問題を修正しました。

## Mayaa 1.1.0-beta6 : 2006-07-30
### Changes
- echoプロセッサにname属性を追加し、タグ名を変更できるようにしました。
- SessionScope#getUnderlyingContext()を呼び出したときにSessionオブジェクトを
 生成しないよう修正しました。Sessionオブジェクトがない場合はnullを返します。
- 定義されていないprefixが見つかった場合に例外を出さないようにしました。
- 直接出力となるスクリプトの処理で、処理結果がnullの場合に空文字列を出力する
 ようにしました。
- 標準ではPageNotFoundExceptionのスタックトレースを出力しないよう変更しました。
 INFOレベルで"Page not found: /foo.html"の形(getMessage())のログを出力します。
- beforeRenderおよびafterRenderでbindingを使えるようにしました。
- コンポーネントから更にコンポーネントを呼び出すとき、同じbinding変数名を
 再定義できるように修正しました。
- m:attributeのvalueにがnullの場合、属性を削除するようにしました。
- メモリ使用量を削減しました。
- 描画速度を向上させました。
- ページのビルド結果をserialize/deserializeできるようにしました。
 WEB-INF/.mayaaSpecCacheフォルダ以下にserファイルが作成されます。
 デフォルトは無効です。
 ServiceProvider設定でEngineのパラメータに"pageSerialize"を追加し、
 値に"true"または"false"を指定してください。
- ページのビルド結果をgcの回収対象にするまでの回数を指定できるようにしました。
 デフォルトでは5回目の解放要求で回収対象になります。
 ServiceProvider設定でEngineのパラメータに"surviveLimit"を追加し、
 値に解放要求を無視する回数を整数で指定してください。
- アプリケーション起動時にすべてのページを自動的にビルドする機能を追加しました。
 デフォルトは無効です。
 ServiceProvider設定でEngineのパラメータに"autoBuild"を追加し、
 値に"true"または"false"を指定してください。

### Fixes
- 例外ハンドラページからforwardやredirectができない問題を修正しました。
- コンポーネントを再帰的に使用するとき、forEachが正常に動作しないのを修正しました。
- ページを再読込したときに古いページが解放されない問題を修正しました。
- VariableInfo の NESTED が正常に動作しない問題を修正しました。

## Mayaa 1.1.0-beta5 : 2006-05-28
### Changes
- PrefixMapping, PrefixAwareNameをsingletonにすることで省メモリ化しました。
- スクリプトで`page`を直接した場合、ページスコープのルートを指すよう変更しました。
  `page`がカレントスコープを指すことを想定して使われている場合、意図したものと動作が変わる可能性があります。
  現在スコープを明示したい場合は `page.__current__` を使用してください。
  ※現時点では VariableInfo の NESTED が正常に動作しないという問題があります。
- formatDateプロセッサとformatNumberプロセッサにdefault属性を追加しました。
  writeプロセッサ同様の仕様で、文字列あるいはStringを戻すスクリプトを書けます。

### Fixes
- グローバル関数redirect,error後も描画処理が実行されてしまうのを修正しました。
- HTMLの場合にxmlnsが正しく出力されないのを修正しました。
- echoプロセッサとattributeプロセッサを組み合わせたとき、テンプレート上に存在する
 属性を上書きできていなかったのを修正しました。


## Mayaa 1.1.0-beta4 : 2006-05-07
### Changes
- org.seasar.mayaa.impl.MayaaApplicationFilterを追加しました。
 StrutsServletなどからMayaaへforwardする場合、このFilterを通すことで
 初期化できます。
- ディレクトリへのアクセスが"/"で終わっていない場合に、"/"を付けたパスへ
 リダイレクトするようにしました。
- SimpleTagをサポートしました。
### Fixes
- 初期化時に発生する例外が他の例外を引き起こすのを修正しました。
- 空要素でないタグまで空要素出力していたのを修正しました。


## Mayaa 1.1.0-beta3 : 2006-04-23
### Changes
- .mayaaファイル上に同一のm:idを複数定義した場合、Warningログを出すよう変更
 しました。ServiceProviderファイルでEqualsIDInjectionResolverのパラメータ
 reportDuplicatedIDにfalseを設定すればログを出さないようにできます。
- jaxenを1.1-beta-8から1.1-beta-9にバージョンアップしました。
- 何かのタグのボディ部に直接${}でスクリプトを書いた場合、そのスクリプト内のスコープがタグのスコープと同一になるよう変更しました。
- PageSourceFactoryのパラメータ folder と absolutePath を追加型に変更
 しました。複数書くことで複数のパスを有効にできます。上書きを前提として
 使用している場合には意図しない動作をする可能性がありますので、ご注意ください。
- Mayaaファイルのタグボディとして`&lt;`などのエンティティ参照を書いた場合に
 エンティティ解決していなかったのを修正しました。
 このバグを前提に作られているものがある場合には正常に動作しなくなるかもしれませんのでご注意ください。
### Fixes
- スクリプトのブレースの数の整合性チェックをするとき、文字列内かどうかの判定をしていなかったバグを修正しました。
- パスの自動調整で"/"で終わるパスの場合に例外が出るのを修正しました。


## Mayaa 1.1.0-beta2 : 2006-04-08
### Changes
- スコープ名修飾なしの場合に走査するスコープを追加できるよう、CycleFactoryImplのパラメータに
 addedStandardScopeを追加しました。org.seasar.mayaa.cycle.CycleFactoryファイルに
 `<parameter name="addedStandardScope" value="スコープ名" />`とすることで追加できます。
- EngineのdoServiceにMapを渡せるよう変更しました。渡したMapはpageスコープの初期値として格納されます。
- templatePathPattern, notTemplatePathPatternのいずれにもマッチしない場合の判定を
 以前の通りmime-typeで判定するように戻しました。
- ServiceCycle#load (m:exec src) のスクリプトをキャッシュするよう変更しました。
- QNameをSingletonにしてメモリを節約するよう変更しました。
### Fixes
- テンプレートを探す位置をカスタマイズ(WEB-INF/pageなど)した場合に、テンプレートとして
 扱わないリソース(cssなど)を探せない問題を修正しました。
- JSPカスタムタグで例外が出たときに正しく行番号を表示するよう修正しました。


## Mayaa 1.1.0-beta1 : 2006-03-23
### Changes
- 静的な出力しかしないプロセッサを最適化し、パフォーマンスを向上する機能を追加しました。

## Mayaa 1.0.3 : 2006-03-19
### Changes
- JSPカスタムタグにテンプレートの属性値を渡す機能のうち、属性名のエイリアスを
 定義するときに、テンプレート側属性名としてQNameの形式でセットできるようにしました。
- m:injectに記述したものがhtml,xhtml,xmlの名前空間に属する場合には
 エラーログを出力するよう変更しました。
- HttpSessionオブジェクトを生成するタイミングを、setAttributeするときのみに変更しました。
### Fixes
- JavaScriptの戻り値がvoid (Voidではなく) の場合に例外が発生するのを修正しました。
- 多段階のコンポーネントに同名で引数を渡すと無限ループするのを修正しました。
- テンプレート側に`xmlns:m="http://mayaa.seasar.org"`を記述する場合に、この宣言が
 そのまま出力されてしまうのを修正しました。`http://mayaa.seasar.org` で始まる名前空間宣言はすべて出力されません。


## Mayaa 1.0.2 : 2006-02-26
### Changes
- Java5.0で実行する場合にXMLパーサーに関連するログが大量に出力されるため、それらのログレベルをDEBUGに変更しました。
- JSPカスタムタグにテンプレートの属性値を渡す機能を追加しました。この機能はデフォルトでは無効です。
- writeプロセッサのdefault属性値をエスケープ (escapeXml, escapeEol, escapeWhitespace)
 の対象外にしました。
### Fixes
- ログメッセージのシングルクォーテーション (') のエスケープをしていなかったのを修正しました。
- JSPカスタムタグでdoStartTagの戻り値がEVAL_BODY_BUFFEREDの場合に、
 doAfterBodyでHTMLへ書き込めないバグを修正しました。

## Mayaa 1.0.1 : 2006-01-30
### Changes
- JspFactoryを受け取れなかった場合のJSPバージョンを2.0としました。
- ライブラリのprefixマッピングで"/"開始のときはディレクトリ存在チェックをし、存在しなければInsertに変換しないようにしました。
### Fixes
- 予約名前空間"xml"を使った場合、名前空間"xml"を定義していない場合に例外が発生する問題を修正しました。
- forEachプロセッサに単純なオブジェクト以外を使うと例外が出る問題を修正しました。
- forEachプロセッサでスレッド再利用時にNullPointerExceptionが出る問題を修正しました。

## Mayaa 1.0.0 : 2006-01-23
### Changes
- ServiceCycleにHTTPエラーを返すためのerror(int)とerror(int, String)を追加しました。
 HttpServletResponse#sendErrorに準じます。
