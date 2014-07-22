package org.avaje.metric.jvm;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

import org.avaje.metric.Gauge;
import org.avaje.metric.GaugeMetric;
import org.avaje.metric.GaugeMetricGroup;
import org.avaje.metric.MetricName;
import org.avaje.metric.core.DefaultGaugeMetric;
import org.avaje.metric.core.DefaultGaugeMetricGroup;
import org.avaje.metric.core.DefaultMetricName;

public final class JvmMemoryMetricGroup {

  /**
   * Helper interface for Heap and NonHeap MemorySource.
   */
  static interface MemoryUsageSource {
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
  
  private static final String[] names = {"pct","init","used","committed","max"};
  
  private static final double MEGABYTES = 1024*1024;
  
  /**
   * Create the Heap Memory based GaugeMetricGroup.
   */
  public static GaugeMetricGroup createHeapGroup() {
    
    DefaultMetricName heapName = DefaultMetricName.createBaseName("jvm","memory.heap");   
    HeapMemoryUsageSource source = new HeapMemoryUsageSource(ManagementFactory.getMemoryMXBean());
    return createGroup(heapName, source);
  }
 
  /**
   * Create the NonHeap Memory based GaugeMetricGroup.
   */
  public static GaugeMetricGroup createNonHeapGroup() {
    DefaultMetricName nonHeapName = DefaultMetricName.createBaseName("jvm","memory.nonheap");
    NonHeapMemoryUsageSource source = new NonHeapMemoryUsageSource(ManagementFactory.getMemoryMXBean());
    return createGroup(nonHeapName, source);
  }

  private static GaugeMetricGroup createGroup(DefaultMetricName baseName, MemoryUsageSource source) {
    Gauge[] gauges = createGauges(source);
    GaugeMetric[] group = createGroup(baseName, gauges);
    return new DefaultGaugeMetricGroup(baseName, group);
  }
  
  private static GaugeMetric[] createGroup(DefaultMetricName baseName,  Gauge[] gauges) {
    GaugeMetric[] group = new GaugeMetric[gauges.length];
    for (int i = 0; i < gauges.length; i++) {
      group[i] = createGaugeMetric(baseName, names[i], gauges[i]);
    }
    return group;
  }
  
  private static GaugeMetric createGaugeMetric(DefaultMetricName baseName, String name, Gauge gauge) {
    MetricName specificName = baseName.deriveWithName(name);
    return new DefaultGaugeMetric(specificName, gauge);
  }
  
 
  private static Gauge[] createGauges(MemoryUsageSource source) {
    return new MemUsageGauages(source).getGauges();
  }
  
  static class MemUsageGauages {
    private final Gauge[] gauges;
    
    private MemUsageGauages(MemoryUsageSource source) {
      this.gauges = createGauges(source);
    }
    
    public Gauge[] getGauges() {
      return gauges;
    }
    
    private Gauge[] createGauges(MemoryUsageSource source) {
      Gauge[] ga = new Gauge[5];
      ga[0] = new Pct(source);
      ga[1] = new Init(source);
      ga[2] = new Used(source);
      ga[3] = new Committed(source);
      ga[4] = new Max(source);
      return ga;
    }
  
    private abstract class Base {
      MemoryUsageSource source;
      Base(MemoryUsageSource source) {
        this.source = source;
      }
    }
    private class Init extends Base implements Gauge {
      Init(MemoryUsageSource source) {
        super(source);
      }
      @Override
      public double getValue() {
        return source.getUsage().getInit() / MEGABYTES;
      }
    }
    
    private class Used extends Base implements Gauge {
      Used(MemoryUsageSource source) {
        super(source);
      }
      @Override
      public double getValue() {
        return source.getUsage().getUsed() / MEGABYTES;
      }
    }
    
    private class Committed extends Base implements Gauge {
      Committed(MemoryUsageSource source) {
        super(source);
      }
      @Override
      public double getValue() {
        return source.getUsage().getCommitted() / MEGABYTES;
      }
    }
    
    private class Max extends Base implements Gauge {
      Max(MemoryUsageSource source) {
        super(source);
      }
      @Override
      public double getValue() {
        return source.getUsage().getMax() / MEGABYTES;
      }
    }

    private class Pct extends Base implements Gauge {
      Pct(MemoryUsageSource source) {
        super(source);
      }
      @Override
      public double getValue() {
        MemoryUsage memoryUsage = source.getUsage();
        return 100* memoryUsage.getUsed() / memoryUsage.getMax() ;
      }
    }
  }
  
}
