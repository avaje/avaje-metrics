package org.avaje.metric.core;

import org.avaje.metric.MetricManager;
import org.avaje.metric.TimedMetric;
import org.avaje.metric.RequestTimingEntry;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
class NestedContext {

  final ArrayStack<BaseTimingEntry> stack = new ArrayStack<>();

  int depth;

  List<RequestTimingEntry> entries = new ArrayList<>();

  public NestedContext() {

  }

  public boolean add(TimedMetric metric) {
    if (depth < 1) {
      return false;
    }
    stack.push(new BaseTimingEntry(depth++, metric, System.nanoTime()));
    return true;
  }

  public void start(TimedMetric metric) {
    stack.push(new BaseTimingEntry(depth++, metric, System.nanoTime()));
  }

  public void end() {
    --depth;
    BaseTimingEntry pop = stack.pop();
    pop.setEndNanos(System.nanoTime());
    entries.add(pop);
    if (stack.isEmpty()) {
      report(entries);
      entries = new ArrayList<>();
    }
  }

  protected void report(List<RequestTimingEntry> entries) {
    MetricManager.reportTiming(entries);
  }


}
