package io.avaje.metrics.core;

import io.avaje.metrics.GaugeLong;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JvmCGroupMemoryTest {

  private final JvmCGroupMemory me = new JvmCGroupMemory();

  @Test
  void toMegaBytes() {
    assertThat(JvmCGroupMemory.toMegaBytes(1074790400)).isEqualTo(1025);
    assertThat(JvmCGroupMemory.toMegaBytes(238493696)).isEqualTo(227);
  }

  @Test
  void memSource() {

    long limit = 1074790400;

    FileLines useSource = new FileLines("src/test/resources/cgroup/memory.usage_in_bytes");
    assertTrue(useSource.exists());

    JvmCGroupMemory.MemSource source = new JvmCGroupMemory.MemSource(limit, useSource);

    assertThat(source.limitMb()).isEqualTo(1025);
    assertThat(source.usageMb()).isEqualTo(227);
    assertThat(source.pctUsage()).isEqualTo(22);

    final GaugeLong memoryLimit = me.limit(source, true);
    assertThat(memoryLimit.value()).isEqualTo(1025);

    final GaugeLong memoryUsage = me.usage(source, true);
    assertThat(memoryUsage.value()).isEqualTo(227);

    final GaugeLong pctUsage = me.pctUsage(source, true);
    assertThat(pctUsage.value()).isEqualTo(22);
  }

}
