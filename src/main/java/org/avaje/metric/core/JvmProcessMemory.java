package org.avaje.metric.core;

import org.avaje.metric.GaugeLong;
import org.avaje.metric.Metric;
import org.avaje.metric.MetricName;
import org.avaje.metric.util.ProcessHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.ArrayList;
import java.util.List;

/**
 * OS process memory metrics VmRSS and VmHWM to collect when running on Linux.
 */
final class JvmProcessMemory {

	private static final Logger logger = LoggerFactory.getLogger(JvmProcessMemory.class);

	private static final long TO_MEGABYTES = 1024L;

	private static final long MEGABYTES = 1024L * 1024L;

	private final String pid;

	/**
	 * Return the list of OS process memory metrics.
	 */
	static List<Metric> createGauges(boolean reportChangesOnly) {
		return new JvmProcessMemory().metrics(reportChangesOnly);
	}

	/**
	 * Create checking os platform and obtaining the PID.
	 */
	private JvmProcessMemory() {
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
	public List<Metric> metrics(boolean reportChangesOnly) {

		List<Metric> metrics = new ArrayList<>();
		if (pid == null || MetricManifest.get().disableProcessMemory()) {
			// not supported on the OS platform (linux only support at this stage)
			logger.debug("No process memory collection - os:{} disabled:{}", System.getProperty("os.name"), MetricManifest.get().disableProcessMemory());
			return metrics;
		}

		MemoryWarnWatcher memoryWatcher = memoryWatcher(warnMemoryLevel(), pid);

		MetricName baseName = new DefaultMetricName("jvm.memory.process");
		MetricName vmRssName = baseName.append("vmrss");
		MetricName vmHwmName = baseName.append("vmhwm");

		Source source = new Source(pid);

		metrics.add(new DefaultGaugeLongMetric(vmRssName, new VmRSS(source, memoryWatcher), reportChangesOnly));
		metrics.add(new DefaultGaugeLongMetric(vmHwmName, new VmHWM(source), reportChangesOnly));
		return metrics;
	}

	private MemoryWarnWatcher memoryWatcher(long warnLevel, String pid) {
		if (warnLevel == 0) {
			return new NoMemoryWarn();
		} else {
			long freq = MetricManifest.get().getMemoryWarnFrequency();
			return new MemoryWarn(warnLevel, pid, new DefaultMemoryAlertHandler(freq));
		}
	}

	/**
	 * Does not perform any memory warning.
	 */
	private final static class NoMemoryWarn implements MemoryWarnWatcher {
		public long process(long memoryInMB) {
			return memoryInMB;
		}
	}

	private final static class MemoryWarn implements MemoryWarnWatcher {

		private final long warnLevel;

		private final String pid;

		private final MemoryWarnHandler alertHandler;

		private long lastAlertLevel;

		MemoryWarn(long warnLevel, String pid, MemoryWarnHandler alertHandler) {
			this.warnLevel = warnLevel;
			this.pid = pid;
			this.alertHandler = alertHandler;
		}

		public long process(long memoryInMB) {
			if (memoryInMB >= warnLevel) {
				hitWarnLevel(memoryInMB);
			}
			return memoryInMB;
		}

		private void hitWarnLevel(long memoryInMB) {
			MemoryWarnEvent event = new MemoryWarnEvent(memoryInMB, warnLevel, lastAlertLevel, pid);
			if (memoryInMB > lastAlertLevel) {
				alertHandler.warning(event);
			} else {
				alertHandler.stillWarning(event);
			}
			lastAlertLevel = memoryInMB;
		}

	}

	private long warnMemoryLevel() {

		MetricManifest manifest = MetricManifest.get();
		if (!manifest.hasMemoryWarning()) {
			return 0;
		}

		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
		long heapMax = memoryMXBean.getHeapMemoryUsage().getMax() / MEGABYTES;
		long nonHeapMax = memoryMXBean.getNonHeapMemoryUsage().getMax() / MEGABYTES;

		long total = manifest.getMemoryWarnAbsolute();
		long relative = 0;
		if (total == 0) {
			relative = manifest.getMemoryWarnRelative();
			total = relative + heapMax + nonHeapMax;
		}

		logger.info("registering memory warning at {}mb - heapMax:{}mb nonHeapMax:{}mb relative:{}mb", total, heapMax, nonHeapMax, relative);
		return total;
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

		private final MemoryWarnWatcher warn;

		VmRSS(Source source, MemoryWarnWatcher warn) {
			this.source = source;
			this.warn = warn;
		}

		@Override
		public long getValue() {
			return warn.process( source.getRss() / TO_MEGABYTES);
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
