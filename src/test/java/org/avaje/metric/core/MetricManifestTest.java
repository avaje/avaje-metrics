package org.avaje.metric.core;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class MetricManifestTest {

	@Test
	public void testRead() throws Exception {

		MetricManifest metricManifest = MetricManifest.get();
		assertTrue(metricManifest.hasMemoryWarning());
		assertEquals(metricManifest.getMemoryWarnRelative(), 200);
	}

	@Test
	public void testOtherRead() throws Exception {

		MetricManifest metricManifest = MetricManifest.read("metric-other.mf");

		assertTrue(metricManifest.hasMemoryWarning());
		assertEquals(metricManifest.getMemoryWarnRelative(), 300);
		assertTrue(metricManifest.disableProcessMemory());
	}

}