package io.avaje.metrics.core;

import io.avaje.metrics.Metric;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

abstract class BaseReportName {

  final Metric.ID id;
  final String unit;
  volatile Metric.@Nullable ID reportId;

  BaseReportName(Metric.ID id) {
    this(id, "");
  }

  BaseReportName(Metric.ID id, String unit) {
    this.id = id;
    this.unit = normalizeUnit(unit);
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

  static String normalizeUnit(String unit) {
    var normalized = requireNonNull(unit, "unit");
    return normalized.isBlank() ? "" : normalized;
  }

}
