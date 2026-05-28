module io.avaje.metrics.otel.trace {

  exports io.avaje.metrics.otel.trace;

  requires io.avaje.metrics;
  requires static io.opentelemetry.api;
  requires static io.opentelemetry.context;

  provides io.avaje.metrics.spi.SpiTimedSpanFactory with io.avaje.metrics.otel.trace.OtelTimedSpanFactory;
}
