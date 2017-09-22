package org.avaje.metric.util;

import org.testng.annotations.Test;

import java.lang.management.ManagementFactory;
import java.util.List;

import static org.testng.Assert.assertFalse;

public class ProcessHandlerTest {

	@Test
	public void testCommand() throws Exception {

		boolean linux = System.getProperty("os.name").toLowerCase().contains("linux");
		if (linux) {

			String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];

			ProcessResult result = ProcessHandler.command("grep", "Vm", "/proc/" + pid + "/status");

			List<String> lines = result.getStdOutLines();
			System.out.println(lines);
			assertFalse(lines.isEmpty());
		}
	}

}