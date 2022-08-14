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

import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.XMLEntityScanner;

/**
 * HTML構文固有のHTMLEntityScannerを使用するためのEntityManager
 * @author Mitsutaka WATANABE
 */
public class HTMLEntityManager extends XMLEntityManager {
  protected HTMLEntityScanner fHTMLEntityScanner;

  @Override
  public void reset() {
    super.reset();

    if (fHTMLEntityScanner != null) {
      fHTMLEntityScanner.reset(fSymbolTable, this, fErrorReporter);
    }
  }

  @Override
  public XMLEntityScanner getEntityScanner() {
    if (fEntityScanner == null || !(fEntityScanner instanceof HTMLEntityScanner)) {
      if (fHTMLEntityScanner == null) {
        fHTMLEntityScanner = new HTMLEntityScanner();
      }
      fHTMLEntityScanner.reset(fSymbolTable, this, fErrorReporter);

      fEntityScanner = fHTMLEntityScanner;
      fEntityScanner.setCurrentEntity(getCurrentEntity());
    }
    return fEntityScanner;
  }
}
