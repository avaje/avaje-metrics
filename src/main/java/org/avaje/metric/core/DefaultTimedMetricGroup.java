package org.avaje.metric.core;

import org.avaje.metric.MetricManager;
import org.avaje.metric.MetricName;
import org.avaje.metric.MetricNameCache;
import org.avaje.metric.TimedEvent;
import org.avaje.metric.TimedMetric;
import org.avaje.metric.TimedMetricGroup;


/**
 * Used when many TimedMetric's share a common base name (group, type etc) and
 * only differ by their name.
 * <p>
 * For example a TimedMetricGroup might be used for a web service end point and
 * the TimedMetric's only differ by the operation name - the operation name is
 * specific and the rest (group, type etc) is common to all the metrics.
 * </p>
 */
public class DefaultTimedMetricGroup implements TimedMetricGroup {

  /**
   * The metric name cache.
   */
  private final MetricNameCache metricNameCache;

  /**
   * Create the TimedMetricGroup.
   */
  public DefaultTimedMetricGroup(MetricName baseName) {
    this.metricNameCache = MetricManager.getMetricNameCache(baseName);
  }

  /**
   * Start the event for the given name.
   * <p>
   * The group and type parts of the metric name are common and the metrics only
   * differ by this name.
   * </p>
   * 
   * @param name
   *          the specific name for the metric (group and type name parts are
   *          common).
   * 
   * @return the TimedMetricEvent that has started.
   */
  @Override
  public TimedEvent start(String name) {

    TimedMetric timedMetric = getTimedMetric(name);
    return timedMetric.startEvent();
  }

  /**
   * Return the TimedMetric for the specific name.
   */
  @Override
  public TimedMetric getTimedMetric(String name) {

    MetricName m = metricNameCache.get(name);
    return MetricManager.getTimedMetric(m);
  }

}
