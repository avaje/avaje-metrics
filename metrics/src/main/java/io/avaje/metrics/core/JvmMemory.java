package io.avaje.metrics.core;

import io.avaje.metrics.Metric;
import io.avaje.metrics.MetricRegistry;
import io.avaje.metrics.Tags;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.function.LongSupplier;

final class JvmMemory {

  private static final long MEGABYTES = 1024 * 1024L;

  /**
   * Helper interface for Heap and NonHeap MemorySource.
   */
  interface MemoryUsageSource {
    MemoryUsage usage();
  }

  /**
   * Heap MemorySource.
   */
  static final class HeapMemoryUsageSource implements MemoryUsageSource {
    final MemoryMXBean memoryMXBean;

    HeapMemoryUsageSource(MemoryMXBean memoryMXBean) {
      this.memoryMXBean = memoryMXBean;
    }

    @Override
    public MemoryUsage usage() {
      return memoryMXBean.getHeapMemoryUsage();
    }
  }

  /**
   * NonHeap MemorySource.
   */
  static final class NonHeapMemoryUsageSource implements MemoryUsageSource {
    final MemoryMXBean memoryMXBean;

    NonHeapMemoryUsageSource(MemoryMXBean memoryMXBean) {
      this.memoryMXBean = memoryMXBean;
    }

    @Override
    public MemoryUsage usage() {
      return memoryMXBean.getNonHeapMemoryUsage();
    }
  }

  /**
   * Create the Heap Memory based GaugeMetricGroup.
   */
  static void createHeapGroup(MetricRegistry registry, boolean reportChangesOnly, boolean withDetails, Tags globalTags) {
    HeapMemoryUsageSource source = new HeapMemoryUsageSource(ManagementFactory.getMemoryMXBean());
    createGroup(registry, "jvm.memory.heap", source, reportChangesOnly, withDetails, globalTags);
  }

  /**
   * Create the NonHeap Memory based GaugeDoubleMetricGroup.
   */
  static void createNonHeapGroup(MetricRegistry registry, boolean reportChangesOnly, boolean withDetails, Tags globalTags) {
    NonHeapMemoryUsageSource source = new NonHeapMemoryUsageSource(ManagementFactory.getMemoryMXBean());
    createGroup(registry, "jvm.memory.nonheap", source, reportChangesOnly, withDetails, globalTags);
  }

  private static void createGroup(MetricRegistry registry, String baseName, MemoryUsageSource source, boolean reportChangesOnly, boolean withDetails, Tags globalTags) {
    new MemUsageGauages(source, baseName, globalTags).createMetric(registry, reportChangesOnly, withDetails);
  }

  static final class MemUsageGauages {
    private final MemoryUsageSource source;
    private final String baseName;
    private final Tags globalTags;

    private MemUsageGauages(MemoryUsageSource source, String baseName, Tags globalTags) {
      this.source = source;
      this.baseName = baseName;
      this.globalTags = globalTags;
    }

    void createMetric(MetricRegistry registry, boolean reportChangesOnly, boolean withDetails) {
      if (withDetails) {
        registry.register(DGaugeLong.once(name("init"), new Init(source)));
      }
      registry.register(DGaugeLong.of(name("used"), new Used(source), reportChangesOnly));
      registry.register(DGaugeLong.of(name("committed"), new Committed(source), reportChangesOnly));
      // JRE 8 is not reporting max for non-heap memory
      boolean hasMax = (source.usage().getMax() > 0);
      if (hasMax) {
        // also collect Max and Percentage
        registry.register(DGaugeLong.once(name("max"), new Max(source)));
        if (withDetails) {
          registry.register(DGaugeLong.of(name("pct"), new Pct(source), reportChangesOnly));
        }
      }
    }

    private Metric.ID name(String name) {
      return Metric.ID.of(baseName + "." + name, globalTags);
    }

    private abstract static class Base {
      final MemoryUsageSource source;

      private Base(MemoryUsageSource source) {
        this.source = source;
      }
    }

    private static final class Init extends Base implements LongSupplier {
      private Init(MemoryUsageSource source) {
        super(source);
      }

      @Override
      public long getAsLong() {
        return source.usage().getInit() / MEGABYTES;
      }
    }

    private static final class Used extends Base implements LongSupplier {
      private Used(MemoryUsageSource source) {
        super(source);
      }

      @Override
      public long getAsLong() {
        return source.usage().getUsed() / MEGABYTES;
      }
    }

    private static final class Committed extends Base implements LongSupplier {
      private Committed(MemoryUsageSource source) {
        super(source);
      }

      @Override
      public long getAsLong() {
        return source.usage().getCommitted() / MEGABYTES;
      }
    }

    private static class Max extends Base implements LongSupplier {
      private Max(MemoryUsageSource source) {
        super(source);
      }

      @Override
      public long getAsLong() {
        return source.usage().getMax() / MEGABYTES;
      }
    }

    private static class Pct extends Base implements LongSupplier {
      private Pct(MemoryUsageSource source) {
        super(source);
      }

      @Override
      public long getAsLong() {
        MemoryUsage memoryUsage = source.usage();
        return 100 * memoryUsage.getUsed() / memoryUsage.getMax();
      }
    }
  }

}
