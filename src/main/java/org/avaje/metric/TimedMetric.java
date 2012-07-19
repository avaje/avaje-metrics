package org.avaje.metric;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import javax.management.ObjectName;

import org.avaje.metric.stats.ValueEventCollector;

/**
 * Designed to capture the duration of timed events.
 * <p>
 * The major difference compared with ValueMetric is that it is specifically
 * oriented towards collecting time duration and provides separate statistics
 * for success and error completion.
 * </p>
 */
public final class TimedMetric implements Metric {

  private final MetricName name;
  private final Clock clock;

  private final ConcurrentLinkedQueue<TimedMetricEvent> successQueue = new ConcurrentLinkedQueue<TimedMetricEvent>();
  private final ConcurrentLinkedQueue<TimedMetricEvent> errorQueue = new ConcurrentLinkedQueue<TimedMetricEvent>();

  private final ValueEventCollector successStats;
  private final ValueEventCollector errorStats;
  private ObjectName errorMBeanName;

  public TimedMetric(MetricName name, TimeUnit rateUnit, Clock clock) {

    Clock clockToUse = (clock == null) ? Clock.defaultClock() : clock;
    TimeUnit rateToUse = (rateUnit == null) ? TimeUnit.SECONDS : rateUnit;

    this.name = name;
    this.errorMBeanName = name.deriveWithNameSuffix(".error").getMBeanObjectName();
    this.clock = clockToUse;
    this.successStats = new ValueEventCollector(rateToUse, clockToUse);
    this.errorStats = new ValueEventCollector(rateToUse, clockToUse);
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
    while (!queue.isEmpty()) {
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
