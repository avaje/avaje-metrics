package io.avaje.metrics.core;

import io.avaje.metrics.GaugeLong;
import io.avaje.metrics.Metric;
import io.avaje.metrics.MetricName;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;

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
    MetricName heapName = new DefaultMetricName("jvm.memory.heap");
    HeapMemoryUsageSource source = new HeapMemoryUsageSource(ManagementFactory.getMemoryMXBean());
    return createGroup(heapName, source, reportChangesOnly);
  }

  /**
   * Create the NonHeap Memory based GaugeDoubleMetricGroup.
   */
  static List<Metric> createNonHeapGroup(boolean reportChangesOnly) {
    MetricName nonHeapName = new DefaultMetricName("jvm.memory.nonheap");
    NonHeapMemoryUsageSource source = new NonHeapMemoryUsageSource(ManagementFactory.getMemoryMXBean());
    return createGroup(nonHeapName, source, reportChangesOnly);
  }

  private static List<Metric> createGroup(MetricName baseName, MemoryUsageSource source, boolean reportChangesOnly) {
    return new MemUsageGauages(source, baseName).createMetric(reportChangesOnly);
  }

  static final class MemUsageGauages {
    private final MemoryUsageSource source;
    private final MetricName baseName;

    private MemUsageGauages(MemoryUsageSource source, MetricName baseName) {
      this.source = source;
      this.baseName = baseName;
    }

    public List<Metric> createMetric(boolean reportChangesOnly) {
      List<Metric> metrics = new ArrayList<>();
      metrics.add(new DefaultGaugeLongMetric(name("init"), new Init(source), reportChangesOnly));
      metrics.add(new DefaultGaugeLongMetric(name("used"), new Used(source), reportChangesOnly));
      metrics.add(new DefaultGaugeLongMetric(name("committed"), new Committed(source), reportChangesOnly));

      // JRE 8 is not reporting max for non-heap memory
      boolean hasMax = (source.getUsage().getMax() > 0);
      if (hasMax) {
        // also collect Max and Percentage
        metrics.add(new DefaultGaugeLongMetric(name("max"), new Max(source), reportChangesOnly));
        metrics.add(new DefaultGaugeLongMetric(name("pct"), new Pct(source), reportChangesOnly));
      }
      return metrics;
    }

    private MetricName name(String name) {
      return baseName.append(name);
    }

    private abstract static class Base {
      MemoryUsageSource source;

      Base(MemoryUsageSource source) {
        this.source = source;
      }
    }

    private static final class Init extends Base implements GaugeLong {
      Init(MemoryUsageSource source) {
        super(source);
      }

      @Override
      public long getValue() {
        return source.getUsage().getInit() / MEGABYTES;
      }
    }

    private static final class Used extends Base implements GaugeLong {
      Used(MemoryUsageSource source) {
        super(source);
      }

      @Override
      public long getValue() {
        return source.getUsage().getUsed() / MEGABYTES;
      }
    }

    private static final class Committed extends Base implements GaugeLong {
      Committed(MemoryUsageSource source) {
        super(source);
      }

      @Override
      public long getValue() {
        return source.getUsage().getCommitted() / MEGABYTES;
      }
    }

    private static class Max extends Base implements GaugeLong {
      Max(MemoryUsageSource source) {
        super(source);
      }

      @Override
      public long getValue() {
        return source.getUsage().getMax() / MEGABYTES;
      }
    }

    private static class Pct extends Base implements GaugeLong {
      Pct(MemoryUsageSource source) {
        super(source);
      }

      @Override
      public long getValue() {
        MemoryUsage memoryUsage = source.getUsage();
        return 100 * memoryUsage.getUsed() / memoryUsage.getMax();
      }
    }
  }

}
