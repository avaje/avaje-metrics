package io.avaje.metrics.core;

import io.avaje.metrics.GaugeLong;
import io.avaje.metrics.Metric;

import java.util.ArrayList;
import java.util.List;

final class JvmCGroupMemoryMetricGroup {

  private static final long MEG = 1_048_576;

  private final List<Metric> metrics = new ArrayList<>();

  /**
   * Return the list of OS process memory metrics.
   */
  static List<Metric> createGauges(boolean reportChangesOnly) {
    return new JvmCGroupMemoryMetricGroup().metrics(reportChangesOnly);
  }

  private void add(Metric metric) {
    if (metric != null) {
      metrics.add(metric);
    }
  }

  private List<Metric> metrics(boolean reportChangesOnly) {
    FileLines memLimit = new FileLines("/sys/fs/cgroup/memory/memory.limit_in_bytes");
    FileLines memUsage = new FileLines("/sys/fs/cgroup/memory/memory.usage_in_bytes");
    if (memLimit.exists() && memUsage.exists()) {
      long limitInBytes = memLimit.single();
      MemSource source = new MemSource(limitInBytes, memUsage);
      add(createMemoryUsage(source, reportChangesOnly));
      if (limitInBytes < 1_000_000_000_000L) {
        // only include when limit is in effect
        add(createMemoryLimit(source, reportChangesOnly));
        add(createMemoryPctUsage(source, reportChangesOnly));
      }
    }
    return metrics;
  }

  static long toMegaBytes(long bytes) {
    return bytes / MEG;
  }

  static final class MemSource {

    private final FileLines memUsage;
    private final long limitMb;
    private long usageMb;
    private long pctUsage;

    MemSource(long memLimit, FileLines memUsage) {
      this.limitMb = toMegaBytes(memLimit);
      this.memUsage = memUsage;
    }

    private void read() {
      synchronized (this) {
        final long usageBytes = memUsage.single();
        this.usageMb = toMegaBytes(usageBytes);
        this.pctUsage = usageMb * 100 / limitMb;
      }
    }

    long limitMb() {
      return limitMb;
    }

    long usageMb() {
      read();
      return usageMb;
    }

    long getPctUsage() {
      return pctUsage;
    }
  }

  GaugeLong createMemoryUsage(MemSource source, boolean reportChangesOnly) {
    return new DGaugeLong("jvm.cgroup.memory.usageMb", source::usageMb, reportChangesOnly);
  }

  GaugeLong createMemoryPctUsage(MemSource source, boolean reportChangesOnly) {
    return new DGaugeLong("jvm.cgroup.memory.pctUsage", source::getPctUsage, reportChangesOnly);
  }

  GaugeLong createMemoryLimit(MemSource source, boolean reportChangesOnly) {
    return new DGaugeLong("jvm.cgroup.memory.limit", source::limitMb, reportChangesOnly);
  }

}
