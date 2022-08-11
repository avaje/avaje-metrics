package io.avaje.metrics.core;

import io.avaje.metrics.Metric;

abstract class BaseReportName {

  final String name;
  String reportName;

  BaseReportName(String name) {
    this.name = name;
  }

  final String reportName(Metric.Visitor collector) {
    final String tmp = collector.namingConvention().apply(name);
    this.reportName = tmp;
    return tmp;
  }

}
