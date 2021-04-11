package org.seasar.mayaa.impl.source;

import org.seasar.mayaa.impl.builder.library.scanner.SourceAlias;
import org.seasar.mayaa.source.SourceDescriptor;

/**
 * jar内のファイルやURIの別称が与えられているSourceDescriptorを表すインタフェース
 * @author Mitsutaka Watanabe
 */
public interface HavingAliasSourceDescriptor extends SourceDescriptor {

  /**
   * 別名を表すエイリアスオブジェクトを返す
   * @return SourceAliasインスタンス。エイリアスが未定義の場合はnull
   */
  SourceAlias getAlias();
}
