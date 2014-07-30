package org.avaje.metric.core.noop;

import org.avaje.metric.ValueStatistics;

/**
 * A NOOP placeholder for ValueStatistics.
 */
public class NoopValueStatistics implements ValueStatistics {

  public static NoopValueStatistics INSTANCE = new NoopValueStatistics();
  
  @Override
  public long getStartTime() {
    return 0;
  }

  @Override
  public long getCount() {
    return 0;
  }

  @Override
  public long getTotal() {
    return 0;
  }

  @Override
  public long getMax() {
    return 0;
  }

  @Override
  public long getMean() {
    return 0;
  }
  
}
