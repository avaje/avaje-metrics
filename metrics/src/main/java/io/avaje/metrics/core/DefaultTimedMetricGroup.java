package io.avaje.metrics.core;

import io.avaje.metrics.*;

import java.util.concurrent.ConcurrentHashMap;


/**
 * Used when many TimedMetric's share a common base name (group, type etc) and
 * only differ by their name.
 * <p>
 * For example a TimedMetricGroup might be used for a web service end point and
 * the TimedMetric's only differ by the operation name - the operation name is
 * specific and the rest (group, type etc) is common to all the metrics.
 * </p>
 */
final class DefaultTimedMetricGroup implements TimedMetricGroup {

  private final ConcurrentHashMap<String, TimedMetric> cache = new ConcurrentHashMap<String, TimedMetric>();

  /**
   * The metric name cache.
   */
  private final MetricNameCache metricNameCache;

  /**
   * Create the TimedMetricGroup.
   */
  DefaultTimedMetricGroup(MetricName baseName) {
    this.metricNameCache = MetricManager.nameCache(baseName);
  }

  /**
   * Start the event for the given name.
   * <p>
   * The group and type parts of the metric name are common and the metrics only
   * differ by this name.
   * </p>
   *
   * @param name the specific name for the metric (group and type name parts are
   *             common).
   * @return the TimedMetricEvent that has started.
   */
  @Override
  public TimedEvent start(String name) {
    TimedMetric timedMetric = timed(name);
    return timedMetric.startEvent();
  }


  @Override
  public void addEventSince(String name, boolean success, long startNanos) {
    long durationNanos = System.nanoTime() - startNanos;
    addEventDuration(name, success, durationNanos);
  }

  @Override
  public void addEventDuration(String name, boolean success, long durationNanos) {
    TimedMetric timedMetric = timed(name);
    timedMetric.addEventDuration(success, durationNanos);
  }

  /**
   * Return the TimedMetric for the specific name.
   */
  @Override
  public TimedMetric timed(String name) {
    // try local cache first to try and avoid the name parse
    TimedMetric found = cache.get(name);
    if (found != null) {
      return found;
    }
    // parse name and find/create using MetricManager
    MetricName metricName = metricNameCache.get(name);
    // this is safe in that it is single threaded on construction/put
    TimedMetric timedMetric = MetricManager.timed(metricName);
    final TimedMetric existing = cache.putIfAbsent(name, timedMetric);
    return (existing != null) ? existing : timedMetric;
  }

}
