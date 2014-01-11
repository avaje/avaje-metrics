package org.avaje.metric.report;

import org.avaje.metric.CounterMetric;
import org.avaje.metric.GaugeMetric;
import org.avaje.metric.GaugeMetricGroup;
import org.avaje.metric.TimedMetric;
import org.avaje.metric.ValueMetric;


/**
 * Visitor for the statistics held by a metric.
 */
public interface MetricVisitor {

  /**
   * Visit a TimedMetric.
   */
  public void visit(TimedMetric metric);

  /**
   * Visit a ValueMetric.
   */
  public void visit(ValueMetric metric);
  
  /**
   * Visit a CounterMetric.
   */
  public void visit(CounterMetric metric);

  /**
   * Visit a GaugeMetricGroup.
   */
  public void visit(GaugeMetricGroup gaugeMetricGroup);

  /**
   * Visit an individual GaugeMetric.
   */
  public void visit(GaugeMetric gaugeMetric);

}
