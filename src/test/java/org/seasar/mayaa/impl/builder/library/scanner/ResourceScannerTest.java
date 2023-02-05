package org.seasar.mayaa.impl.builder.library.scanner;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seasar.mayaa.impl.source.ApplicationFileSourceDescriptor;
import org.seasar.mayaa.source.SourceDescriptor;
import org.seasar.mayaa.test.util.ManualProviderFactory;

public class ResourceScannerTest {
  private ResourceScanner scanner;

  @BeforeEach
  public void setUp() throws Exception {
      ManualProviderFactory.setUp(this);
      ManualProviderFactory.SCRIPT_ENVIRONMENT.initScope();

      scanner = new ResourceScanner();
  }

  @AfterEach
  public void tearDown() throws Exception {
      ManualProviderFactory.tearDown();
  }

  @Test
  public void testScanDefault() {
      scanner.setParameter("root", "META-INF/");
      scanner.setParameter("extension", ".mld");
      scanner.setParameter("extension", ".tld");
      scanner.setParameter("ignore", "META-INF/MANIFEST.MF");

      List<String> expected = Arrays.asList(
        "/META-INF/TestTag.tld",
        "/META-INF/c.tld",
        "/META-INF/c-1_0-rt.tld",
        "/META-INF/c-1_1.tld",
        "/META-INF/fmt-1_0-rt.tld",
        "/META-INF/fmt.tld",
        "/META-INF/fn.tld",
        "/META-INF/issue13.mld",
        "/META-INF/permittedTaglibs.tld",
        "/META-INF/scriptfree.tld",
        "/META-INF/sql-1_0-rt.tld",
        "/META-INF/sql.tld",
        "/META-INF/x-1_0-rt.tld",
        "/META-INF/x.tld"
      );

      List<String> sources = new ArrayList<>();
      scanner.scan().forEachRemaining(e -> {
        sources.add(e.getSystemID());
      });

      Collections.sort(sources);
      Collections.sort(expected);
      assertIterableEquals(expected, sources);
  }

  @Test
  public void testScanIncludeExcludeJar() {
      scanner.setParameter("root", "META-INF/");
      scanner.setParameter("extension", ".mld");
      scanner.setParameter("extension", ".tld");
      scanner.setParameter("ignore", "META-INF/MANIFEST.MF");
      scanner.setParameter("includeJar", "taglibs-standard-*.jar");
      scanner.setParameter("excludeJar", "*");

      List<String> expected = Arrays.asList(
        "/META-INF/TestTag.tld",
        "/META-INF/c.tld",
        "/META-INF/c-1_0-rt.tld",
        "/META-INF/c-1_1.tld",
        "/META-INF/fmt-1_0-rt.tld",
        "/META-INF/fmt.tld",
        "/META-INF/fn.tld",
        "/META-INF/issue13.mld",
        "/META-INF/permittedTaglibs.tld",
        "/META-INF/scriptfree.tld",
        "/META-INF/sql-1_0-rt.tld",
        "/META-INF/sql.tld",
        "/META-INF/x-1_0-rt.tld",
        "/META-INF/x.tld"
      );

      List<String> sources = new ArrayList<>();
      scanner.scan().forEachRemaining(e -> {
        sources.add(e.getSystemID());
      });

      Collections.sort(sources);
      Collections.sort(expected);
      assertIterableEquals(expected, sources);
  }

  @Test
  public void testScanExcludeJar() {
      scanner.setParameter("root", "META-INF/");
      scanner.setParameter("extension", ".mld");
      scanner.setParameter("extension", ".tld");
      scanner.setParameter("ignore", "META-INF/MANIFEST.MF");
      scanner.setParameter("excludeJar", "*");

      List<String> expected = Arrays.asList(
        "/META-INF/TestTag.tld",
        "/META-INF/issue13.mld"
      );

      List<String> sources = new ArrayList<>();
      scanner.scan().forEachRemaining(e -> {
        sources.add(e.getSystemID());
      });

      Collections.sort(sources);
      Collections.sort(expected);
      assertIterableEquals(expected, sources);
  }

  @Test
  public void returnsClassLoaderSourceDescriptor() {
      scanner.setParameter("root", "META-INF/");
      scanner.setParameter("extension", ".mld");
      scanner.setParameter("extension", ".tld");
      scanner.setParameter("ignore", "META-INF/MANIFEST.MF");
      scanner.setParameter("excludeJar", "*");

      List<String> expected = Arrays.asList(
        "/META-INF/TestTag.tld",
        "/META-INF/issue13.mld"
      );

      List<String> sources = new ArrayList<>();
      for (Iterator<SourceDescriptor> it = scanner.scan(); it.hasNext();) {
          SourceDescriptor source = it.next();
          sources.add(source.getSystemID());

          assertInstanceOf(ApplicationFileSourceDescriptor.class, source);
          assertNotNull(source.getInputStream());
      }

      Collections.sort(sources);
      Collections.sort(expected);
      assertIterableEquals(expected, sources);
  }
}
