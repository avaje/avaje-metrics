package io.avaje.metrics.report;

import io.avaje.metrics.TimedMetric;
import io.avaje.metrics.MetricStats;
import io.avaje.metrics.MetricStatsVisitor;

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

  void process(List<MetricStats> stats) {

    for (MetricStats stat : stats) {
      if (isMatch(stat)) {
        add((TimedMetric.Stats) stat);
      }
    }

    if (aggTimedError != null) {
      stats.add(aggTimedError);
    }
    if (aggTimed != null) {
      stats.add(aggTimed);
    }
  }

  private void add(TimedMetric.Stats stat) {
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

  private boolean isMatch(MetricStats stat) {
    return (stat instanceof TimedMetric.Stats) && stat.name().startsWith(prefix);
  }

  static class AggTimed implements TimedMetric.Stats {

    private final String name;

    AggTimed(String name) {
      this.name = name;
    }

    @Override
    public boolean isBucket() {
      return false;
    }

    @Override
    public String bucketRange() {
      return null;
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
    public String name() {
      return name;
    }

    @Override
    public String nameWithBucket() {
      return name();
    }

    @Override
    public void visit(MetricStatsVisitor visitor) {
      visitor.visit(this);
    }

    private long count;
    private long total;
    private long max;

    void add(TimedMetric.Stats stat) {
      count += stat.count();
      total += stat.total();
      max = Math.max(max, stat.max());
    }
  }
}
