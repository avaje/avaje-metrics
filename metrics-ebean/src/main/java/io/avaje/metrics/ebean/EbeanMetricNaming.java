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
 *   <tr><td>{@code dto.X}</td><td>{@code ebean.query}</td><td>{@code kind=dto, type=&lt;bean&gt;, label=X}</td></tr>
 *   <tr><td>{@code orm.X}</td><td>{@code ebean.query}</td><td>{@code kind=orm, type=&lt;bean&gt;, label=X}</td></tr>
 *   <tr><td>{@code sql.X}</td><td>{@code ebean.query}</td><td>{@code kind=sql, type=&lt;bean&gt;, label=X}</td></tr>
 *   <tr><td>{@code txn.named.X} or {@code txn.X}</td><td>{@code ebean.txn}</td><td>{@code label=X}</td></tr>
 *   <tr><td>{@code l2.<region>.<op>}</td><td>{@code ebean.l2}</td><td>{@code op=..., region=...}</td></tr>
 *   <tr><td>(unrecognised)</td><td>{@code ebean.other}</td><td>{@code label=&lt;original name&gt;}</td></tr>
 * </table>
 *
 * <p>For query metrics the {@code kind} tag is the query category (orm/dto/sql) while the
 * {@code type} tag is the queried bean/entity simple name (e.g. {@code Contact}). The bean
 * type is useful for secondary {@code _lazy}/{@code _query} loads whose name reflects the
 * parent/root query rather than the loaded entity. The {@code type} tag is omitted when the
 * bean type is unknown.
 */
final class EbeanMetricNaming {

  private EbeanMetricNaming() {
  }

  static Metric.ID toId(String ebeanName) {
    return toId(ebeanName, null);
  }

  static Metric.ID toId(String ebeanName, String beanType) {
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
        return queryId("dto", rest, beanType);
      case "orm":
        return queryId("orm", rest, beanType);
      case "sql":
        return queryId("sql", rest, beanType);
      case "txn":
        var txnLabel = rest.startsWith("named.") ? rest.substring("named.".length()) : rest;
        return Metric.ID.of("ebean.txn", Tags.of("label:" + txnLabel));
      case "l2":
        return l2Id(rest);
      default:
        return Metric.ID.of("ebean.other", Tags.of("label:" + ebeanName));
    }
  }

  private static Metric.ID queryId(String kind, String label, String beanType) {
    if (beanType == null || beanType.isEmpty()) {
      return Metric.ID.of("ebean.query", Tags.of("kind:" + kind, "label:" + label));
    }
    return Metric.ID.of("ebean.query", Tags.of("kind:" + kind, "type:" + beanType, "label:" + label));
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
