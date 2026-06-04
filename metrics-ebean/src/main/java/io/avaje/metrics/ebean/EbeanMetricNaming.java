package io.avaje.metrics.ebean;

import io.avaje.metrics.Metric;
import io.avaje.metrics.Tags;

/**
 * Maps Ebean's internal flat metric names (e.g. {@code iud.BProcessLog.insertBatch},
 * {@code dto.SensorState}, {@code orm.Organisation.findById}, {@code txn.named.MapDailyMachine.run})
 * into avaje-metrics names with tags following the label-tag convention.
 *
 * <p>Mapping:
 * <table>
 *   <caption>Ebean prefix → avaje-metrics name + tags</caption>
 *   <tr><th>Ebean prefix</th><th>Metric name</th><th>Tags</th></tr>
 *   <tr><td>{@code iud.X}</td><td>{@code ebean.dml}</td><td>{@code label=X}</td></tr>
 *   <tr><td>{@code dto.X}</td><td>{@code ebean.query}</td><td>{@code type=dto, label=X}</td></tr>
 *   <tr><td>{@code orm.X}</td><td>{@code ebean.query}</td><td>{@code type=orm, label=X}</td></tr>
 *   <tr><td>{@code sql.X}</td><td>{@code ebean.query}</td><td>{@code type=sql, label=X}</td></tr>
 *   <tr><td>{@code txn.named.X} or {@code txn.X}</td><td>{@code ebean.txn}</td><td>{@code label=X}</td></tr>
 *   <tr><td>{@code l2.<region>.<op>}</td><td>{@code ebean.l2}</td><td>{@code op=..., region=...}</td></tr>
 *   <tr><td>(unrecognised)</td><td>{@code ebean.other}</td><td>{@code label=&lt;original name&gt;}</td></tr>
 * </table>
 */
final class EbeanMetricNaming {

  private EbeanMetricNaming() {
  }

  static Metric.ID toId(String ebeanName) {
    if (ebeanName == null || ebeanName.isEmpty()) {
      return Metric.ID.of("ebean.other");
    }
    int firstDot = ebeanName.indexOf('.');
    if (firstDot <= 0) {
      return Metric.ID.of("ebean.other", Tags.of("label:" + ebeanName));
    }
    var prefix = ebeanName.substring(0, firstDot);
    var rest = ebeanName.substring(firstDot + 1);
    switch (prefix) {
      case "iud":
        return Metric.ID.of("ebean.dml", Tags.of("label:" + rest));
      case "dto":
        return Metric.ID.of("ebean.query", Tags.of("type:dto", "label:" + rest));
      case "orm":
        return Metric.ID.of("ebean.query", Tags.of("type:orm", "label:" + rest));
      case "sql":
        return Metric.ID.of("ebean.query", Tags.of("type:sql", "label:" + rest));
      case "txn":
        var txnLabel = rest.startsWith("named.") ? rest.substring("named.".length()) : rest;
        return Metric.ID.of("ebean.txn", Tags.of("label:" + txnLabel));
      case "l2":
        return l2Id(rest);
      default:
        return Metric.ID.of("ebean.other", Tags.of("label:" + ebeanName));
    }
  }

  private static Metric.ID l2Id(String rest) {
    // Ebean L2 names look like "l2.<region>.<op>" or "l2.<op>".
    int dot = rest.indexOf('.');
    if (dot <= 0) {
      return Metric.ID.of("ebean.l2", Tags.of("op:" + rest));
    }
    var region = rest.substring(0, dot);
    var op = rest.substring(dot + 1);
    return Metric.ID.of("ebean.l2", Tags.of("op:" + op, "region:" + region));
  }
}
