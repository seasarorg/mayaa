package org.seasar.mayaa.impl.knowledge;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.util.StringUtil;

public class HTMLKnowledge {
  /**
   * HTMLの void elements を表すQNameの一覧
   * https://html.spec.whatwg.org/multipage/syntax.html#elements-2
   */
  private static final HashSet<QName> HTML_VOID_ELEMENTS = new HashSet<QName>(Arrays.asList(
      CONST_IMPL.QH_AREA,
      CONST_IMPL.QH_BASE,
      CONST_IMPL.QH_BR,
      CONST_IMPL.QH_COL,
      CONST_IMPL.QH_COMMAND,
      CONST_IMPL.QH_EMBED,
      CONST_IMPL.QH_HR,
      CONST_IMPL.QH_IMG,
      CONST_IMPL.QH_INPUT,
      CONST_IMPL.QH_KEYGEN,
      CONST_IMPL.QH_LINK,
      CONST_IMPL.QH_META,
      CONST_IMPL.QH_PARAM,
      CONST_IMPL.QH_SOURCE,
      CONST_IMPL.QH_TRACK,
      CONST_IMPL.QH_WBR,
      CONST_IMPL.QX_AREA,
      CONST_IMPL.QX_BASE,
      CONST_IMPL.QX_BR,
      CONST_IMPL.QX_COL,
      CONST_IMPL.QX_COMMAND,
      CONST_IMPL.QX_EMBED,
      CONST_IMPL.QX_HR,
      CONST_IMPL.QX_IMG,
      CONST_IMPL.QX_INPUT,
      CONST_IMPL.QX_KEYGEN,
      CONST_IMPL.QX_LINK,
      CONST_IMPL.QX_META,
      CONST_IMPL.QX_PARAM,
      CONST_IMPL.QX_SOURCE,
      CONST_IMPL.QX_TRACK,
      CONST_IMPL.QX_WBR,
      CONST_IMPL.QX_BASEFONT,  // transitional
      CONST_IMPL.QX_ISINDEX,   // transitional
      CONST_IMPL.QX_FRAME,     // nonstandard
      CONST_IMPL.QX_BGSOUND,   // nonstandard
      CONST_IMPL.QX_NEXTID,    // nonstandard
      CONST_IMPL.QX_SOUND,     // nonstandard
      CONST_IMPL.QX_SPACER     // nonstandard
        )
    );
  private static final HashSet<String> HTML_VOID_ELEM_LOCALPART = new HashSet<>();
  static {
    for (QName qn: HTML_VOID_ELEMENTS) {
      HTML_VOID_ELEM_LOCALPART.add(qn.getLocalName());
    }
  }

  private static final HashMap<String, Character> HTML_ENTITIES = new HashMap<>();
  static {
    HTML_ENTITIES.put("lt", Character.valueOf('<'));
    HTML_ENTITIES.put("gt", Character.valueOf('>'));
    HTML_ENTITIES.put("amp", Character.valueOf('&'));
    HTML_ENTITIES.put("quot", Character.valueOf('"'));
    HTML_ENTITIES.put("apos", Character.valueOf('\''));
  }
  /**
   * HTML仕様のVoid Elementであればtrueを返す。
   * 対応する名前空間は HTML4 (http://www.w3.org/TR/html4) および HTML (http://www.w3.org/1999/xhtml)
   * 
   * <ul>
   * <li>http://www.w3.org/TR/html4</li>
   * <li>http://www.w3.org/1999/xhtml</li>
   * </ul>
   * 
   * エレメントの仕様はW3Cを参照
   * https://www.w3.org/TR/2011/WD-html5-20110525/namespaces.html
   * 
   * @param qname エレメントのQName
   * @return Void Elementであればtrue
   */
  public static boolean isVoidElement(final QName qname) {
    return HTML_VOID_ELEMENTS.contains(qname);
  }

  public static boolean isVoidElementLocalPart(final String localPart) {
    return HTML_VOID_ELEM_LOCALPART.contains(localPart);
  }

  /**
   * 取り扱う必要のある文字参照名かを判定する。
   * &amp;amp; &amp;lt; &amp;gt; &amp;quot; &amp;apos; のみ。
   * @param name 文字参照名
   * @return　HTML定義の文字参照なら true
   */
  public static boolean isHTMLEntitiy(final String name) {
    return HTML_ENTITIES.containsKey(name);
  }

  /**
   * HTML定義の文字参照を文字に変換する。
   * @param name 文字参照名
   * @return　HTML定義の文字参照の文字(Character型)。未定義の文字参照名なら nullを返す。
   */
  public static Character convertHTMLEntitiy(final String name) {
    Objects.requireNonNull(name);
    if (name.length() > 0 && name.charAt(0) == '#') {
      String num = name.substring(1);
      try {
        int radix = 10;
        int c = num.charAt(0);
        if (c == 'x' || c == 'X') {
          num = num.substring(1);
          radix = 16;
        }
        int value = Integer.parseInt(num, radix);
        if (Character.MIN_VALUE <= value && value <= Character.MAX_VALUE) {
          return Character.valueOf((char) value);
        } else {
          return null;
        }
      } catch (Exception e) {
        return null;
      }
    }
    return HTML_ENTITIES.get(name);
  }

  public static String escapeHTMLEntity(String text) {
    return StringUtil.escapeXml(text);
  }

  public static String escapeHTMLEntityExceptAmp(String text) {
    return StringUtil.escapeXmlWithoutAmp(text);
  }

  public static String unescapeHTMLEntity(String text) {
    int lastMatchedIndex = 0;

    int matchedIndex = text.indexOf('&', lastMatchedIndex);
    if (matchedIndex == -1) {
      return text;
    }

    StringBuilder sb = new StringBuilder(text.length());
    while (matchedIndex != -1) {
      sb.append(text.substring(lastMatchedIndex, matchedIndex));
      int colonIndex = text.indexOf(';', matchedIndex + 1);
      if (colonIndex == -1) {
        // コロンが見つからない場合は&も含めて末尾まで追加して終了する。
        lastMatchedIndex = matchedIndex;
        break;
      }
      
      final String name = text.substring(matchedIndex + 1, colonIndex);
      Character ch = convertHTMLEntitiy(name);
      if (ch == null) {
        sb.append(text.substring(matchedIndex, colonIndex - 1));
      } else {
        sb.append(ch);
      }
      lastMatchedIndex = colonIndex + 1;
      matchedIndex = text.indexOf('&', lastMatchedIndex);
    }
    sb.append(text.substring(lastMatchedIndex));
    return sb.toString();
  }
}
