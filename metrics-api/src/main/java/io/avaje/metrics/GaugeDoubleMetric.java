package io.avaje.metrics;

/**
 * Metric based on a gauge returning double values.
 * <p>
 * A GaugeDoubleMetric is created by {@link MetricManager#register(MetricName, GaugeDouble)}.
 *
 * <p>
 * Example:
 *
 * <pre>
 * <code>
 *   class FreeMemoryGauge implements GaugeDouble {
 *
 *       public double getValue() {
 *         return mxBean.getFreeMemory() / mxBean.getTotalMemory();
 *       }
 *     }
 *
 *
 *   GaugeDoubleMetric gauge = MetricManager.register("jvm.memory.pctfree", freeMemoryGauge);
 *
 * </code>
 * </pre>
 * <p>
 * Note that <em>metric-core</em> registers some core JVM gauges that include threads, memory
 * and garbage collection.
 */
public interface GaugeDoubleMetric extends Metric {

  /**
   * Return the value.
   */
  double getValue();

}
