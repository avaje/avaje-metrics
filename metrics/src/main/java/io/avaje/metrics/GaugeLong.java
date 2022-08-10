package io.avaje.metrics;

import java.util.function.LongSupplier;

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
 * Note that <em>metrics</em> registers some core JVM gauges that include
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
  interface Stats extends Statistics {

    /**
     * Return the count of values collected.
     */
    long value();
  }

  /**
   * Return a LongSupplier for an always increasing supplier.
   * <p>
   * The value of the gauge will be the difference between each collected value.
   */
  static LongSupplier incrementing(LongSupplier supplier) {
    return new Incrementing(supplier);
  }
}
