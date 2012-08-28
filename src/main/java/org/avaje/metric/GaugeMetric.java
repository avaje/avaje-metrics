package org.avaje.metric;

/**
 * A Metric that gets its value from a Gauge.
 * <p>
 * GaugeMetric can be put into groups via {@link GaugeMetricGroup}.
 * </p>
 */
public class GaugeMetric implements Metric {

  protected final MetricName name;

  protected final Gauge gauge;

  protected final boolean wholeNumber;

  /**
   * Create where the Gauge is a monotonically increasing value.
   * <p>
   * This will determine the delta increase in underlying value and return that
   * for the value.
   * </p>
   */
  public static GaugeMetric incrementing(MetricName name, Gauge gauge, boolean wholeNumber) {
    return new Incrementing(name, gauge, wholeNumber);
  }

  /**
   * Create a GaugeMetric.
   * 
   * @param name
   *          the name of the metric.
   * @param gauge
   *          the gauge used to get the value.
   * @param wholeNumber
   *          set this to true if the underlying value is a long or int.
   */
  public GaugeMetric(MetricName name, Gauge gauge, boolean wholeNumber) {
    this.name = name;
    this.gauge = gauge;
    this.wholeNumber = wholeNumber;
  }

  @Override
  public MetricName getName() {
    return name;
  }

  public String toString() {
    return name + " " + getValue();
  }

  /**
   * Return the value.
   */
  public double getValue() {
    return gauge.getValue();
  }

  /**
   * Return the value formatted to a String.
   * <p>
   * If the value is a whole number it will be returned with no decimal places.
   * </p>
   */
  public String getFormattedValue(int decimalPlaces) {
    if (wholeNumber) {
      return NumFormat.dp(0, getValue());
    } else {
      return NumFormat.dp(decimalPlaces, getValue());
    }
  }

  @Override
  public void visit(MetricVisitor visitor) {
     boolean empty = gauge.getValue() == 0;
     if (!visitor.visitBegin(this, empty)) {
       // skip processing/reporting for empty metric
     } else {
       visitor.visit(this);
       visitor.visitEnd(this);
     }
  }

  @Override
  public void clearStatistics() {
    // No need to do anything - direct to gauge
  }

  @Override
  public void updateStatistics() {
    // No need to do anything - direct to gauge
  }

  /**
   * Supports monotonically increasing gauges.
   */
  private static class Incrementing extends GaugeMetric {

    private double runningValue;

    Incrementing(MetricName name, Gauge gauge, boolean wholeNumber) {
      super(name, gauge, wholeNumber);
    }

    @Override
    public double getValue() {

      synchronized (this) {

        double nowValue = super.getValue();
        double diffValue = nowValue - runningValue;
        runningValue = nowValue;
        return diffValue;
      }
    }

  }

}
