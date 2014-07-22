package org.avaje.metric.core;

import org.avaje.metric.Metric;
import org.avaje.metric.MetricName;
import org.avaje.metric.MetricVisitor;
import org.avaje.metric.ValueMetric;
import org.avaje.metric.ValueStatistics;



/**
 * Measure events that occur with a long value. This long value could be bytes
 * or rows processed or time. Typically you would use TimedMetric for time based
 * events though.
 */
public final class DefaultValueMetric implements Metric, ValueMetric {

  private final MetricName name;

  private final ValueCounter valueCounter = new ValueCounter();
  
  private ValueStatistics collectedStatistics;
  
  /**
   * Create with a name.
   */
  public DefaultValueMetric(MetricName name) {
    this.name = name;
  }


  @Override
  public boolean collectStatistics() {
    this.collectedStatistics = valueCounter.collectStatistics();
    return collectedStatistics != null;
  }


  @Override
  public ValueStatistics getCollectedStatistics() {
    return collectedStatistics;
  }

  @Override
  public void visit(MetricVisitor visitor) {

    visitor.visit(this);
  }

  @Override
  public void clearStatistics() {
    valueCounter.reset();
  }

  @Override
  public MetricName getName() {
    return name;
  }

  @Override
  public void addEvent(long value) {
    valueCounter.add(value);
  }

}
