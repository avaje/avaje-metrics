package io.avaje.metrics.statsd;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TrimTest {

  @Test
  void trim() {
    assertThat(Trim.trim("web.api.Foo.bar", 8)).isEqualTo("Foo.bar");
    assertThat(Trim.trim("app.Foo.bar", 4)).isEqualTo("Foo.bar");
  }

  @Test
  void query() {
    assertThat(Trim.qry("orm.Foo_findList")).isEqualTo("Foo_findList");
    assertThat(Trim.qry("sql.Foo_findList")).isEqualTo("Foo_findList");
    assertThat(Trim.qry("txn.main")).isEqualTo("main");
    assertThat(Trim.qry("txn.named.foo")).isEqualTo("named.foo");

    assertThat(Trim.qry("orm.Foo4567890_123456789_123456789_123456789_123456789_123456789_123456789_123456789_123456789_123456789_Xxxxxx_Yyyyyyy"))
      .isEqualTo("Foo4567890_123456789_123456789_123456789_123456789_123456789_123456789_123456789_123456789_1234_");
  }

}
