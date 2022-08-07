package io.avaje.metrics.core;

import io.avaje.metrics.MetricRegistry;
import io.avaje.metrics.Timer;
import io.avaje.metrics.TimerGroup;

import java.util.concurrent.ConcurrentHashMap;


/**
 * Used when many TimedMetrics share a common base name (group, type etc) and
 * only differ by their name.
 * <p>
 * For example a TimedMetricGroup might be used for a web service end point and
 * the TimedMetrics only differ by the operation name - the operation name is
 * specific and the rest (group, type etc) is common to all the metrics.
 */
final class DTimerGroup implements TimerGroup {

  private final ConcurrentHashMap<String, Timer> cache = new ConcurrentHashMap<>();
  private final DMetricNameCache metricNameCache;
  private final MetricRegistry registry;

  DTimerGroup(String baseName, MetricRegistry registry) {
    this.metricNameCache = new DMetricNameCache(baseName);
    this.registry = registry;
  }

  /**
   * Start the event for the given name.
   * <p>
   * The group and type parts of the metric name are common and the metrics only
   * differ by this name.
   *
   * @param name the specific name for the metric (group and type name parts are
   *             common).
   * @return the TimedMetricEvent that has started.
   */
  @Override
  public Timer.Event start(String name) {
    Timer timedMetric = timed(name);
    return timedMetric.startEvent();
  }


  @Override
  public void addEventSince(String name, boolean success, long startNanos) {
    long durationNanos = System.nanoTime() - startNanos;
    addEventDuration(name, success, durationNanos);
  }

  @Override
  public void addEventDuration(String name, boolean success, long durationNanos) {
    Timer timedMetric = timed(name);
    timedMetric.addEventDuration(success, durationNanos);
  }

  /**
   * Return the TimedMetric for the specific name.
   */
  @Override
  public Timer timed(String name) {
    // try local cache first to try and avoid the name parse
    Timer found = cache.get(name);
    if (found != null) {
      return found;
    }
    // parse name and find/create using MetricManager
    String metricName = metricNameCache.get(name);
    // this is safe in that it is single threaded on construction/put
    Timer timedMetric = registry.timed(metricName);
    final Timer existing = cache.putIfAbsent(name, timedMetric);
    return (existing != null) ? existing : timedMetric;
  }

}
