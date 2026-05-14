package io.avaje.metrics.otel;

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
    return new PreparedSpan(id.name(), attributes(id, bucketRange));
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

    private PreparedSpan(String spanName, Attributes attributes) {
      this.spanName = spanName;
      this.attributes = attributes;
    }

    @Override
    public @Nullable OtelSpan start() {
      if (!Span.current().isRecording()) {
        return null;
      }
      var span = GlobalOpenTelemetry.get()
        .getTracer(SCOPE_NAME)
        .spanBuilder(spanName)
        .setAllAttributes(attributes)
        .startSpan();
      return new OtelSpan(span);
    }
  }
}
