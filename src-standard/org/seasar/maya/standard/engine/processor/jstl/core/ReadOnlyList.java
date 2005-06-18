package org.seasar.maya.standard.engine.processor.jstl.core;

/**
 * @author maruo_syunsuke
 */
public interface ReadOnlyList {
	public Object get(int index);
	public int size();
}
