package io.avaje.metrics.core;

import io.avaje.metrics.GaugeLong;
import io.avaje.metrics.MetricRegistry;

final class JvmCGroupMemory {

  private static final long MEG = 1_048_576;

  static void createGauges(MetricRegistry registry, boolean reportChangesOnly) {
    new JvmCGroupMemory().metrics(registry, reportChangesOnly);
  }

  private void metrics(MetricRegistry registry, boolean reportChangesOnly) {
    FileLines memLimit = new FileLines("/sys/fs/cgroup/memory/memory.limit_in_bytes");
    FileLines memUsage = new FileLines("/sys/fs/cgroup/memory/memory.usage_in_bytes");
    if (memLimit.exists() && memUsage.exists()) {
      long limitInBytes = memLimit.single();
      MemSource source = new MemSource(limitInBytes, memUsage);
      registry.register(usage(source, reportChangesOnly));
      if (limitInBytes < 1_000_000_000_000L) {
        // only include when limit is in effect
        registry.register(limit(source, reportChangesOnly));
        registry.register(pctUsage(source, reportChangesOnly));
      }
    }
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

    long pctUsage() {
      return pctUsage;
    }
  }

  GaugeLong usage(MemSource source, boolean reportChangesOnly) {
    return DGaugeLong.of("jvm.cgroup.memory.usage", source::usageMb, reportChangesOnly);
  }

  GaugeLong pctUsage(MemSource source, boolean reportChangesOnly) {
    return DGaugeLong.of("jvm.cgroup.memory.pctUsage", source::pctUsage, reportChangesOnly);
  }

  GaugeLong limit(MemSource source, boolean reportChangesOnly) {
    return DGaugeLong.of("jvm.cgroup.memory.limit", source::limitMb, reportChangesOnly);
  }

}
