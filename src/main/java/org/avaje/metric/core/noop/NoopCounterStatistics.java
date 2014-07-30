package org.avaje.metric.core.noop;

import org.avaje.metric.CounterStatistics;

public final class NoopCounterStatistics implements CounterStatistics {

  public static final NoopCounterStatistics INSTANCE = new NoopCounterStatistics();
  
  @Override
  public long getStartTime() {
    return 0;
  }

  @Override
  public long getCount() {
    return 0;
  }

  
}
