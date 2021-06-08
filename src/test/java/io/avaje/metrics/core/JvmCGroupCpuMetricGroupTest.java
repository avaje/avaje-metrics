package io.avaje.metrics.core;


import io.avaje.metrics.GaugeLongMetric;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JvmCGroupCpuMetricGroupTest {

  private final JvmCGroupCpuMetricGroup me = new JvmCGroupCpuMetricGroup();

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
  void cpuUsageMicros() {

    FileLines source = new FileLines("src/test/resources/cgroup/cpuacct.usage");
    assertTrue(source.exists());

    final JvmCGroupCpuMetricGroup.CpuUsageMicros usageMicros = new JvmCGroupCpuMetricGroup.CpuUsageMicros(source);

    final long value = usageMicros.getValue();
    assertThat(value).isEqualTo(55035664L);
  }

  @Test
  void cpuThrottleMicros() {

    FileLines source = new FileLines("src/test/resources/cgroup/cpu.stat");
    assertTrue(source.exists());

    final JvmCGroupCpuMetricGroup.CpuStatsSource cpuStats = new JvmCGroupCpuMetricGroup.CpuStatsSource(source);

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

    JvmCGroupCpuMetricGroup me = new JvmCGroupCpuMetricGroup();
    final GaugeLongMetric metric = me.createCGroupCpuLimit(cpuQuota, period);
    final long limit = metric.getValue();
    assertThat(limit).isEqualTo(600L);
  }

  @Test
  void createCGroupCpuRequests() {

    FileLines cpuShares = new FileLines("src/test/resources/cgroup/cpu.shares");
    assertTrue(cpuShares.exists());

    JvmCGroupCpuMetricGroup me = new JvmCGroupCpuMetricGroup();
    final GaugeLongMetric metric = me.createCGroupCpuRequests(cpuShares);
    final long requests = metric.getValue();
    assertThat(requests).isEqualTo(200L);
  }

}