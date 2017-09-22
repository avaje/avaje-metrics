package org.avaje.metric.jvm;

import org.avaje.metric.Metric;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;

public class JvmProcessMemoryTest {

	@Test
	public void testGetMetrics() throws Exception {

		List<Metric> gauges = JvmProcessMemory.createGauges();
		dump(gauges);
	}

	private void dump(List<Metric> metrics) throws InterruptedException {
		for (int i = 0; i < 5; i++) {
			if (!metrics.isEmpty()) {
				Thread.sleep(1000);
				List<Metric> m = new ArrayList<>();
				for (Metric metric : metrics) {
					metric.collectStatistics(m);
				}

				String values = m.toString();
				System.out.println(values);
				if (i == 0) {
					// will be empty when values not changing, always have values
					// on first run though so assert then
					assertTrue(values.contains("jvm.memory.process.vmhwm"));
					assertTrue(values.contains("jvm.memory.process.vmrss"));
				}
			}
		}
	}

}