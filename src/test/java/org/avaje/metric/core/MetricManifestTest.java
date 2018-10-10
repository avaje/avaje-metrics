package org.avaje.metric.core;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

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
