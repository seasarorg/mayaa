package org.seasar.mayaa.impl.builder.parser;

import java.util.Arrays;

import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.xni.XNIException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public abstract class HTMLErrorReporter extends XMLErrorReporter{
  ErrorHandler _errorHandler;
  HTMLErrorReporter(ErrorHandler errorHandler) {
    _errorHandler = errorHandler;
  }

  public void setErrorHandler(ErrorHandler errorHandler) {
    _errorHandler = errorHandler;
  }

  abstract void reportError(HtmlLocation location, ParseError error, Object[] args);
}

class DefaultHTMLErrorHandler extends HTMLErrorReporter {

  DefaultHTMLErrorHandler(ErrorHandler errorHandler) {
    super(errorHandler);
  }

  @Override
  void reportError(HtmlLocation location, ParseError error, Object[] args) {
    if (_errorHandler != null) {
      try {
        switch (error.severity) {
        case ParseError.SEVERITY_FATAL_ERROR:
          _errorHandler.fatalError(createSAXException(location, error, args));
          break;
        case ParseError.SEVERITY_ERROR:
          _errorHandler.error(createSAXException(location, error, args));
          break;
        case ParseError.SEVERITY_WARNING:
          _errorHandler.warning(createSAXException(location, error, args));
          break;
        }
      } catch (SAXException e) {
        throw new XNIException(e);
      }
    }
  }

  private SAXParseException createSAXException(HtmlLocation location, ParseError error, Object[] args) {
    return new SAXParseException(error.name() + " (args: " + Arrays.toString(args) + ") " + location.getTextAroundCurrent(), location.getPublicId(), location.getSystemId(), location.getLineNumber(), location.getColumnNumber());
  }

}