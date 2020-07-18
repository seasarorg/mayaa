# Mayaaの監視および管理

## JMX

MayaaではJMX(Java Management Extensions)を経由して内部状態の監視や一部の動作を変更することができます。
ドメイン名は org.seasar.mayaa です。

| ObjectName                                                 | 説明 |
|------------------------------------------------------------|-----|
| org.seasar.mayaa:type=CacheControl,name=SpecificationCache | Page および Template のビルド結果のキャッシュ |
| org.seasar.mayaa:type=CacheControl,name=CompiledScript     | スクリプトのコンパイル結果のキャッシュ |
| org.seasar.mayaa:type=CacheControl,name=JspTagPool         | JSPタグライブラリのインスタンスプール |
| org.seasar.mayaa:type=CacheControl,name=PrefixAwareName    | 名前空間付きの属性名のキャッシュ |
| org.seasar.mayaa:type=MayaaEngine                          | Mayaa全体の挙動 |
