package org.seasar.mayaa.impl.cycle.script.rhino;

import org.mozilla.javascript.IdFunctionObject;
import org.mozilla.javascript.NativeJavaPackage;
import org.mozilla.javascript.NativeJavaTopPackage;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.seasar.mayaa.cycle.scope.AttributeScope;
import org.seasar.mayaa.impl.cycle.CycleUtil;

public class VirtualNativeObject extends NativeObject {

	private static final long serialVersionUID = 4781599810286253299L;
	private static final String GLOBAL_OBJECT = "__global__";
	
	private boolean _initializing = true;
	
	private NativeObject _copyGlobal = new NativeObject();
	
	public void initEnd() {
		_initializing = false;
	}
	
	public boolean has(String name, Scriptable start) {
		return GLOBAL_OBJECT.equals(name) || super.has(name, start);
	}

	public Object get(String name, Scriptable start) {
		if (GLOBAL_OBJECT.equals(name)) return _copyGlobal;
		
		return super.get(name, start);
	}

	public void put(String name, Scriptable start, Object value) {
		// ignore overwrite
		if (GLOBAL_OBJECT.equals(name)) return;
		
		if (_initializing
				|| value instanceof IdFunctionObject
				|| value instanceof NativeJavaTopPackage
				|| value instanceof NativeJavaPackage) {
			super.put(name, start, value);
			_copyGlobal.put(name, _copyGlobal, value);
			return;
		}
		AttributeScope pageScope = CycleUtil.getPageScope();
		if (pageScope != null) {
			pageScope.setAttribute(name, value);
		} else {
			super.put(name, start, value);
			_copyGlobal.put(name, _copyGlobal, value);
		}
	}

}
