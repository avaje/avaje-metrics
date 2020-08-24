package io.avaje.metrics;

/**
 * A group of TimedMetric that share a common base name.
 * <p>
 * This is intended to be used when the full metric name is determined at runtime.
 */
public interface TimedMetricGroup {

  /**
   * Start the event for the given name.
   * <p>
   * The group and type parts of the metric name are common and the metrics only differ by this
   * name.
   * <p>
   * Typically the underlying implementation uses a cache to lookup the TimedMetric and create it if
   * necessary.
   *
   * @param name the specific name for the metric (group and type name parts are common).
   * @return the TimedMetricEvent that has started.
   */
  TimedEvent start(String name);

  /**
   * Return the TimedMetric for the specific name.
   */
  TimedMetric timed(String name);

  /**
   * Add an event based on a startNanos (determined by {@link System#nanoTime()}).
   * <p>
   * Success and failure statistics are kept separately.
   * <p>
   * This is an alternative to using {@link #start(String)}. Note that using startEvent() has
   * slightly higher overhead as it instantiates a TimedEvent object which must be later GC'ed. In
   * this sense generally addEventSince() is the preferred method to use.
   */
  void addEventSince(String name, boolean success, long startNanos);

  /**
   * Add an event duration in nanoseconds noting if it was a success or failure result.
   * <p>
   * Success and failure statistics are kept separately.
   * <p>
   * This is an alternative to using {@link #addEventSince(String, boolean, long)} where you pass in the
   * duration rather than the start nanoseconds.
   */
  void addEventDuration(String name, boolean success, long durationNanos);
}