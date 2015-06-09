package org.avaje.metric.core;

import org.avaje.metric.AbstractTimedMetric;
import org.avaje.metric.RequestTimingEntry;

/**
 * Base implementation of TimingEntry.
 */
public class BaseTimingEntry implements RequestTimingEntry {

  final AbstractTimedMetric metric;

  final int depth;

  final long startNanos;

  long endNanos;

  public BaseTimingEntry(int depth, AbstractTimedMetric metric, long startNanos) {
    this.depth = depth;
    this.metric = metric;
    this.startNanos = startNanos;
  }

  public long setEndNanos(long endNanos) {
    this.endNanos = endNanos;
    return endNanos - startNanos;
  }

  public String toString() {
    return "d:" + depth + " start:" + startNanos + " end:" + endNanos + " metric:" + metric.getName();
  }

  public int getDepth() {
    return depth;
  }

  public AbstractTimedMetric getMetric() {
    return metric;
  }

  public long getStartNanos() {
    return startNanos;
  }

  public long getEndNanos() {
    return endNanos;
  }

  @Override
  public long getExecutionNanos() {
    return endNanos - startNanos;
  }

  @Override
  public int compareTo(RequestTimingEntry other) {

    int compare = Long.compare(startNanos, other.getStartNanos());
    return compare != 0 ? compare : Integer.compare(depth, other.getDepth());
  }
}
