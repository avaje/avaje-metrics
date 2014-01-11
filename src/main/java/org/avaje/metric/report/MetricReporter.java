package org.avaje.metric.report;

import java.util.List;

import org.avaje.metric.Metric;

public interface MetricReporter {

  public void report(List<Metric> metrics);
  
  public void cleanup();
}
