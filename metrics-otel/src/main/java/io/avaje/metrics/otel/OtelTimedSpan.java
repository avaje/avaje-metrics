package io.avaje.metrics.otel;

import io.avaje.metrics.spi.SpiTimedSpan;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;

final class OtelTimedSpan implements SpiTimedSpan {

  private final Span span;

  OtelTimedSpan(Span span) {
    this.span = span;
  }

  @Override
  public void end() {
    span.end();
  }

  @Override
  public void endWithError() {
    span.setStatus(StatusCode.ERROR);
    span.end();
  }
}
