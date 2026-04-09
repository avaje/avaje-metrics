package io.avaje.metrics.otel;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OtelAttributesTest {

  @Test
  void nullTags_returnsEmpty() {
    assertThat(OtelAttributes.of(null)).isEqualTo(Attributes.empty());
  }

  @Test
  void emptyTags_returnsEmpty() {
    assertThat(OtelAttributes.of(new String[0])).isEqualTo(Attributes.empty());
  }

  @Test
  void singleTag() {
    Attributes attrs = OtelAttributes.of(new String[]{"env:prod"});
    assertThat(attrs.get(AttributeKey.stringKey("env"))).isEqualTo("prod");
    assertThat(attrs.size()).isEqualTo(1);
  }

  @Test
  void multipleTags() {
    Attributes attrs = OtelAttributes.of(new String[]{"env:prod", "region:us-east-1", "tier:backend"});
    assertThat(attrs.get(AttributeKey.stringKey("env"))).isEqualTo("prod");
    assertThat(attrs.get(AttributeKey.stringKey("region"))).isEqualTo("us-east-1");
    assertThat(attrs.get(AttributeKey.stringKey("tier"))).isEqualTo("backend");
    assertThat(attrs.size()).isEqualTo(3);
  }

  @Test
  void tagWithoutColon_skipped() {
    Attributes attrs = OtelAttributes.of(new String[]{"badtag", "env:prod"});
    assertThat(attrs.size()).isEqualTo(1);
    assertThat(attrs.get(AttributeKey.stringKey("env"))).isEqualTo("prod");
  }

  @Test
  void tagWithColonAtStart_skipped() {
    // colon at index 0 — key would be empty
    Attributes attrs = OtelAttributes.of(new String[]{":value", "env:prod"});
    assertThat(attrs.size()).isEqualTo(1);
    assertThat(attrs.get(AttributeKey.stringKey("env"))).isEqualTo("prod");
  }

  @Test
  void tagWithValueContainingColon() {
    // value contains extra colon — split on first colon only
    Attributes attrs = OtelAttributes.of(new String[]{"url:http://example.com"});
    assertThat(attrs.get(AttributeKey.stringKey("url"))).isEqualTo("http://example.com");
  }

  @Test
  void nullTagEntry_skipped() {
    Attributes attrs = OtelAttributes.of(new String[]{null, "env:prod"});
    assertThat(attrs.size()).isEqualTo(1);
    assertThat(attrs.get(AttributeKey.stringKey("env"))).isEqualTo("prod");
  }
}
