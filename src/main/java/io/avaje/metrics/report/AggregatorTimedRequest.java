package io.avaje.metrics.report;

import io.avaje.metrics.MetricName;
import io.avaje.metrics.statistics.MetricStatistics;
import io.avaje.metrics.statistics.MetricStatisticsVisitor;
import io.avaje.metrics.statistics.TimedStatistics;

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

  void process(List<MetricStatistics> stats) {

    for (MetricStatistics stat : stats) {
      if (isMatch(stat)) {
        add((TimedStatistics) stat);
      }
    }

    if (aggTimedError != null) {
      stats.add(aggTimedError);
    }
    if (aggTimed != null) {
      stats.add(aggTimed);
    }
  }

  private void add(TimedStatistics stat) {
    if (stat.getName().endsWith(ERROR)) {
      if (aggTimedError == null) {
        aggTimedError = new AggTimed(name(ERROR), stat.getStartTime());
      }
      aggTimedError.add(stat);
    } else {
      if (aggTimed == null) {
        aggTimed = new AggTimed(name(""), stat.getStartTime());
      }
      aggTimed.add(stat);
    }
  }

  private MetricName name(String suffix) {
    return MetricName.of(name + suffix);
  }

  private boolean isMatch(MetricStatistics stat) {
    return (stat instanceof TimedStatistics) && stat.getName().startsWith(prefix);
  }

  static class AggTimed implements TimedStatistics {

    private final MetricName name;
    private final long startTime;

    AggTimed(MetricName name, long startTime) {
      this.name = name;
      this.startTime = startTime;
    }

    @Override
    public boolean isBucket() {
      return false;
    }

    @Override
    public String getBucketRange() {
      return null;
    }

    @Override
    public long getStartTime() {
      return startTime;
    }

    @Override
    public long getCount() {
      return count;
    }

    @Override
    public long getTotal() {
      return total;
    }

    @Override
    public long getMax() {
      return max;
    }

    @Override
    public long getMean() {
      return (count < 1) ? 0L : Math.round((double) (total / count));
    }

    @Override
    public String getName() {
      return name.getSimpleName();
    }

    @Override
    public void visit(MetricStatisticsVisitor visitor) {
      visitor.visit(this);
    }

    private long count;
    private long total;
    private long max;

    void add(TimedStatistics stat) {
      count += stat.getCount();
      total += stat.getTotal();
      max = Math.max(max, stat.getMax());
    }
  }
}
