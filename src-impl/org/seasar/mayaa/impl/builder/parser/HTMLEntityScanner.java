/*
 * Copyright 2004-2022 the Seasar Foundation and the Others.
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
package org.seasar.mayaa.impl.builder.parser;

import java.io.IOException;

import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.XMLEntityScanner;

/**
 * XMLEntityScannerの挙動を変更する。
 */
public class HTMLEntityScanner extends XMLEntityScanner {

    boolean _load(int offset, boolean changeEntity) throws IOException {

        fCurrentEntity.baseCharOffset += (fCurrentEntity.position - fCurrentEntity.startPosition);
        // read characters
        int length = fCurrentEntity.ch.length - offset;
        if (!fCurrentEntity.mayReadChunks && length > XMLEntityManager.DEFAULT_XMLDECL_BUFFER_SIZE) {
            length = XMLEntityManager.DEFAULT_XMLDECL_BUFFER_SIZE;
        }
        int count = fCurrentEntity.reader.read(fCurrentEntity.ch, offset, length);

        // reset count and position
        boolean entityChanged = false;
        if (count != -1) {
            if (count != 0) {
                fCurrentEntity.count = count + offset;
                fCurrentEntity.position = offset;
                fCurrentEntity.startPosition = offset;
            }
        }

        // end of this entity
        else {
            fCurrentEntity.count = offset;
            fCurrentEntity.position = offset;
            fCurrentEntity.startPosition = offset;
            entityChanged = true;
            if (changeEntity) {
                // fEntityManager.endEntity();
                // if (fCurrentEntity == null) {
                //     throw END_OF_DOCUMENT_ENTITY;
                // }
                // handle the trailing edges
                if (fCurrentEntity.position == fCurrentEntity.count) {
                    _load(0, true);
                }
            }
        }
        return entityChanged;
    } // load(int, boolean):boolean

    /**
     * 指定した文字列が大文字小文字区別せず存在していれば消費して真を返す。
     *
     * @param s The string to skip.
     * @return Returns true if the string was skipped.
     *
     * @throws IOException  Thrown if i/o error occurs.
     * @throws EOFException Thrown on end of file.
     */
    public boolean skipString(String s) throws IOException {
        if (fCurrentEntity.position == fCurrentEntity.count) {
            _load(0, true);
        }

        s = s.toUpperCase(); // 大文字に揃える
        final int length = s.length();
        for (int i = 0; i < length; i++) {
            char c = fCurrentEntity.ch[fCurrentEntity.position++];
            c = Character.toUpperCase(c); // 大文字に揃える
            if (c != s.charAt(i)) {
                fCurrentEntity.position -= i + 1;
                return false;
            }
            if (i < length - 1 && fCurrentEntity.position == fCurrentEntity.count) {
                System.arraycopy(fCurrentEntity.ch, fCurrentEntity.count - i - 1, fCurrentEntity.ch, 0, i + 1);
                // REVISIT: Can a string to be skipped cross an
                //          entity boundary? -Ac
                if (_load(i + 1, false)) {
                    fCurrentEntity.startPosition -= i + 1; 
                    fCurrentEntity.position -= i + 1;
                    return false;
                }
            }
        }
        fCurrentEntity.columnNumber += length;
        return true;
    } // skipString(String):boolean

}
