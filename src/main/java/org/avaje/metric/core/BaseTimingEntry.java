package org.avaje.metric.core;

import org.avaje.metric.TimedMetric;
import org.avaje.metric.RequestTimingEntry;

/**
 * Base implementation of TimingEntry.
 */
public class BaseTimingEntry implements RequestTimingEntry {

  final TimedMetric metric;

  final int depth;

  final long startNanos;

  long endNanos;

  public BaseTimingEntry(int depth, TimedMetric metric, long startNanos) {
    this.depth = depth;
    this.metric = metric;
    this.startNanos = startNanos;
  }

  void setEndNanos(long endNanos) {
    this.endNanos = endNanos;
  }

  public String toString() {
    return "d:" + depth + " start:" + startNanos + " end:" + endNanos + " metric:" + metric.getName();
  }

  public int getDepth() {
    return depth;
  }

  public TimedMetric getMetric() {
    return metric;
  }

  public long getStartNanos() {
    return startNanos;
  }

  public long getEndNanos() {
    return endNanos;
  }
}
