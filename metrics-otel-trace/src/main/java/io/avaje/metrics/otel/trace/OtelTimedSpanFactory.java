package io.avaje.metrics.otel.trace;

import io.avaje.metrics.Metric;
import io.avaje.metrics.spi.SpiTimedSpanFactory;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.Span;
import org.jspecify.annotations.Nullable;

/**
 * OTEL span support for traced timers.
 */
public final class OtelTimedSpanFactory implements SpiTimedSpanFactory {

  static final String SCOPE_NAME = "io.avaje.metrics";
  static final String ATTR_METRIC_NAME = "avaje.metrics.name";
  static final String ATTR_BUCKET_RANGE = "avaje.metrics.bucket";

  @Override
  public @Nullable Prepared prepare(Metric.ID id, @Nullable String bucketRange) {
    return prepare(id, bucketRange, SpanMode.CHILD);
  }

  @Override
  public @Nullable Prepared prepare(Metric.ID id, @Nullable String bucketRange, SpanMode spanMode) {
    return new PreparedSpan(spanName(id), attributes(id, bucketRange), spanMode);
  }

  private String spanName(Metric.ID id) {
    for (String tag : id.tags().array()) {
      int colon = tag.indexOf(':');
      if (colon > 0 && "label".equals(tag.substring(0, colon))) {
        return tag.substring(colon + 1);
      }
    }
    return id.name();
  }

  private Attributes attributes(Metric.ID id, @Nullable String bucketRange) {
    AttributesBuilder builder = Attributes.builder();
    builder.put(ATTR_METRIC_NAME, id.name());
    appendTags(builder, id.tags().array());
    if (bucketRange != null) {
      builder.put(ATTR_BUCKET_RANGE, bucketRange);
    }
    return builder.build();
  }

  private void appendTags(AttributesBuilder builder, String[] tags) {
    for (String tag : tags) {
      int colon = tag.indexOf(':');
      if (colon > 0) {
        builder.put(tag.substring(0, colon), tag.substring(colon + 1));
      }
    }
  }

  private static final class PreparedSpan implements Prepared {

    private final String spanName;
    private final Attributes attributes;
    private final SpanMode spanMode;

    private PreparedSpan(String spanName, Attributes attributes, SpanMode spanMode) {
      this.spanName = spanName;
      this.attributes = attributes;
      this.spanMode = spanMode;
    }

    @Override
    public @Nullable OtelSpan start() {
      var currentSpan = Span.current();
      if (spanMode == SpanMode.CHILD && !currentSpan.isRecording()) {
        return null;
      }
      if (spanMode == SpanMode.ROOT && !currentSpan.isRecording() && currentSpan.getSpanContext().isValid()) {
        return null;
      }
      var span = GlobalOpenTelemetry.get()
        .getTracer(SCOPE_NAME)
        .spanBuilder(spanName)
        .setAllAttributes(attributes)
        .startSpan();
      return spanMode == SpanMode.ROOT ? new OtelSpan(span, span.makeCurrent()) : new OtelSpan(span);
    }
  }
}
