package io.avaje.metrics.otel.producer;

import io.avaje.metrics.Tags;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;

import java.util.Map;

final class OtelAttributes {

  private OtelAttributes() {}

  static Attributes of(Tags tags, Map<String, Attributes> cache) {
    var cacheKey = tags.cacheKey();
    if (cacheKey.isEmpty()) {
      return Attributes.empty();
    }
    return cache.computeIfAbsent(cacheKey, key -> build(tags));
  }

  private static Attributes build(Tags tags) {
    AttributesBuilder builder = Attributes.builder();
    for (String tag : tags.array()) {
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
