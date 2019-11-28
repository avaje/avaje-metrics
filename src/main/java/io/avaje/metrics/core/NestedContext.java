package io.avaje.metrics.core;

import io.avaje.metrics.MetricManager;
import io.avaje.metrics.RequestTimingEntry;
import io.avaje.metrics.TimedMetric;
import io.avaje.metrics.util.ArrayStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Uses a ThreadLocal scope holding a 'context/stack' of all the timing metrics occurring for a single request.
 * <p>
 * There is a system property <code>metric.context.threshold.micros</code> that can be used to apply a threshold
 * so that timing metrics that execute below that threshold are excluded (in order to reduce 'noise').
 * </p>
 */
final class NestedContext {

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
      logger.error("Invalid number value [" + threshold + "] for metric.context.threshold.micros", e);
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
  static boolean pushIfActive(TimedMetric metric) {
    return local.get().pushMetricIfActive(metric);
  }

  /**
   * Push a suppler of timed metric.
   */
  static boolean pushIfActive(Supplier<TimedMetric> supplier) {
    return local.get().pushMetricIfActive(supplier);
  }

  /**
   * Add the TimedMetric to the nested context.
   * <p>
   * If the nested context is not active it will become so.
   * </p>
   */
  static void push(TimedMetric metric) {
    local.get().pushMetric(metric);
  }

  /**
   * Pop the last TimedMetric off the nested context.
   */
  static void pop() {
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
   * The supplier allows lazy evaluation of the metric.
   */
  boolean pushMetricIfActive(Supplier<TimedMetric> supplier) {
    if (depth < 1) {
      return false;
    }
    TimedMetric metric = supplier.get();
    if (metric == null) {
      return false;
    } else {
      stack.push(new BaseTimingEntry(depth++, metric, System.nanoTime()));
      return true;
    }
  }

  /**
   * Return true if this timing metric should be added to the nested context.
   * This should only occur if the nested context is already active - which means
   * some top level TimedMetric has already turned it on.
   */
  boolean pushMetricIfActive(TimedMetric metric) {
    if (depth < 1) {
      return false;
    }
    stack.push(new BaseTimingEntry(depth++, metric, System.nanoTime()));
    return true;
  }

  /**
   * Add the metric. Used when a TimedMetric has it's requestTiming explicitly set on.
   */
  void pushMetric(TimedMetric metric) {
    stack.push(new BaseTimingEntry(depth++, metric, System.nanoTime()));
  }

  /**
   * Pop the last entry and add it to the entry list.
   * <p>
   * If the stack is empty then the entry list is reported.
   */
  void popMetric() {
    if (--depth < 0) {
      logger.error("Unexpected depth [" + depth + "] when popping metric");
      resetContext();

    } else {
      BaseTimingEntry pop = stack.pop();
      if (pop.setEndNanos(System.nanoTime()) > thresholdNanos) {
        entries.add(pop);
      }
      // (if required) decrement the collection count on the metric
      pop.getMetric().decrementCollectionCount();
      if (stack.isEmpty()) {
        report(entries);
        entries = new ArrayList<>();
      }
    }
  }

  /**
   * Report the list of entries to the MetricManager.
   */
  static void report(List<RequestTimingEntry> entries) {
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
