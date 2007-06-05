package org.seasar.mayaa.impl.cycle;

import java.io.Serializable;

import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * スコープ無指定で変数を参照した場合に、順に中身を見ていくスコープをまとめたクラス。
 * 新しく追加したスコープはpageとrequestの間にはいります。
 * 複数追加したとき、後から追加したものが優先になります。
 *
 * @author Koji Suga (Gluegent, Inc.)
 */
public class StandardScope implements Serializable {

    private static final long serialVersionUID = -291469372635600135L;

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

    /**
     * 指定した名前を含むかどうか判定します。
     *
     * @param scopeName 判定したいスコープ名
     * @return 含むならtrue
     */
    public boolean contains(String scopeName) {
        if (StringUtil.isEmpty(scopeName)) {
            throw new IllegalArgumentException();
        }

        String[] scopeNames = _scopeNames;
        for (int i = 0; i < scopeNames.length; i++) {
            if (scopeName.equals(scopeNames[i])) {
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
