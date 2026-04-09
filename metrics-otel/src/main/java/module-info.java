module io.avaje.metrics.otel {

  exports io.avaje.metrics.otel;

  requires io.avaje.applog;
  requires io.avaje.metrics;
  requires static io.opentelemetry.api;
}
