package io.avaje.metrics.core;

import io.avaje.metrics.GaugeLong;
import io.avaje.metrics.Metric;
import io.avaje.metrics.MetricRegistry;
import io.avaje.metrics.Tags;

import java.math.RoundingMode;
import java.util.Optional;
import java.util.function.LongSupplier;

import static java.math.BigDecimal.valueOf;

final class JvmCGroupCpu {

  static void createGauges(MetricRegistry registry, boolean reportChangesOnly, boolean withDetails, Tags globalTags) {
    new JvmCGroupCpu().create(registry, reportChangesOnly, withDetails, globalTags);
  }

  void create(MetricRegistry registry, boolean reportChangesOnly, boolean withDetails, Tags globalTags) {
     FileLines cpu = new FileLines("/sys/fs/cgroup/cpu,cpuacct/cpuacct.usage");
    if (cpu.exists()) {
      createCGroupCpuUsage(registry, cpu, globalTags);
    }
    FileLines cpuStat = new FileLines("/sys/fs/cgroup/cpu,cpuacct/cpu.stat");
    if (cpuStat.exists()) {
      createCGroupCpuThrottle(registry, cpuStat, reportChangesOnly, withDetails, globalTags);
    }
    FileLines cpuShares = new FileLines("/sys/fs/cgroup/cpu,cpuacct/cpu.shares");
    if (cpuStat.exists()) {
      registry.register(createCGroupCpuRequests(cpuShares, globalTags));
    }
    FileLines cpuQuota = new FileLines("/sys/fs/cgroup/cpu,cpuacct/cpu.cfs_quota_us");
    FileLines period = new FileLines("/sys/fs/cgroup/cpu,cpuacct/cpu.cfs_period_us");
    if (cpuQuota.exists() && period.exists()) {
      createCGroupCpuLimit(cpuQuota, period, globalTags).ifPresent(registry::register);
    }
  }

  Optional<GaugeLong> createCGroupCpuLimit(FileLines cpuQuota, FileLines period, Tags globalTags) {
    final long cpuQuotaVal = cpuQuota.single();
    long quotaPeriod = period.single();
    if (cpuQuotaVal > 0 && quotaPeriod > 0) {
      final long limit = convertQuotaToLimits(cpuQuotaVal, quotaPeriod);
      return Optional.of(DGaugeLong.once(Metric.ID.of("jvm.cgroup.cpu.limit", globalTags), new FixedGauge(limit)));
    }
    return Optional.empty();
  }

  GaugeLong createCGroupCpuRequests(FileLines cpuShares, Tags globalTags) {
    final long requests = convertSharesToRequests(cpuShares.single());
    return DGaugeLong.once(Metric.ID.of("jvm.cgroup.cpu.requests", globalTags), new FixedGauge(requests));
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

  private void createCGroupCpuUsage(MetricRegistry registry, FileLines cpu, Tags globalTags) {
    registry.gauge("jvm.cgroup.cpu.usage", globalTags, new CpuUsage(cpu));
  }

  private void createCGroupCpuThrottle(MetricRegistry registry, FileLines cpuStat, boolean reportChangesOnly, boolean withDetails, Tags globalTags) {
    final var source = new CpuStatsSource(cpuStat);
    registry.register(gauge(Metric.ID.of("jvm.cgroup.cpu.throttleMicros", globalTags), source::getThrottleMicros, reportChangesOnly));
    if (withDetails) {
      registry.register(gauge(Metric.ID.of("jvm.cgroup.cpu.numPeriod", globalTags), source::getNumPeriod, reportChangesOnly));
      registry.register(gauge(Metric.ID.of("jvm.cgroup.cpu.numThrottle", globalTags), source::getNumThrottle, reportChangesOnly));
      registry.register(gauge(Metric.ID.of("jvm.cgroup.cpu.pctThrottle", globalTags), source::getPctThrottle, reportChangesOnly));
    }
  }

  private GaugeLong gauge(Metric.ID id, LongSupplier gauge, boolean reportChangesOnly) {
    return DGaugeLong.of(id, gauge, reportChangesOnly);
  }

  /** CPU Usage in Millicores */
  static final class CpuUsage implements LongSupplier {

    private final FileLines source;

    private long prevUsageNanos;
    private long prevTimeMillis;

    CpuUsage(FileLines source) {
      this.source = source;
    }

    @Override
    public long getAsLong() {
      final long newTimeMillis = System.currentTimeMillis();
      final long newUsageNanos = source.single();
      if (prevUsageNanos == 0) {
        prevUsageNanos = newUsageNanos;
        prevTimeMillis = newTimeMillis;
        return 0;
      }
      final long deltaTimeMicros = (newTimeMillis - prevTimeMillis) * 1000;
      final long millicores = (newUsageNanos - prevUsageNanos) / deltaTimeMicros;
      this.prevTimeMillis = newTimeMillis;
      this.prevUsageNanos = newUsageNanos;
      return millicores;
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

  static final class FixedGauge implements LongSupplier {

    private final long value;

    FixedGauge(long value) {
      this.value = value;
    }

    @Override
    public long getAsLong() {
      return value;
    }
  }
}
