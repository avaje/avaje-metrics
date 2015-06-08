package org.avaje.metric.core;

import org.avaje.metric.AbstractTimedMetric;
import org.avaje.metric.MetricManager;
import org.avaje.metric.RequestTimingEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Uses a ThreadLocal scope holding a 'context/stack' of all the timing metrics occurring for a single request.
 * <p>
 *   There is a system property <code>metric.context.threshold.micros</code> that can be used to apply a threshold
 *   so that timing metrics that execute below that threshold are excluded (in order to reduce 'noise').
 * </p>
 */
public final class NestedContext {

  private static Logger logger = LoggerFactory.getLogger(NestedContext.class);

  private static final long thresholdNanos = 1000 * getThresholdMicros();

  /**
   * Return the threshold to apply in microseconds.
   */
  private static long getThresholdMicros() {
    String threshold = System.getProperty("metric.context.threshold.micros", "0");
    try {
      return Long.parseLong(threshold);
    } catch (NumberFormatException e) {
      logger.error("Invalid number value ["+threshold+"] for metric.context.threshold.micros", e);
      return 0;
    }
  }

  private static ThreadLocal<NestedContext> local = new ThreadLocal<NestedContext>() {
    protected synchronized NestedContext initialValue() {
      return new NestedContext();
    }
  };

  /**
   * Add the TimedMetric to the nested context if the context is already active.
   * Returns true if the metric was added and otherwise false.
   *
   * @return true if the metric was added to the nested context implying the context was already active.
   */
  public static boolean pushIfActive(AbstractTimedMetric metric) {
    return local.get().pushMetricIfActive(metric);
  }

  /**
   * Add the TimedMetric to the nested context.
   * <p>
   * If the nested context is not active it will become so.
   * </p>
   */
  public static void push(AbstractTimedMetric metric) {
    local.get().pushMetric(metric);
  }

  /**
   * Pop the last TimedMetric off the nested context.
   */
  public static void pop() {
    local.get().popMetric();
  }

  /**
   * Reset the nested context.
   */
  public static void reset() {
    local.get().resetContext();
  }


  /**
   * Stack that the timing entries go onto.
   */
  final ArrayStack<BaseTimingEntry> stack = new ArrayStack<>();

  /**
   * The current depth.
   */
  int depth;

  /**
   * The entries collected off the stack when they end.
   */
  List<RequestTimingEntry> entries = new ArrayList<>();

  NestedContext() {
  }

  /**
   * Return true if this timing metric should be added to the nested context.
   * This should only occur if the nested context is already active - which means
   * some top level TimedMetric has already turned it on.
   */
  boolean pushMetricIfActive(AbstractTimedMetric metric) {
    if (depth < 1) {
      return false;
    }
    stack.push(new BaseTimingEntry(depth++, metric, System.nanoTime()));
    return true;
  }

  /**
   * Add the metric. Used when a TimedMetric has it's requestTiming explicitly set on.
   */
  void pushMetric(AbstractTimedMetric metric) {
    stack.push(new BaseTimingEntry(depth++, metric, System.nanoTime()));
  }

  /**
   * Pop the last entry and add it to the entry list.
   *
   * If the stack is empty then the entry list is reported.
   */
  void popMetric() {
    if (--depth < 0) {
      logger.error("Unexpected depth ["+depth+"] when popping metric");
      resetContext();

    } else {
      BaseTimingEntry pop = stack.pop();
      if (pop.setEndNanos(System.nanoTime()) > thresholdNanos) {
        entries.add(pop);
      }
      if (stack.isEmpty()) {
        pop.getMetric().decrementCollectionCount();
        report(entries);
        entries = new ArrayList<>();
      }
    }
  }

  /**
   * Report the list of entries to the MetricManager.
   */
  void report(List<RequestTimingEntry> entries) {
    MetricManager.reportTiming(new DefaultRequestTiming(entries, System.currentTimeMillis()));
  }

  /**
   * Reset the nested context.
   */
  void resetContext() {
    stack.clear();
    depth = 0;
    entries = new ArrayList<>();
  }
}
