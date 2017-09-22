package org.avaje.metric.jvm;

import org.avaje.metric.GaugeLong;
import org.avaje.metric.Metric;
import org.avaje.metric.core.DefaultGaugeLongMetric;
import org.avaje.metric.core.DefaultMetricName;
import org.avaje.metric.util.ProcessHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * OS process memory metrics VmRSS and VmHWM to collect when running on Linux.
 */
public final class JvmProcessMemory {

	private static final Logger logger = LoggerFactory.getLogger(JvmProcessMemory.class);

	private static final long TO_MEGABYTES = 1024L;

	private final String pid;

	/**
	 * Return the list of OS process memory metrics.
	 */
	public static List<Metric> createGauges() {
		return new JvmProcessMemory().metrics();
	}

	/**
	 * Create checking os platform and obtaining the PID.
	 */
	JvmProcessMemory() {
		pid = checkVmRSS(linuxPid());
	}

	/**
	 * Check we can get VmRSS value. Return the PID when we can.
	 */
	private String checkVmRSS(String pid) {

		if (pid == null) {
			// OS not supported (linux only at this stage)
			return  null;
		}
		try {
			// check the command works
			long rss = new Source(pid).getRss();
			return rss > 0 ? pid : null;
		} catch (Exception e) {
			logger.debug("No support for /proc/x/status on this os platform");
			return null;
		}
	}

	/**
	 * Return the PID (when running on linux).
	 */
	private String linuxPid() {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("linux")) {
			String[] parts = ManagementFactory.getRuntimeMXBean().getName().split("@");
			if (parts.length > 0) {
				return parts[0];
			}
		}
		return null;
	}

	/**
	 * Return the metrics for VmRSS and VmHWM.
	 */
	public List<Metric> metrics() {

		List<Metric> metrics = new ArrayList<>();
		if (pid == null) {
			// not supported on the OS platform (linux only support at this stage)
			return metrics;
		}


		DefaultMetricName baseName = DefaultMetricName.createBaseName("jvm","memory.process");
		DefaultMetricName vmRssName = baseName.withName("vmrss");
		DefaultMetricName vmHwmName = baseName.withName("vmhwm");

		Source source = new Source(pid);

		metrics.add(new DefaultGaugeLongMetric(vmRssName, new VmRSS(source)));
		metrics.add(new DefaultGaugeLongMetric(vmHwmName, new VmHWM(source)));
		return metrics;
	}


	/**
	 * Helper that parses the /proc/x/status output getting VmRSS and VmHWM.
	 */
	private static final class ProcessStatus {

		private long vmHWM;
		private long vmRSS;

		ProcessStatus(List<String> lines) {
			for (String line : lines) {
				if (line.startsWith("VmHWM")) {
					vmHWM = parse(line);
				} else if (line.startsWith("VmRSS")) {
					vmRSS = parse(line);
				}
			}
		}

		private long parse(String line) {
			String[] cols = line.trim().split(" ");
			return Long.parseLong(cols[cols.length - 2]);
		}
	}

	private static final class VmHWM implements GaugeLong {

		private final Source source;

		VmHWM(Source source) {
			this.source = source;
		}

		@Override
		public long getValue() {
			return source.getHwm() / TO_MEGABYTES;
		}
	}

	private static final class VmRSS implements GaugeLong {

		private final Source source;

		VmRSS(Source source) {
			this.source = source;
		}

		@Override
		public long getValue() {
			return source.getRss() / TO_MEGABYTES;
		}
	}

	/**
	 * Helper that executes the command to obtain the process memory details we want.
	 */
	private static final class Source {

		private final String pid;

		ProcessStatus procStatus;

		Source(String pid) {
			this.pid = pid;
		}

		private void load() {
			procStatus = new ProcessStatus(ProcessHandler.command("grep", "Vm", "/proc/"+pid+"/status").getStdOutLines());
		}

		long getHwm() {
			return procStatus.vmHWM;
		}

		long getRss() {
			load();
			return procStatus.vmRSS;
		}
	}
}
