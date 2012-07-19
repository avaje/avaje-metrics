package org.avaje.metric;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.avaje.metric.stats.ValueEventCollector;

/**
 * Measure events that occur with a long value. This long value could be bytes
 * or rows processed or time. Typically you would use a TimedMetricEvent for
 * time based events though.
 */
public final class ValueMetric implements Metric {

  private final MetricName name;

  private final Clock clock = Clock.defaultClock();

  private final ConcurrentLinkedQueue<ValueMetricEvent> queue = new ConcurrentLinkedQueue<ValueMetricEvent>();

  private final ValueEventCollector stats;

  public ValueMetric(MetricName name, TimeUnit rateUnit) {

    TimeUnit rateToUse = (rateUnit == null) ? TimeUnit.SECONDS : rateUnit;
    this.name = name;
    this.stats = new ValueEventCollector(rateToUse, clock);
  }

  public MetricStatistics getStatistics() {
    return stats;
  }

  @Override
  public void clearStatistics() {
    stats.clear();
  }

  public void updateStatistics() {

    List<ValueMetricEvent> successEvents = removeEvents(queue);
    stats.update(successEvents);
  }

  private List<ValueMetricEvent> removeEvents(ConcurrentLinkedQueue<ValueMetricEvent> queue) {

    ArrayList<ValueMetricEvent> events = new ArrayList<ValueMetricEvent>();
    while (!queue.isEmpty()) {
      ValueMetricEvent metricEvent = queue.remove();
      events.add(metricEvent);
    }
    return events;
  }

  public MetricName getName() {
    return name;
  }

  public void addEvent(long value) {
    queue.add(new ValueMetricEvent(clock.getTimeMillis(), value));
  }

  private static final class ValueMetricEvent implements MetricValueEvent {

    private final long timeMillis;
    private final long value;

    private ValueMetricEvent(long timeMillis, long value) {
      this.timeMillis = timeMillis;
      this.value = value;
    }

    @Override
    public long getEventTime() {
      return timeMillis;
    }

    @Override
    public long getValue() {
      return value;
    }

    public String toString() {
      return "value:" + getValue();
    }
  }
}
