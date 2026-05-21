module io.avaje.metrics.otel.otlp {

  exports io.avaje.metrics.otel.otlp;

  requires io.avaje.metrics;
  requires io.avaje.metrics.otel.producer;
  requires io.opentelemetry.api;
  requires io.opentelemetry.context;
  requires io.opentelemetry.exporter.otlp;
  requires io.opentelemetry.sdk;
  requires io.opentelemetry.sdk.common;
  requires io.opentelemetry.sdk.metrics;
  requires io.opentelemetry.sdk.trace;
}
