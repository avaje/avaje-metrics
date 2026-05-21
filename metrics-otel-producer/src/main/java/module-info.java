module io.avaje.metrics.otel.producer {

  exports io.avaje.metrics.otel.producer;

  requires io.avaje.metrics;
  requires io.opentelemetry.api;
  requires io.opentelemetry.sdk.common;
  requires io.opentelemetry.sdk.metrics;
}
