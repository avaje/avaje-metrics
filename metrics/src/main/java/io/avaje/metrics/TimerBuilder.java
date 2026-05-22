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
   * Create and register a traced timer.
   */
  Timer buildTraced();
}
