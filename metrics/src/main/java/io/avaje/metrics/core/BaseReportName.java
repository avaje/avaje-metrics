package io.avaje.metrics.core;

import io.avaje.metrics.Metric;
import org.jspecify.annotations.Nullable;

abstract class BaseReportName {

  final Metric.ID id;
  volatile Metric.@Nullable ID reportId;

  BaseReportName(Metric.ID id) {
    this.id = id;
  }

  final Metric.ID reportId(Metric.Visitor collector) {
    final var id = reportId;
    return id != null ? id : useNamingConvention(collector);
  }

  final Metric.ID useNamingConvention(Metric.Visitor collector) {
    final Metric.ID tmp = id.withName(collector.namingConvention().apply(id.name()));
    this.reportId = tmp;
    return tmp;
  }

}
