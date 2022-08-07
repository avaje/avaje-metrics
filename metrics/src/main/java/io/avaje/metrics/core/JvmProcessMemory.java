package io.avaje.metrics.core;

import io.avaje.metrics.Metric;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * OS process memory metrics VmRSS and VmHWM to collect when running on Linux.
 */
final class JvmProcessMemory {

  private static final long TO_MEGABYTES = 1024L;

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
    pid = linuxPid();
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
      return metrics;
    }
    FileLines procStatus = new FileLines("/proc/" + pid + "/status");
    if (procStatus.exists()) {
      Source source = new Source(procStatus);
      metrics.add(new DGaugeLong("jvm.memory.process.vmrss", source::getRss, reportChangesOnly));
      metrics.add(new DGaugeLong("jvm.memory.process.vmhwm", source::getHwm, reportChangesOnly));
    }
    return metrics;
  }

  /**
   * Helper that executes the command to obtain the process memory details we want.
   */
  static final class Source {

    private final FileLines statusFile;
    private long vmHWM;
    private long vmRSS;

    Source(FileLines statusFile) {
      this.statusFile = statusFile;
    }

    private long parse(String line) {
      String[] cols = line.trim().split(" ");
      return Long.parseLong(cols[cols.length - 2]);
    }

    private void load() {
      statusFile.readLines(line -> {
        if (line.startsWith("VmHWM")) {
          vmHWM = parse(line) / TO_MEGABYTES;
        } else if (line.startsWith("VmRSS")) {
          vmRSS = parse(line) / TO_MEGABYTES;
          return false;
        }
        return true;
      });
    }

    long getRss() {
      load();
      return vmRSS;
    }

    long getHwm() {
      return vmHWM;
    }

  }
}
