package org.avaje.metric.jvm;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.avaje.metric.MetricName;
import org.avaje.metric.LoadMetric;

public class GarbageCollectionRateCollection {

  private final Collector[] collectors;
  
  public GarbageCollectionRateCollection(Timer timer) {
    
    List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
    
    collectors = new Collector[garbageCollectorMXBeans.size()];
    for (int i = 0; i < garbageCollectorMXBeans.size(); i++) {
      GarbageCollectorMXBean garbageCollectorMXBean  = garbageCollectorMXBeans.get(i);      
      collectors[i] = new Collector(garbageCollectorMXBean);
    }
    
    timer.scheduleAtFixedRate(new CollectTask(), 10000, 10000);
  }
  
  public String toString() {
    return Arrays.toString(collectors);
  }


  private void collectGcStats() {
    synchronized (this) {
      for (int i = 0; i < collectors.length; i++) {
        collectors[i].collect();
      }
    }
  }
  
  
  private class CollectTask extends TimerTask {
    @Override
    public void run() {
      collectGcStats();
    }
  }
  
  /**
   * A per garbage collector collector.
   */
  private static class Collector {
    
    final GarbageCollectorMXBean garbageCollectorMXBean;
    
    final String name;
    final LoadMetric gcLoadMetric;
    
    final AtomicLong lastCollectionCount = new AtomicLong();
    final AtomicLong lastCollectionTime = new AtomicLong();
    
    Collector(GarbageCollectorMXBean garbageCollectorMXBean) {
      this.garbageCollectorMXBean = garbageCollectorMXBean;
      this.name = garbageCollectorMXBean.getName();
 
      MetricName gcCountName = new MetricName(GarbageCollectionRateCollection.class, this.garbageCollectorMXBean.getName()+".gc");

      gcLoadMetric = new LoadMetric(gcCountName, TimeUnit.MINUTES, "gc", "ms");
    }
    
    private void collect() {
      
      long collectionCount = garbageCollectorMXBean.getCollectionCount();
      long collectionTime = garbageCollectorMXBean.getCollectionTime();
      
      long gcCountSince = collectionCount - lastCollectionCount.get();
      long gcDurationMillisSince = collectionTime -lastCollectionTime.get();
      
      
      gcLoadMetric.addEvent(gcCountSince, gcDurationMillisSince);
     
      lastCollectionCount.set(collectionCount);
      lastCollectionTime.set(collectionTime);
          
      System.out.println("GC: "+name+" "+gcLoadMetric);
    }
    
    public LoadMetric getGCLoadMetric() {
      return gcLoadMetric;
    }
    
    public String toString(){
      return gcLoadMetric.toString();
    }
    
  }
}
