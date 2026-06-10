/**
 * Convenience aggregator that combines {@code avaje-metrics-ebean} (the Ebean
 * metric source) and {@code ebean-insight} (the insight forwarder / query-plan
 * capture client) so a consumer can depend on a single artifact.
 *
 * <p>This module contains no code; it only re-exports the two modules so they
 * are readable transitively.
 */
module io.avaje.metrics.ebean.insight {

  requires transitive io.avaje.metrics;
  requires transitive io.avaje.metrics.ebean;
  requires transitive io.ebean.insight;
}
