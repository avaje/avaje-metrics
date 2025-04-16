package io.avaje.metrics;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MetricIDTest {

  @Test
  void equalToNoTag() {
    var one = Metric.ID.of("one");
    assertThat(one).isEqualTo(Metric.ID.of("one"));

    assertThat(one).isNotEqualTo(Metric.ID.of("two"));
    assertThat(one).isNotEqualTo(Metric.ID.of("one", Tags.of("a", "b")));
  }

  @Test
  void equalToWithTag() {
    var one = Metric.ID.of("one", Tags.of("a", "b"));
    assertThat(one).isEqualTo(Metric.ID.of("one", Tags.of("a", "b")));

    assertThat(one).isNotEqualTo(Metric.ID.of("one"));
    assertThat(one).isNotEqualTo(Metric.ID.of("one", Tags.EMPTY));
    assertThat(one).isNotEqualTo(Metric.ID.of("one", Tags.of("x", "y")));
    assertThat(one).isNotEqualTo(Metric.ID.of("two", Tags.of("a", "b")));
  }

  @Test
  void suffix() {
    var one = Metric.ID.of("one", Tags.of("a", "b"));
    Metric.ID suffix = one.suffix(".error");
    assertThat(suffix.name()).isEqualTo("one.error");
    assertThat(suffix.tags()).isEqualTo(one.tags());
  }

  @Test
  void withName_sameName_expectSame() {
    var one = Metric.ID.of("one", Tags.of("a", "b"));
    Metric.ID expectSame = one.withName("one");
    assertThat(expectSame).isSameAs(one);
  }

  @Test
  void withName_differentName() {
    var one = Metric.ID.of("one", Tags.of("a", "b"));
    Metric.ID other = one.withName("other");
    assertThat(other.name()).isEqualTo("other");
    assertThat(other.tags()).isEqualTo(one.tags());
  }

  @Test
  void withTags_sameTags() {
    var one = Metric.ID.of("one", Tags.of("a", "b"));

    Metric.ID other = one.withTags(Tags.of("a", "b"));
    assertThat(other).isSameAs(one);
  }

  @Test
  void withTags_differentTags() {
    var one = Metric.ID.of("one", Tags.of("a", "b"));

    Metric.ID other = one.withTags(Tags.of("x", "y"));
    assertThat(other.name()).isEqualTo(one.name());
    assertThat(other.tags()).isNotEqualTo(one.tags());
    assertThat(other.tags()).isEqualTo(Tags.of("x", "y"));
  }

}
