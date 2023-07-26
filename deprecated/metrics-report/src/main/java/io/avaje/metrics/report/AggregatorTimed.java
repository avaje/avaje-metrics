package io.avaje.metrics.report;

import io.avaje.metrics.Metric;

import java.util.List;

class AggregatorTimed implements MetricReportAggregator {

  private final String prefix;
  private final String name;

  AggregatorTimed(String prefix, String name) {
    this.prefix = prefix;
    this.name = name;
  }

  @Override
  public void process(List<Metric.Statistics> stats) {
    new AggregatorTimedRequest(prefix, name).process(stats);
  }
}
