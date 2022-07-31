package org.seasar.mayaa.impl.builder.parser;

public class ParserEncodingChangedException extends RuntimeException {
  private String _encoding;

  public ParserEncodingChangedException(String encoding) {
    _encoding = encoding;
  }

  /**
   * 変更後のエンコーディング名を返す。
   * @return 変更後のエンコーディング名
   */
  public String getEncoding() {
    return _encoding;
  }

}
