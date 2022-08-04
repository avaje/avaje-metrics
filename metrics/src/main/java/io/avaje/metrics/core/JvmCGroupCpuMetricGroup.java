package io.avaje.metrics.core;

import io.avaje.metrics.GaugeLong;
import io.avaje.metrics.GaugeLongMetric;
import io.avaje.metrics.Metric;
import io.avaje.metrics.MetricName;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static java.math.BigDecimal.valueOf;

final class JvmCGroupCpuMetricGroup {

  private final List<Metric> metrics = new ArrayList<>();

  /**
   * Return the list of OS process memory metrics.
   */
  static List<Metric> createGauges(boolean reportChangesOnly) {
    return new JvmCGroupCpuMetricGroup().metrics(reportChangesOnly);
  }

  private void add(Metric metric) {
    if (metric != null) {
      metrics.add(metric);
    }
  }

  private List<Metric> metrics(boolean reportChangesOnly) {
    FileLines cpu = new FileLines("/sys/fs/cgroup/cpu,cpuacct/cpuacct.usage");
    if (cpu.exists()) {
      add(createCGroupCpuUsage(cpu));
    }
    FileLines cpuStat = new FileLines("/sys/fs/cgroup/cpu,cpuacct/cpu.stat");
    if (cpuStat.exists()) {
      createCGroupCpuThrottle(cpuStat, reportChangesOnly);
    }
    FileLines cpuShares = new FileLines("/sys/fs/cgroup/cpu,cpuacct/cpu.shares");
    if (cpuStat.exists()) {
      add(createCGroupCpuRequests(cpuShares));
    }
    FileLines cpuQuota = new FileLines("/sys/fs/cgroup/cpu,cpuacct/cpu.cfs_quota_us");
    FileLines period = new FileLines("/sys/fs/cgroup/cpu,cpuacct/cpu.cfs_period_us");
    if (cpuQuota.exists() && period.exists()) {
      add(createCGroupCpuLimit(cpuQuota, period));
    }

    return metrics;
  }

  GaugeLongMetric createCGroupCpuLimit(FileLines cpuQuota, FileLines period) {
    final long cpuQuotaVal = cpuQuota.single();
    long quotaPeriod = period.single();
    if (cpuQuotaVal > 0 && quotaPeriod > 0) {
      final long limit = convertQuotaToLimits(cpuQuotaVal, quotaPeriod);
      return new DGaugeLongMetric(name("jvm.cgroup.cpu.limit"), new FixedGauge(limit));
    }
    return null;
  }

  GaugeLongMetric createCGroupCpuRequests(FileLines cpuShares) {
    final long requests = convertSharesToRequests(cpuShares.single());
    return new DGaugeLongMetric(name("jvm.cgroup.cpu.requests"), new FixedGauge(requests));
  }

  long convertQuotaToLimits(long cpuQuotaVal, long quotaPeriod) {
    return valueOf(cpuQuotaVal)
      .multiply(valueOf(1000)) // to micro cores
      .divide(valueOf(quotaPeriod), RoundingMode.HALF_UP)
      .longValue();
  }

  /**
   * Convert docker cpu shares to K8s micro cores.
   */
  long convertSharesToRequests(long shares) {
    return valueOf(shares)
      .multiply(valueOf(1000))
      .divide(valueOf(1024), RoundingMode.HALF_UP)
      .setScale(-1, RoundingMode.HALF_UP)
      .longValue();
  }

  private GaugeLongMetric createCGroupCpuUsage(FileLines cpu) {
    return incrementing(name("jvm.cgroup.cpu.usageMicros"), new CpuUsageMicros(cpu));
  }

  private void createCGroupCpuThrottle(FileLines cpuStat, boolean reportChangesOnly) {
    CpuStatsSource source = new CpuStatsSource(cpuStat);
    metrics.add(gauge(name("jvm.cgroup.cpu.throttleMicros"), source::getThrottleMicros, reportChangesOnly));
    metrics.add(gauge(name("jvm.cgroup.cpu.numPeriod"), source::getNumPeriod, reportChangesOnly));
    metrics.add(gauge(name("jvm.cgroup.cpu.numThrottle"), source::getNumThrottle, reportChangesOnly));
    metrics.add(gauge(name("jvm.cgroup.cpu.pctThrottle"), source::getPctThrottle, reportChangesOnly));
  }

  private GaugeLongMetric incrementing(MetricName name, GaugeLong gauge) {
    return DGaugeLongMetric.incrementing(name, gauge);
  }

  private GaugeLongMetric gauge(MetricName name, GaugeLong gauge, boolean reportChangesOnly) {
    return new DGaugeLongMetric(name, gauge, reportChangesOnly);
  }

  private MetricName name(String s) {
    return new DMetricName(s);
  }

  static final class CpuUsageMicros implements GaugeLong {

    private final FileLines source;

    CpuUsageMicros(FileLines source) {
      this.source = source;
    }

    @Override
    public long value() {
      return source.singleMicros();
    }
  }

  static final class CpuStatsSource {

    private final FileLines source;

    private long prevNumPeriod;
    private long prevNumThrottle;
    private long prevThrottleMicros;

    private long currNumPeriod;
    private long currNumThrottle;
    private long currThrottleMicros;

    private long numPeriod;
    private long numThrottle;
    private long throttleMicros;

    CpuStatsSource(FileLines source) {
      this.source = source;
    }

    void load() {
      synchronized (this) {
        for (String line : source.readLines()) {
          if (line.startsWith("nr_p")) {
            currNumPeriod = Long.parseLong(line.substring(11));
          } else if (line.startsWith("nr_t")) {
            // convert from nanos to micros
            currNumThrottle = Long.parseLong(line.substring(13));
          } else {
            currThrottleMicros = Long.parseLong(line.substring(15)) / 1000;
          }
        }
        numPeriod = currNumPeriod - prevNumPeriod;
        numThrottle = currNumThrottle - prevNumThrottle;
        throttleMicros = currThrottleMicros - prevThrottleMicros;
        prevNumPeriod = currNumPeriod;
        prevNumThrottle = currNumThrottle;
        prevThrottleMicros = currThrottleMicros;
      }
    }

    long getThrottleMicros() {
      load();
      return throttleMicros;
    }

    long getNumThrottle() {
      return numThrottle;
    }

    long getNumPeriod() {
      return numPeriod;
    }

    long getPctThrottle() {
      return (numPeriod <= 0) ? 0 : numThrottle * 100 / numPeriod;
    }
  }

  static final class FixedGauge implements GaugeLong {

    private final long value;

    FixedGauge(long value) {
      this.value = value;
    }

    @Override
    public long value() {
      return value;
    }
  }
}
