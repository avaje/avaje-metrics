package io.avaje.metrics.otel.trace;

import io.avaje.metrics.spi.SpiSpan;
import io.opentelemetry.context.Scope;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import org.jspecify.annotations.Nullable;

final class OtelSpan implements SpiSpan {

  private final Span span;
  private final @Nullable Scope scope;

  OtelSpan(Span span) {
    this(span, null);
  }

  OtelSpan(Span span, @Nullable Scope scope) {
    this.span = span;
    this.scope = scope;
  }

  @Override
  public void end() {
    closeScope();
    span.end();
  }

  @Override
  public void endWithError() {
    span.setStatus(StatusCode.ERROR);
    closeScope();
    span.end();
  }

  @Override
  public void endWithError(Throwable error) {
    span.recordException(error);
    endWithError();
  }

  private void closeScope() {
    if (scope != null) {
      scope.close();
    }
  }
}
