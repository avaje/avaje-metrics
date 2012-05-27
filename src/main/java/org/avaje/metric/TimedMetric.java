package org.avaje.metric;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import javax.management.ObjectName;

import org.avaje.metric.stats.MetricStatsCollector;



public final class TimedMetric implements Metric {

  private final MetricName name;
  private final Clock clock;
  
  private final ConcurrentLinkedQueue<TimedMetricEvent> successQueue = new ConcurrentLinkedQueue<TimedMetricEvent>();
  private final ConcurrentLinkedQueue<TimedMetricEvent> errorQueue = new ConcurrentLinkedQueue<TimedMetricEvent>();

  private final MetricStatsCollector successStats;
  private final MetricStatsCollector errorStats;
  private ObjectName errorMBeanName;
  
  public TimedMetric(MetricName name, TimeUnit rateUnit, Clock clock) {
  
    Clock clockToUse = (clock == null) ? Clock.defaultClock() : clock;
    TimeUnit rateToUse = (rateUnit == null) ? TimeUnit.SECONDS : rateUnit;
    this.name = name;
    this.errorMBeanName = name.deriveWithNameSuffix(".error").getMBeanObjectName();
    this.clock = clockToUse;
    this.successStats = new MetricStatsCollector(rateToUse, clockToUse);
    this.errorStats = new MetricStatsCollector(rateToUse, clockToUse);
  }
  
  public String toString() {
    return name.toString();
  }
  
  @Override
  public void clearStatistics() {
    successStats.clear();
    errorStats.clear();
  }

  public ObjectName getErrorMBeanName() {
    return errorMBeanName;
  }

  protected long getTimeMillis() {
    return clock.getTimeMillis();
  }
  
  protected long getTickNanos() {
    return clock.getTickNanos();
  }
  
  public MetricStatistics getSuccessStatistics() {
    return successStats;
  }
  
  public MetricStatistics getErrorStatistics() {
    return errorStats;
  }
  
  public void updateStatistics() {
    
    List<TimedMetricEvent> successEvents = removeEvents(successQueue);
    successStats.update(successEvents);
    
    List<TimedMetricEvent> errorEvents = removeEvents(errorQueue);
    errorStats.update(errorEvents);
  }
  
  private List<TimedMetricEvent> removeEvents(ConcurrentLinkedQueue<TimedMetricEvent> queue) {
    
    ArrayList<TimedMetricEvent> events = new ArrayList<TimedMetricEvent>();
    while(!queue.isEmpty()) {
      TimedMetricEvent metricEvent = queue.remove();
      events.add(metricEvent);
    }
    return events;
  }

  public MetricName getName() {
    return name;
  }
  
  public TimedMetricEvent startEvent() {
    return new TimedMetricEvent(this);
  }

  protected void endWithSuccess(TimedMetricEvent event) {
    successQueue.add(event);
  }
  
  protected void endWithError(TimedMetricEvent event) {
    errorQueue.add(event);
  }
  
}
