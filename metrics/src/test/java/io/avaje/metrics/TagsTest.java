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
    Tags tag = Tags.of("env", "dev");
    assertThat(tag).isEqualTo(Tags.of("env", "dev"));
    assertThat(tag.array()).isEqualTo(new String[]{"env","dev"});

    assertThat(tag).isNotEqualTo(Tags.of("env", "test"));
    assertThat(tag).isNotEqualTo(Tags.of("enx", "dev"));
  }

  @Test
  void testToString() {
    assertThat(Tags.EMPTY.toString()).isEqualTo("");
    assertThat(Tags.of("env", "dev").toString()).isEqualTo("tags:[env, dev]");
    assertThat(Tags.of("a", "b", "x", "y").toString()).isEqualTo("tags:[a, b, x, y]");
  }
}
