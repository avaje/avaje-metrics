package io.avaje.metrics;

/**
 * Metric based on an underlying gauge that reports long values.
 * <p>
 * A GaugeLongMetric is created by {@link Metrics#register(MetricName, GaugeLong)}.
 *
 * <p>
 * Example:
 *
 * <pre>
 * <code>
 *   class ThreadCountGauge implements GaugeLong {
 *
 *       public long getValue() {
 *         return threadMXBean.getThreadCount();
 *       }
 *     }
 *
 *
 *   GaugeLongMetric gauge = MetricManager.register("jvm.thread.count", threadCountGauge);
 *
 * </code>
 * </pre>
 * <p>
 * Note that <em>metric-core</em> registers some core JVM gauges that include
 * threads, memory and garbage collection.
 */
public interface GaugeLong extends Metric {

  /**
   * Return the value.
   */
  long value();

  /**
   * Statistics provided by the {@link GaugeLong}.
   */
  interface Stats extends MetricStats {

    /**
     * Return the count of values collected.
     */
    long value();
  }
}
