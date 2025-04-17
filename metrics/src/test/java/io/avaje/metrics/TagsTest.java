package io.avaje.metrics;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TagsTest {

  @Test
  void empty() {
    assertThat(Tags.of()).isEqualTo(Tags.EMPTY);
    assertThat(Tags.EMPTY.array().length).isEqualTo(0);
  }

  @Test
  void of() {
    Tags tag = Tags.of("env:dev", "service:foo");
    assertThat(tag).isEqualTo(Tags.of("env:dev", "service:foo"));
    assertThat(tag.array()).isEqualTo(new String[]{"env:dev","service:foo"});

    assertThat(tag).isNotEqualTo(Tags.of("env:test", "service:foo"));
    assertThat(tag).isNotEqualTo(Tags.of("enx:dev", "service:foo"));
  }

  @Test
  void testToString() {
    assertThat(Tags.EMPTY.toString()).isEqualTo("");
    assertThat(Tags.of("e:d", "d:v").toString()).isEqualTo("tags:[e:d, d:v]");
    assertThat(Tags.of("a", "b", "x", "y").toString()).isEqualTo("tags:[a, b, x, y]");
  }

  @Test
  void append_when_empty() {
    assertThat(Tags.of().append("x:y")).isEqualTo(new String[]{"x:y"});
  }

  @Test
  void append() {
    assertThat(Tags.of("a:1", "b:2").append("x:y")).isEqualTo(new String[]{"a:1", "b:2", "x:y"});
  }
}
