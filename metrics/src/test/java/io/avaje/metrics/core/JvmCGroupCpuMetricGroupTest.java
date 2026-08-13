package io.avaje.metrics.core;

import io.avaje.metrics.Tags;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JvmCGroupCpuMetricGroupTest {

  private final JvmCGroupCpu cpu = new JvmCGroupCpu();

  @Test
  void readsCGroupV2CpuCounters() {
    var source = new FileLines("src/test/resources/cgroup/cpu-v2.stat");
    assertThat(source.exists()).isTrue();

    var stats = new JvmCGroupCpu.CpuStatsSource(source);

    assertThat(stats.usageMicros()).isEqualTo(1_234_567L);
    assertThat(stats.userMicros()).isEqualTo(987_654L);
    assertThat(stats.systemMicros()).isEqualTo(246_913L);
    assertThat(stats.throttledMicros()).isEqualTo(7_890L);
    assertThat(stats.periods()).isEqualTo(1_234L);
    assertThat(stats.throttledPeriods()).isEqualTo(56L);
  }

  @Test
  void parsesCpuLimitInMillicores() {
    assertThat(cpu.parseCpuMax("60000 100000")).contains(600L);
    assertThat(cpu.parseCpuMax("190000 100000")).contains(1_900L);
    assertThat(cpu.parseCpuMax("max 100000")).isEmpty();
  }

  @Test
  void createsCpuLimitMetric() {
    var cpuMax = new FileLines("src/test/resources/cgroup/cpu-v2.max");
    assertThat(cpuMax.exists()).isTrue();

    assertThat(cpu.createCGroupCpuLimit(cpuMax, Tags.of("app:shop")))
      .isPresent()
      .hasValueSatisfying(metric -> {
        assertThat(metric.name()).isEqualTo("jvm.cgroup.cpu.limitMillicores");
        assertThat(metric.unit()).isEqualTo("mCPU");
        assertThat(metric.value()).isEqualTo(600L);
      });
  }
}
