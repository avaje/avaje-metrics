package org.avaje.metric.jvm;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

import org.avaje.metric.Gauge;
import org.avaje.metric.GaugeMetric;
import org.avaje.metric.GaugeMetricGroup;
import org.avaje.metric.MetricName;

public final class JvmMemoryMetricGroup {

  private static final String[] names = {"pct","init","used","committed","max"};
  
  private static final double MEGABYTES = 1024*1024;
  
  public static GaugeMetricGroup createHeapGroup() {
    
    MetricName heapName = MetricName.createBaseName("jvm","memory.heap");   
    return createGroup(heapName, ManagementFactory.getMemoryMXBean().getHeapMemoryUsage());
  }
 
  public static GaugeMetricGroup createNonHeapGroup() {
    MetricName nonHeapName = MetricName.createBaseName("jvm","memory.nonheap");
    return createGroup(nonHeapName, ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage());
  }

  private static GaugeMetricGroup createGroup(MetricName baseName,  MemoryUsage memoryUsage) {
    Gauge[] gauges = createGauges(memoryUsage);
    GaugeMetric[] group = createGroup(baseName, gauges);
    return new GaugeMetricGroup(baseName, group);
  }
  
  private static GaugeMetric[] createGroup(MetricName baseName,  Gauge[] gauges) {
    GaugeMetric[] group = new GaugeMetric[gauges.length];
    for (int i = 0; i < gauges.length; i++) {
      group[i] = createGaugeMetric(baseName, names[i], gauges[i]);
    }
    return group;
  }
  
  private static GaugeMetric createGaugeMetric(MetricName baseName, String name, Gauge gauge) {
    MetricName specificName = baseName.deriveWithName(name);
    return new GaugeMetric(specificName, gauge, false);
  }
  
 
  private static Gauge[] createGauges(MemoryUsage memoryUsage) {
    return new MemUsageGauages(memoryUsage).getGauges();
  }
  
  static class MemUsageGauages {
    private final MemoryUsage memoryUsage;
    private final Gauge[] gauges;
    
    private MemUsageGauages(MemoryUsage memoryUsage) {
      this.memoryUsage = memoryUsage;
      this.gauges = createGauges();
    }
    
    public Gauge[] getGauges() {
      return gauges;
    }
    
    private Gauge[] createGauges() {
      Gauge[] ga = new Gauge[5];
      ga[0] = new Pct();
      ga[1] = new Init();
      ga[2] = new Used();
      ga[3] = new Committed();
      ga[4] = new Max();
      return ga;
    }
    
    private class Init implements Gauge {
      @Override
      public double getValue() {
        return memoryUsage.getInit() / MEGABYTES;
      }
    }
    
    private class Used implements Gauge {
      @Override
      public double getValue() {
        return memoryUsage.getUsed() / MEGABYTES;
      }
    }
    
    private class Committed implements Gauge {
      @Override
      public double getValue() {
        return memoryUsage.getCommitted() / MEGABYTES;
      }
    }
    
    private class Max implements Gauge {
      @Override
      public double getValue() {
        return memoryUsage.getMax() / MEGABYTES;
      }
    }

    private class Pct implements Gauge {
      @Override
      public double getValue() {
        return 100* memoryUsage.getUsed() / memoryUsage.getMax() ;
      }
    }
  }
  
}
