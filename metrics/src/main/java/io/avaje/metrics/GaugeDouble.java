package io.avaje.metrics;

/**
 * Metric based on a gauge returning double values.
 *
 * <p>
 * Example:
 *
 * <pre>
 * <code>
 *   class FreeMemoryGauge implements DoubleSupplier {
 *
 *       public double getAsDouble() {
 *         return mxBean.getFreeMemory() / mxBean.getTotalMemory();
 *       }
 *     }
 *
 *
 *   GaugeDouble gauge = Metrics.gauge("jvm.memory.pctfree", freeMemoryGauge);
 *
 * </code>
 * </pre>
 * <p>
 * Note that <em>metric-core</em> registers some core JVM gauges that include threads, memory
 * and garbage collection.
 */
public interface GaugeDouble extends Metric {

  /**
   * Return the value.
   */
  double value();

  /**
   * Statistics provided by the {@link GaugeDouble}.
   */
  interface Stats extends MetricStats {

    /**
     * Return the count of values collected.
     */
    double value();
  }
}
