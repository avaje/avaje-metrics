module io.avaje.metrics.otel.reporter {

  exports io.avaje.metrics.otel.reporter;

  requires io.avaje.applog;
  requires io.avaje.metrics;
  requires static io.opentelemetry.api;
}
