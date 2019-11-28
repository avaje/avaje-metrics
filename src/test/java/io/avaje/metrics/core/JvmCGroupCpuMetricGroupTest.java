package io.avaje.metrics.core;


import io.avaje.metrics.GaugeLongMetric;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class JvmCGroupCpuMetricGroupTest {

  private final JvmCGroupCpuMetricGroup me = new JvmCGroupCpuMetricGroup();

  @Test
  public void convertQuotaToLimits() {
    assertEquals(600, me.convertQuotaToLimits(60_000, 100_000));
    assertEquals(1900, me.convertQuotaToLimits(190_000, 100_000));
  }

  @Test
  public void convertSharesToRequests() {
    assertEquals(800, me.convertSharesToRequests(819));
    assertEquals(200, me.convertSharesToRequests(204));
  }

  @Test
  public void cpuUsageMicros() {

    FileLines source = new FileLines("src/test/resources/cgroup/cpuacct.usage");
    assertTrue(source.exists());

    final JvmCGroupCpuMetricGroup.CpuUsageMicros usageMicros = new JvmCGroupCpuMetricGroup.CpuUsageMicros(source);

    final long value = usageMicros.getValue();
    assertThat(value).isEqualTo(55035664L);
  }

  @Test
  public void cpuThrottleMicros() {

    FileLines source = new FileLines("src/test/resources/cgroup/cpu.stat");
    assertTrue(source.exists());

    final JvmCGroupCpuMetricGroup.CpuThrottleMicros throttleMicros = new JvmCGroupCpuMetricGroup.CpuThrottleMicros(source);

    final long value = throttleMicros.getValue();
    assertThat(value).isEqualTo(87738876L);
  }

  @Test
  public void createCGroupCpuLimit() {

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
  public void createCGroupCpuRequests() {

    FileLines cpuShares = new FileLines("src/test/resources/cgroup/cpu.shares");
    assertTrue(cpuShares.exists());

    JvmCGroupCpuMetricGroup me = new JvmCGroupCpuMetricGroup();
    final GaugeLongMetric metric = me.createCGroupCpuRequests(cpuShares);
    final long requests = metric.getValue();
    assertThat(requests).isEqualTo(200L);
  }

}