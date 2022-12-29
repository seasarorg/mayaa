package org.seasar.mayaa.impl;

/**
 * 内部挙動変更に伴って互換性維持のための制御が必要な時にスローされる例外クラス。
 */
public class NeedCompatibilityException extends RuntimeException {
  public enum CompatibilityType {
    LoadFactoryDefinitionForwardWay
  }

  private CompatibilityType compatibilityType;
  private Object[] args;

  public NeedCompatibilityException(CompatibilityType compatibilityType, Object ... args) {
    super();
    this.compatibilityType = compatibilityType;
    this.args = args;
  }

  public CompatibilityType getCompatibilityType() {
    return compatibilityType;
  }

  public Object[] getArgs() {
    return args;
  }

}
