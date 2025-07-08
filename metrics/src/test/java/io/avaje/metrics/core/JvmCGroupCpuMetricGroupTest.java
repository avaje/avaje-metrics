package io.avaje.metrics.core;


import io.avaje.metrics.GaugeLong;
import io.avaje.metrics.Tags;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JvmCGroupCpuMetricGroupTest {

  private final JvmCGroupCpu me = new JvmCGroupCpu();

  @Test
  void convertQuotaToLimits() {
    assertEquals(600, me.convertQuotaToLimits(60_000, 100_000));
    assertEquals(1900, me.convertQuotaToLimits(190_000, 100_000));
  }

  @Test
  void convertSharesToRequests() {
    assertEquals(800, me.convertSharesToRequests(819));
    assertEquals(200, me.convertSharesToRequests(204));
  }

  @Test
  void cpuUsageMillis_firstReadingIsZero() {
    FileLines source = new FileLines("src/test/resources/cgroup/cpuacct.usage");
    assertTrue(source.exists());

    final JvmCGroupCpu.CpuUsage usageMicros = new JvmCGroupCpu.CpuUsage(source);

    final long value = usageMicros.getAsLong();
    assertThat(value).isEqualTo(0L);
  }

  @Test
  void cpuThrottleMicros() {

    FileLines source = new FileLines("src/test/resources/cgroup/cpu.stat");
    assertTrue(source.exists());

    final JvmCGroupCpu.CpuStatsSource cpuStats = new JvmCGroupCpu.CpuStatsSource(source);

    assertThat(cpuStats.getThrottleMicros()).isEqualTo(87738876L);
    assertThat(cpuStats.getNumPeriod()).isEqualTo(19295);
    assertThat(cpuStats.getNumThrottle()).isEqualTo(802);
  }

  @Test
  void createCGroupCpuLimit() {

    FileLines cpuQuota = new FileLines("src/test/resources/cgroup/cpu.cfs_quota_us");
    FileLines period = new FileLines("src/test/resources/cgroup/cpu.cfs_period_us");
    assertTrue(cpuQuota.exists());
    assertTrue(period.exists());

    JvmCGroupCpu me = new JvmCGroupCpu();
    Optional<GaugeLong> cGroupCpuLimit = me.createCGroupCpuLimit(cpuQuota, period, Tags.of("myTag:myVal"));
    assertThat(cGroupCpuLimit)
      .isPresent()
      .hasValueSatisfying(metric -> {
        final long limit = metric.value();
        assertThat(limit).isEqualTo(600L);
      });
  }

  @Test
  void createCGroupCpuRequests() {

    FileLines cpuShares = new FileLines("src/test/resources/cgroup/cpu.shares");
    assertTrue(cpuShares.exists());

    JvmCGroupCpu me = new JvmCGroupCpu();
    final GaugeLong metric = me.createCGroupCpuRequests(cpuShares, Tags.EMPTY);
    final long requests = metric.value();
    assertThat(requests).isEqualTo(200L);
  }

}
