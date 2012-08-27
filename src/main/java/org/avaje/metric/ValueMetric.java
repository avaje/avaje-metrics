package org.avaje.metric;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.avaje.metric.stats.CollectValueEvents;

/**
 * Measure events that occur with a long value. This long value could be bytes
 * or rows processed or time. Typically you would use a TimedMetricEvent for
 * time based events though.
 */
public final class ValueMetric implements Metric {

  private final MetricName name;

  private final Clock clock = Clock.defaultClock();

  private final ConcurrentLinkedQueue<ValueMetricEvent> queue = new ConcurrentLinkedQueue<ValueMetricEvent>();

  private final CollectValueEvents stats;

  /**
   * Create with a name and rateUnit.
   * <p>
   * The rateUnit should be chosen to 'scale' the statistics in a reasonable
   * manor - typically events per hour, minute or second.
   * </p>
   */
  public ValueMetric(MetricName name) {
    this.name = name;
    this.stats = new CollectValueEvents(clock);
  }

  /**
   * Return all the statistics collected.
   */
  public MetricStatistics getStatistics() {
    return stats;
  }

  @Override
  public void visit(MetricVisitor visitor) {
    
    boolean empty = stats.isEmpty();
    if (!visitor.visitBegin(this, empty)) {
      // skip processing/reporting for empty metric
      if (empty) {
        // reset effectively moving the start time
        stats.reset();
      }
    } else {
      visitor.visit(stats.getValueStatistics(visitor.isResetStatistics()));   
      visitor.visitEnd(this);
    }
  }
  
  @Override
  public void clearStatistics() {
    stats.reset();
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
