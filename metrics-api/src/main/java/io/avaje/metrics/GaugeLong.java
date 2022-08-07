package io.avaje.metrics;

/**
 * Metric based on an underlying gauge that reports long values.
 *
 * <p>
 * Example:
 *
 * <pre>
 * <code>
 *   class ThreadCountGauge implements LongSupplier {
 *
 *       public long getAsLong() {
 *         return threadMXBean.getThreadCount();
 *       }
 *     }
 *
 *
 *   GaugeLong gauge = Metrics.gauge("jvm.thread.count", threadCountGauge);
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
