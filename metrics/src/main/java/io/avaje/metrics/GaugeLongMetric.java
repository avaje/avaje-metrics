package io.avaje.metrics;

/**
 * Metric based on an underlying gauge that reports long values.
 * <p>
 * A GaugeLongMetric is created by {@link MetricManager#register(MetricName, GaugeLong)}.
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
public interface GaugeLongMetric extends Metric {

  /**
   * Return the value.
   */
  long getValue();

}
