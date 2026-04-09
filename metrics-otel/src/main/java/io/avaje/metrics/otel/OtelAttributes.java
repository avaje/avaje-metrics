package io.avaje.metrics.otel;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;

/**
 * Converts avaje metrics tags to OpenTelemetry {@link Attributes}.
 * <p>
 * Avaje tags use {@code "key:value"} colon-separated format, for example:
 * {@code "env:prod"}, {@code "region:us-east-1"}.
 */
final class OtelAttributes {

  private OtelAttributes() {}

  /**
   * Convert an avaje tags array to OTEL {@link Attributes}.
   * <p>
   * Each tag is split on the first {@code ':'} character. Tags without a colon are skipped.
   *
   * @param tags the avaje tags array (may be null or empty)
   * @return the corresponding OTEL Attributes, or {@link Attributes#empty()} if no valid tags
   */
  static Attributes of(String[] tags) {
    if (tags == null || tags.length == 0) {
      return Attributes.empty();
    }
    AttributesBuilder builder = Attributes.builder();
    for (String tag : tags) {
      if (tag != null) {
        int colon = tag.indexOf(':');
        if (colon > 0) {
          builder.put(tag.substring(0, colon), tag.substring(colon + 1));
        }
      }
    }
    return builder.build();
  }
}
