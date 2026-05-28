package io.avaje.metrics;

/**
 * Builder used to configure and register a timer.
 *
 * <p>Example:
 *
 * <pre>{@code
 * registry.timerBuilder("app.http.request")
 *   .tags(Tags.of("route:/customers"))
 *   .bucketRanges(50, 100, 250, 500)
 *   .buildTraced();
 * }</pre>
 */
public interface TimerBuilder {

  /**
   * Set the tags to use for the timer.
   */
  TimerBuilder tags(Tags tags);

  /**
   * Set the bucket ranges to use for the timer in milliseconds.
   */
  TimerBuilder bucketRanges(int... bucketRangesMillis);

  /**
   * Create and register the timer.
   */
  Timer build();

  /**
   * Create and register a child traced timer.
   * <p>
   * Child traced timers create spans only when there is an existing recording span.
   */
  Timer buildTraced();

  /**
   * Create and register a root traced timer.
   * <p>
   * Root traced timers create a root span when there is no current recording span. If there is a
   * current recording span, they create a child span. If there is a valid unsampled current span,
   * they do not create a new root span.
   */
  Timer buildRootTraced();
}
