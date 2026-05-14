package io.avaje.metrics.spi;

import io.avaje.metrics.Metric;
import org.jspecify.annotations.Nullable;

/**
 * SPI for preparing span creation for traced timers.
 */
public interface SpiTimedSpanFactory {

  /**
   * Prepare a span starter for the given timer.
   *
   * @param id the timer metric id
   * @param bucketRange the bucket range when the timer represents a bucketed series
   */
  @Nullable Prepared prepare(Metric.ID id, @Nullable String bucketRange);

  /**
   * A prepared span starter for a specific metric id and bucket range.
   */
  interface Prepared {

    /**
     * Start a new span for an event.
     */
    @Nullable SpiSpan start();
  }
}
