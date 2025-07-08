package io.avaje.metrics.core;

import io.avaje.metrics.Metric;
import io.avaje.metrics.MetricRegistry;
import io.avaje.metrics.Tags;
import org.jspecify.annotations.Nullable;

import java.lang.management.ManagementFactory;

/**
 * OS process memory metrics VmRSS and VmHWM to collect when running on Linux.
 */
final class JvmProcessMemory {

  private static final long TO_MEGABYTES = 1024L;

  private final @Nullable String pid;

  /**
   * Return the list of OS process memory metrics.
   */
  static void createGauges(MetricRegistry registry, boolean reportChangesOnly, Tags globalTags) {
    new JvmProcessMemory().metrics(registry, reportChangesOnly, globalTags);
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
  private @Nullable String linuxPid() {
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
  public void metrics(MetricRegistry registry, boolean reportChangesOnly, Tags globalTags) {
    if (pid == null) {
      return;
    }
    FileLines procStatus = new FileLines("/proc/" + pid + "/status");
    if (procStatus.exists()) {
      Source source = new Source(procStatus);
      registry.register(DGaugeLong.of(Metric.ID.of("jvm.memory.process.vmrss", globalTags), source::rss, reportChangesOnly));
      registry.register(DGaugeLong.of(Metric.ID.of("jvm.memory.process.vmhwm", globalTags), source::hwm, reportChangesOnly));
    }
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

    long rss() {
      load();
      return vmRSS;
    }

    long hwm() {
      return vmHWM;
    }

  }
}
