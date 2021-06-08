package io.avaje.metrics.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MetricManifestTest {

  @Test
  public void testRead() {

    MetricManifest metricManifest = MetricManifest.get();
    assertTrue(metricManifest.hasMemoryWarning());
    assertEquals(metricManifest.getMemoryWarnRelative(), 200);
  }

  @Test
  public void testOtherRead() {

    MetricManifest metricManifest = MetricManifest.read("metrics-other.mf");

    assertTrue(metricManifest.hasMemoryWarning());
    assertEquals(metricManifest.getMemoryWarnRelative(), 300);
    assertTrue(metricManifest.disableProcessMemory());
  }

}
