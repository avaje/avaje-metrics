package io.avaje.metrics.report;

import io.avaje.metrics.Metric;
import io.avaje.metrics.Timer;

import java.util.List;

class AggregatorTimedRequest {

  private static final String ERROR = ".error";

  private final String prefix;

  private final String name;

  private AggTimed aggTimed;

  private AggTimed aggTimedError;

  AggregatorTimedRequest(String prefix, String name) {
    this.prefix = prefix;
    this.name = name;
  }

  void process(List<Metric.Statistics> stats) {

    for (Metric.Statistics stat : stats) {
      if (isMatch(stat)) {
        add((Timer.Stats) stat);
      }
    }

    if (aggTimedError != null) {
      stats.add(aggTimedError);
    }
    if (aggTimed != null) {
      stats.add(aggTimed);
    }
  }

  private void add(Timer.Stats stat) {
    if (stat.name().endsWith(ERROR)) {
      if (aggTimedError == null) {
        aggTimedError = new AggTimed(name(ERROR));
      }
      aggTimedError.add(stat);
    } else {
      if (aggTimed == null) {
        aggTimed = new AggTimed(name(""));
      }
      aggTimed.add(stat);
    }
  }

  private String name(String suffix) {
    return name + suffix;
  }

  private boolean isMatch(Metric.Statistics stat) {
    return (stat instanceof Timer.Stats) && stat.name().startsWith(prefix);
  }

  static class AggTimed implements Timer.Stats {

    private final String name;

    AggTimed(String name) {
      this.name = name;
    }

    @Override
    public String name() {
      return name;
    }

    @Override
    public long count() {
      return count;
    }

    @Override
    public long total() {
      return total;
    }

    @Override
    public long max() {
      return max;
    }

    @Override
    public long mean() {
      return (count < 1) ? 0L : Math.round((double) (total / count));
    }

    @Override
    public void visit(Metric.Visitor visitor) {
      visitor.visit(this);
    }

    private long count;
    private long total;
    private long max;

    void add(Timer.Stats stat) {
      count += stat.count();
      total += stat.total();
      max = Math.max(max, stat.max());
    }
  }
}
