package org.avaje.metric;

import org.avaje.metric.report.MetricVisitor;


/**
 * Measure events that occur with a long value. This long value could be bytes
 * or rows processed or time. Typically you would use TimedMetric for time based
 * events though.
 */
public final class ValueMetric implements Metric {

  private final MetricName name;

  private final ValueCounter valueCounter = new ValueCounter(true);
  
  private ValueStatistics collectedStatistics;
  
  /**
   * Create with a name.
   */
  public ValueMetric(MetricName name) {
    this.name = name;
  }

  @Override
  public boolean collectStatistics() {
    this.collectedStatistics = valueCounter.collectStatistics();
    return collectedStatistics != null;
  }

  public ValueStatistics getCollectedStatistics() {
    return collectedStatistics;
  }
  
  @Override
  public void visitCollectedStatistics(MetricVisitor visitor) {

    visitor.visit(this);
  }

  @Override
  public void clearStatistics() {
    valueCounter.reset();
  }

  public MetricName getName() {
    return name;
  }

  /**
   * Add a value (bytes, time, rows etc).
   */
  public void addEvent(long value) {
    valueCounter.add(value);
  }

}
