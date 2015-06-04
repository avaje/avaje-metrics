package org.avaje.metric.core;

import org.avaje.metric.TimedMetric;

/**
 * Created by rob on 4/06/15.
 */
public class NestedContextThreadLocal {

  private static ThreadLocal<NestedContext> local = new ThreadLocal<NestedContext>() {
    protected synchronized NestedContext initialValue() {
      return new NestedContext();
    }
  };

  /**
   * Not allowed.
   */
  private NestedContextThreadLocal() {
  }

  public static void start(TimedMetric metric) {
    local.get().start(metric);
  }

  public static void end() {
    local.get().end();
  }

  /**
   * Return the current TransactionState for a given serverName. This is for the
   * local thread of course.
   */
  public static NestedContext get() {
    return local.get();
  }

}
