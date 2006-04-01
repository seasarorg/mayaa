package org.seasar.mayaa.impl.cycle;

import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class StandardScope {

    private String[] _scopeNames = new String[] {
        ServiceCycle.SCOPE_PAGE,
        ServiceCycle.SCOPE_REQUEST,
        ServiceCycle.SCOPE_SESSION,
        ServiceCycle.SCOPE_APPLICATION
    };

    /**
     * Inserting new scope name between PAGE and REQUEST.
     *
     * @param newScopeName
     */
    protected void addScope(String newScopeName) {
        if (StringUtil.isEmpty(newScopeName)) {
            throw new IllegalArgumentException();
        }

        synchronized (this) {
            if (contains(newScopeName) == false) {
                String[] tmp = new String[_scopeNames.length + 1];
                tmp[0] = _scopeNames[0];
                tmp[1] = newScopeName;
                System.arraycopy(_scopeNames, 1, tmp, 2, _scopeNames.length - 1);
                _scopeNames = tmp;
            }
        }
    }

    protected boolean contains(String scopeName) {
        if (StringUtil.isEmpty(scopeName)) {
            throw new IllegalArgumentException();
        }

        for (int i = 0; i < _scopeNames.length; i++) {
            if (scopeName.equals(_scopeNames[i])) {
                return true;
            }
        }
        return false;
    }

    public String get(int index) {
        return _scopeNames[index];
    }

    public int size() {
        return _scopeNames.length;
    }

}
