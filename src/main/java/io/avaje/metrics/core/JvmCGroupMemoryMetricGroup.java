package io.avaje.metrics.core;

import io.avaje.metrics.GaugeLongMetric;
import io.avaje.metrics.Metric;
import io.avaje.metrics.MetricName;

import java.util.ArrayList;
import java.util.List;

class JvmCGroupMemoryMetricGroup {

  private static final long MEG = 1_048_576;

  private final List<Metric> metrics = new ArrayList<>();

  /**
   * Return the list of OS process memory metrics.
   */
  static List<Metric> createGauges() {
    return new JvmCGroupMemoryMetricGroup().metrics();
  }

  private void add(Metric metric) {
    if (metric != null) {
      metrics.add(metric);
    }
  }

  private List<Metric> metrics() {
    FileLines memLimit = new FileLines("/sys/fs/cgroup/memory/memory.limit_in_bytes");
    FileLines memUsage = new FileLines("/sys/fs/cgroup/memory/memory.usage_in_bytes");
    if (memLimit.exists() && memUsage.exists()) {
      long limitInBytes = memLimit.single();
      MemSource source = new MemSource(limitInBytes, memUsage);
      add(createMemoryUsage(source));
      if (limitInBytes < 1_000_000_000_000L) {
        // only include when limit is in effect
        add(createMemoryLimit(source));
        add(createMemoryPctUsage(source));
      }
    }
    return metrics;
  }

  static long toMegaBytes(long bytes) {
    return bytes / MEG;
  }

  static class MemSource {

    private final FileLines memUsage;

    private long limitMb;
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

    long getLimitMb() {
      return limitMb;
    }

    long getUsageMb() {
      read();
      return usageMb;
    }

    long getPctUsage() {
      return pctUsage;
    }
  }

  GaugeLongMetric createMemoryUsage(MemSource source) {
    return new DefaultGaugeLongMetric(name("jvm.cgroup.memory.usageMb"), source::getUsageMb);
  }

  GaugeLongMetric createMemoryPctUsage(MemSource source) {
    return new DefaultGaugeLongMetric(name("jvm.cgroup.memory.pctUsage"), source::getPctUsage);
  }

  GaugeLongMetric createMemoryLimit(MemSource source) {
    return new DefaultGaugeLongMetric(name("jvm.cgroup.memory.limit"), source::getLimitMb);
  }

  private MetricName name(String s) {
    return new DefaultMetricName(s);
  }

}
