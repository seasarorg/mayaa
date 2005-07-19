/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which 
 * accompanies this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package org.seasar.maya.engine.specification;

/**
 * 名前空間URIとローカル名の組み合わせを保持する識別オブジェクト。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class QName {

    private String _namespaceURI;
    private String _localName;
    
    /**
     * デフォルトの名前空間（http://maya.seasar.org）のQName。 
     * @param localName ローカル名。
     */
    public QName(String localName) {
        this("http://maya.seasar.org", localName);
    }
    
    /**
     * @param namespaceURI 名前空間URI。
     * @param localName ローカル名。
     */
    public QName(String namespaceURI, String localName) {
        if(namespaceURI == null || namespaceURI.length() == 0 ||
                localName == null || localName.length() == 0) {
            throw new IllegalArgumentException();
        }
        _namespaceURI = namespaceURI;
        _localName = localName;
    }
    
    /**
     * 名前空間URIの取得。
     * @return 名前空間URI。
     */
    public String getNamespaceURI() {
        return _namespaceURI;
    }
    
    /**
     * ローカル名の取得。
     * @return ローカル名。
     */
    public String getLocalName() {
        return _localName;
    }
    
    public String toString() {
        return "{" + getNamespaceURI() + "}" + getLocalName();
    }
    
    public boolean equals(Object test) {
        if(test instanceof QName) {
            QName qName = (QName)test;
            return _namespaceURI.equals(qName.getNamespaceURI()) &&
            	_localName.equalsIgnoreCase(qName.getLocalName());
        }
        return false;
    }
    
    public int hashCode() {
        return toString().hashCode();
    }

}
