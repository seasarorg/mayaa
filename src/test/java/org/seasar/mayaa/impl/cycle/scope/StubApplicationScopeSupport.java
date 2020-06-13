/*
 * Copyright 2004-2012 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.mayaa.impl.cycle.scope;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * テストメソッド用にServletコンテナがない環境でFactory群を生成させるためのApplicationScopeオブジェクト。
 * テストケースごとに必要な実装があれば本クラスを継承して個別にオーバーロード実装する。
 * 
 * デフォルトのスタブ実装として、下記の実装を行っている。
 * <dl>
 * <dt>void getResource(String path)</dt><dd>テストクラスのパッケージからの相対パスとして取得する。</dd>
 * <dt>上記以外</dt><dd>UnsupportedOperationExceptionをスローする。</dd>
 * </dl>
 */
public class StubApplicationScopeSupport extends AbstractWritableAttributeScope implements ServletContext {

    private static final long serialVersionUID = 1L;

    private Object _testClassInstance;

    /**
     * コンストラクタ
     * @param testClassInstance 現在実行しているテストクラスのインスタンス
     */
    public StubApplicationScopeSupport(Object testClassInstance) {
        _testClassInstance = testClassInstance;
    }

    /**
     * {@link ServletContext#getResource(String)}の代替実装。
     * テストクラスのパッケージからの相対パスとして取得する。
     * 
     * @param path 要求されているリソースのパス
     */
    public URL getResource(String path) throws MalformedURLException {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return _testClassInstance.getClass().getResource(path);
    }

    // IMPLEMENTS OF AttributeScope
    public void setUnderlyingContext(Object context) {
        throw new UnsupportedOperationException();
    }

    public Object getUnderlyingContext() {
        throw new UnsupportedOperationException();
    }

    public Object newAttribute(String name, Class attributeClass) {
        throw new UnsupportedOperationException();
    }

    public Iterator iterateAttributeNames() {
        throw new UnsupportedOperationException();
    }

    public boolean isAttributeWritable() {
        throw new UnsupportedOperationException();
    }

    public boolean hasAttribute(String name) {
        throw new UnsupportedOperationException();
    }

    public String getScopeName() {
        throw new UnsupportedOperationException();
    }

    public void setSystemID(String systemID) {
        throw new UnsupportedOperationException();
    }

    public void setOnTemplate(boolean onTemplate) {
        throw new UnsupportedOperationException();
    }

    public void setLineNumber(int lineNumber) {
        throw new UnsupportedOperationException();
    }

    public boolean isOnTemplate() {
        throw new UnsupportedOperationException();
    }

    public String getSystemID() {
        throw new UnsupportedOperationException();
    }

    public int getLineNumber() {
        throw new UnsupportedOperationException();
    }

    // IMPLEMENTS OF ParameterAware
    public void setParameter(String name, String value) {
        throw new UnsupportedOperationException();
    }

    public Iterator iterateParameterNames() {
        throw new UnsupportedOperationException();
    }

    public String getParameter(String name) {
        throw new UnsupportedOperationException();
    }

    //----- IMPLEMENTS OF ServletContext
    public String getRealPath(String contextRelatedPath) {
        throw new UnsupportedOperationException();
    }

    public String getMimeType(String systemID) {
        throw new UnsupportedOperationException();
    }

    public ServletContext getContext(String uripath) {
        throw new UnsupportedOperationException();
    }

    public void setAttribute(String name, Object attribute) {
        throw new UnsupportedOperationException();
    }

    public Object getAttribute(String name) {
        throw new UnsupportedOperationException();
    }

    public void removeAttribute(String name) {
        throw new UnsupportedOperationException();
    }

    public int getMajorVersion() {
        throw new UnsupportedOperationException();
    }

    public int getMinorVersion() {
        throw new UnsupportedOperationException();
    }

    public Set getResourcePaths(String path) {
        throw new UnsupportedOperationException();
    }

    public InputStream getResourceAsStream(String path) {
        throw new UnsupportedOperationException();
    }

    public RequestDispatcher getRequestDispatcher(String path) {
        throw new UnsupportedOperationException();
    }

    public RequestDispatcher getNamedDispatcher(String name) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public Servlet getServlet(String name) throws ServletException {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public Enumeration getServlets() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public Enumeration getServletNames() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public void log(String msg) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public void log(Exception exception, String msg) {
        throw new UnsupportedOperationException();
    }

    public void log(String message, Throwable throwable) {
        throw new UnsupportedOperationException();
    }

    public String getServerInfo() {
        throw new UnsupportedOperationException();
    }

    public String getInitParameter(String name) {
        throw new UnsupportedOperationException();
    }

    public Enumeration getInitParameterNames() {
        throw new UnsupportedOperationException();
    }

    public Enumeration getAttributeNames() {
        throw new UnsupportedOperationException();
    }

    public String getServletContextName() {
        throw new UnsupportedOperationException();
    }
}
