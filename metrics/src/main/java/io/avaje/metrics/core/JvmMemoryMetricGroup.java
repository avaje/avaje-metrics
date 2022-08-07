package io.avaje.metrics.core;

import io.avaje.metrics.Metric;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;

final class JvmMemoryMetricGroup {

  private static final long MEGABYTES = 1024 * 1024L;

  /**
   * Helper interface for Heap and NonHeap MemorySource.
   */
  interface MemoryUsageSource {
    MemoryUsage getUsage();
  }

  /**
   * Heap MemorySource.
   */
  static final class HeapMemoryUsageSource implements MemoryUsageSource {
    final MemoryMXBean memoryMXBean;

    HeapMemoryUsageSource(MemoryMXBean memoryMXBean) {
      this.memoryMXBean = memoryMXBean;
    }

    public MemoryUsage getUsage() {
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

    public MemoryUsage getUsage() {
      return memoryMXBean.getNonHeapMemoryUsage();
    }
  }

  /**
   * Create the Heap Memory based GaugeMetricGroup.
   */
  static List<Metric> createHeapGroup(boolean reportChangesOnly) {
    HeapMemoryUsageSource source = new HeapMemoryUsageSource(ManagementFactory.getMemoryMXBean());
    return createGroup("jvm.memory.heap", source, reportChangesOnly);
  }

  /**
   * Create the NonHeap Memory based GaugeDoubleMetricGroup.
   */
  static List<Metric> createNonHeapGroup(boolean reportChangesOnly) {
    NonHeapMemoryUsageSource source = new NonHeapMemoryUsageSource(ManagementFactory.getMemoryMXBean());
    return createGroup("jvm.memory.nonheap", source, reportChangesOnly);
  }

  private static List<Metric> createGroup(String baseName, MemoryUsageSource source, boolean reportChangesOnly) {
    return new MemUsageGauages(source, baseName).createMetric(reportChangesOnly);
  }

  static final class MemUsageGauages {
    private final MemoryUsageSource source;
    private final String baseName;

    private MemUsageGauages(MemoryUsageSource source, String baseName) {
      this.source = source;
      this.baseName = baseName;
    }

    public List<Metric> createMetric(boolean reportChangesOnly) {
      List<Metric> metrics = new ArrayList<>();
      metrics.add(new DGaugeLongMetric(name("init"), new Init(source), reportChangesOnly));
      metrics.add(new DGaugeLongMetric(name("used"), new Used(source), reportChangesOnly));
      metrics.add(new DGaugeLongMetric(name("committed"), new Committed(source), reportChangesOnly));

      // JRE 8 is not reporting max for non-heap memory
      boolean hasMax = (source.getUsage().getMax() > 0);
      if (hasMax) {
        // also collect Max and Percentage
        metrics.add(new DGaugeLongMetric(name("max"), new Max(source), reportChangesOnly));
        metrics.add(new DGaugeLongMetric(name("pct"), new Pct(source), reportChangesOnly));
      }
      return metrics;
    }

    private String name(String name) {
      return baseName + "." + name;
    }

    private abstract static class Base {
      MemoryUsageSource source;

      Base(MemoryUsageSource source) {
        this.source = source;
      }
    }

    private static final class Init extends Base implements LongSupplier {
      Init(MemoryUsageSource source) {
        super(source);
      }

      @Override
      public long getAsLong() {
        return source.getUsage().getInit() / MEGABYTES;
      }
    }

    private static final class Used extends Base implements LongSupplier {
      Used(MemoryUsageSource source) {
        super(source);
      }

      @Override
      public long getAsLong() {
        return source.getUsage().getUsed() / MEGABYTES;
      }
    }

    private static final class Committed extends Base implements LongSupplier {
      Committed(MemoryUsageSource source) {
        super(source);
      }

      @Override
      public long getAsLong() {
        return source.getUsage().getCommitted() / MEGABYTES;
      }
    }

    private static class Max extends Base implements LongSupplier {
      Max(MemoryUsageSource source) {
        super(source);
      }

      @Override
      public long getAsLong() {
        return source.getUsage().getMax() / MEGABYTES;
      }
    }

    private static class Pct extends Base implements LongSupplier {
      Pct(MemoryUsageSource source) {
        super(source);
      }

      @Override
      public long getAsLong() {
        MemoryUsage memoryUsage = source.getUsage();
        return 100 * memoryUsage.getUsed() / memoryUsage.getMax();
      }
    }
  }

}
