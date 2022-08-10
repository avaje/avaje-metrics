package io.avaje.metrics.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JvmSystemMetricGroupTest {

  @Test
  void testToLoad() {
    assertThat(JvmOsLoad.toLoad(0.18d)).isEqualTo(18);
    assertThat(JvmOsLoad.toLoad(0.15d)).isEqualTo(15);
    assertThat(JvmOsLoad.toLoad(0.10d)).isEqualTo(10);
    assertThat(JvmOsLoad.toLoad(0.015d)).isEqualTo(2);
    assertThat(JvmOsLoad.toLoad(0.014d)).isEqualTo(1);
  }
}
