package org.avaje.metric.jvm;

import org.avaje.metric.GaugeDouble;
import org.avaje.metric.GaugeLong;
import org.avaje.metric.Metric;
import org.avaje.metric.MetricName;
import org.avaje.metric.core.DefaultGaugeDoubleMetric;
import org.avaje.metric.core.DefaultGaugeLongMetric;
import org.avaje.metric.core.DefaultMetricName;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;

public final class JvmMemoryMetricGroup {

  /**
   * Helper interface for Heap and NonHeap MemorySource.
   */
  interface MemoryUsageSource {
    MemoryUsage getUsage();
  }
  
  /**
   * Heap MemorySource.
   */
  static class HeapMemoryUsageSource implements MemoryUsageSource {
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
  static class NonHeapMemoryUsageSource implements MemoryUsageSource {
    final MemoryMXBean memoryMXBean;
    NonHeapMemoryUsageSource(MemoryMXBean memoryMXBean) {
      this.memoryMXBean = memoryMXBean;
    }
    public MemoryUsage getUsage() {
      return memoryMXBean.getNonHeapMemoryUsage();
    }
  }

  private static final long MEGABYTES = 1024*1024L;
  
  /**
   * Create the Heap Memory based GaugeMetricGroup.
   */
  public static List<Metric> createHeapGroup() {
    
    DefaultMetricName heapName = DefaultMetricName.createBaseName("jvm","memory.heap");   
    HeapMemoryUsageSource source = new HeapMemoryUsageSource(ManagementFactory.getMemoryMXBean());
    return createGroup(heapName, source);
  }
 
  /**
   * Create the NonHeap Memory based GaugeDoubleMetricGroup.
   */
  public static List<Metric> createNonHeapGroup() {
    DefaultMetricName nonHeapName = DefaultMetricName.createBaseName("jvm","memory.nonheap");
    NonHeapMemoryUsageSource source = new NonHeapMemoryUsageSource(ManagementFactory.getMemoryMXBean());
    return createGroup(nonHeapName, source);
  }

  private static List<Metric> createGroup(DefaultMetricName baseName, MemoryUsageSource source) {
    return new MemUsageGauages(source, baseName).createMetric();
  }
  
  static class MemUsageGauages {
    private final MemoryUsageSource source;
    private final DefaultMetricName baseName;

    private MemUsageGauages(MemoryUsageSource source, DefaultMetricName baseName) {
      this.source =  source;
      this.baseName = baseName;
    }

    public List<Metric> createMetric() {

      List<Metric> metrics = new ArrayList<>();

      metrics.add(new DefaultGaugeLongMetric(name("init"), new Init(source)));
      metrics.add(new DefaultGaugeLongMetric(name("used"), new Used(source)));
      metrics.add(new DefaultGaugeLongMetric(name("committed"), new Committed(source)));

      // JRE 8 is not reporting max for non-heap memory
      boolean hasMax = (source.getUsage().getMax() > 0);
      if (hasMax) {
        // also collect Max and Percentage
        metrics.add(new DefaultGaugeLongMetric(name("max"), new Max(source)));
        metrics.add(new DefaultGaugeDoubleMetric(name("pct"), new Pct(source)));
      }

      return metrics;
    }

    private MetricName name(String name) {
      return baseName.withName(name);
    }

    private abstract class Base {
      MemoryUsageSource source;
      Base(MemoryUsageSource source) {
        this.source = source;
      }
    }
    private class Init extends Base implements GaugeLong {
      Init(MemoryUsageSource source) {
        super(source);
      }
      @Override
      public long getValue() {
        return source.getUsage().getInit() / MEGABYTES;
      }
    }
    
    private class Used extends Base implements GaugeLong {
      Used(MemoryUsageSource source) {
        super(source);
      }
      @Override
      public long getValue() {
        return source.getUsage().getUsed() / MEGABYTES;
      }
    }
    
    private class Committed extends Base implements GaugeLong {
      Committed(MemoryUsageSource source) {
        super(source);
      }
      @Override
      public long getValue() {
        return source.getUsage().getCommitted() / MEGABYTES;
      }
    }
    
    private class Max extends Base implements GaugeLong {
      Max(MemoryUsageSource source) {
        super(source);
      }
      @Override
      public long getValue() {
        return source.getUsage().getMax() / MEGABYTES;
      }
    }

    private class Pct extends Base implements GaugeDouble {
      Pct(MemoryUsageSource source) {
        super(source);
      }
      @Override
      public double getValue() {
        MemoryUsage memoryUsage = source.getUsage();
        return 100 *  memoryUsage.getUsed() / memoryUsage.getMax() ;
      }
    }
  }
  
}
