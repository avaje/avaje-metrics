package io.avaje.metrics.spi;

import io.avaje.metrics.Metric;
import org.jspecify.annotations.Nullable;

/**
 * SPI for preparing span creation for traced timers.
 */
public interface SpiTimedSpanFactory {

  /**
   * The span creation mode requested by a traced timer.
   */
  enum SpanMode {
    /**
     * Create spans only under an existing recording span.
     */
    CHILD,
    /**
     * Create a root span when no recording span exists.
     */
    ROOT
  }

  /**
   * Prepare a span starter for the given timer.
   *
   * @param id the timer metric id
   * @param bucketRange the bucket range when the timer represents a bucketed series
   */
  @Nullable Prepared prepare(Metric.ID id, @Nullable String bucketRange);

  /**
   * Prepare a span starter for the given timer and span mode.
   *
   * @param id the timer metric id
   * @param bucketRange the bucket range when the timer represents a bucketed series
   * @param spanMode the requested span creation mode
   */
  default @Nullable Prepared prepare(Metric.ID id, @Nullable String bucketRange, SpanMode spanMode) {
    return prepare(id, bucketRange);
  }

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
