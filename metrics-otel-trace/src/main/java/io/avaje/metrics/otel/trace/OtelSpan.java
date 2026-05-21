package io.avaje.metrics.otel.trace;

import io.avaje.metrics.spi.SpiSpan;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;

final class OtelSpan implements SpiSpan {

  private final Span span;

  OtelSpan(Span span) {
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

  @Override
  public void endWithError(Throwable error) {
    span.recordException(error);
    endWithError();
  }
}
