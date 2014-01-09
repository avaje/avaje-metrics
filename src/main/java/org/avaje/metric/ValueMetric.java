package org.avaje.metric;


/**
 * Measure events that occur with a long value. This long value could be bytes
 * or rows processed or time. Typically you would use TimedMetric for time based
 * events though.
 */
public final class ValueMetric implements Metric {

  private final MetricName name;

  private final ValueCounter valueCounter = new ValueCounter(true);
  
  /**
   * Create with a name and rateUnit.
   * <p>
   * The rateUnit should be chosen to 'scale' the statistics in a reasonable
   * manor - typically events per hour, minute or second.
   * </p>
   */
  public ValueMetric(MetricName name) {
    this.name = name;
  }

  @Override
  public void visit(MetricVisitor visitor) {

    boolean empty = valueCounter.isEmpty();
    if (!visitor.visitBegin(this, empty)) {
      // skip processing/reporting for empty metric
      if (empty) {
        // reset effectively moving the start time
        valueCounter.reset();
      }
    } else {
      visitor.visit(this, valueCounter.getStatistics(visitor.isResetStatistics()));
      visitor.visitEnd(this);
    }
  }

  @Override
  public void clearStatistics() {
    valueCounter.reset();
  }

  public MetricName getName() {
    return name;
  }

  public void addEvent(long value) {
    valueCounter.add(value);
  }

  public void addEvent(MetricValueEvent event) {
    addEvent(event.getEventTime());
  }

}
