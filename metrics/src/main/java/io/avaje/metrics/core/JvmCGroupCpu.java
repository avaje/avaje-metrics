package io.avaje.metrics.core;

import io.avaje.metrics.GaugeLong;
import io.avaje.metrics.MetricRegistry;

import java.math.RoundingMode;
import java.util.Optional;
import java.util.function.LongSupplier;

import static java.math.BigDecimal.valueOf;

final class JvmCGroupCpu {

  static void createGauges(MetricRegistry registry, boolean reportChangesOnly, boolean withDetails) {
    new JvmCGroupCpu().create(registry, reportChangesOnly, withDetails);
  }

  void create(MetricRegistry registry, boolean reportChangesOnly, boolean withDetails) {
     FileLines cpu = new FileLines("/sys/fs/cgroup/cpu,cpuacct/cpuacct.usage");
    if (cpu.exists()) {
      createCGroupCpuUsage(registry, cpu);
    }
    FileLines cpuStat = new FileLines("/sys/fs/cgroup/cpu,cpuacct/cpu.stat");
    if (cpuStat.exists()) {
      createCGroupCpuThrottle(registry, cpuStat, reportChangesOnly, withDetails);
    }
    FileLines cpuShares = new FileLines("/sys/fs/cgroup/cpu,cpuacct/cpu.shares");
    if (cpuStat.exists()) {
      registry.register(createCGroupCpuRequests(cpuShares));
    }
    FileLines cpuQuota = new FileLines("/sys/fs/cgroup/cpu,cpuacct/cpu.cfs_quota_us");
    FileLines period = new FileLines("/sys/fs/cgroup/cpu,cpuacct/cpu.cfs_period_us");
    if (cpuQuota.exists() && period.exists()) {
      createCGroupCpuLimit(cpuQuota, period).ifPresent(registry::register);
    }
  }

  Optional<GaugeLong> createCGroupCpuLimit(FileLines cpuQuota, FileLines period) {
    final long cpuQuotaVal = cpuQuota.single();
    long quotaPeriod = period.single();
    if (cpuQuotaVal > 0 && quotaPeriod > 0) {
      final long limit = convertQuotaToLimits(cpuQuotaVal, quotaPeriod);
      return Optional.of(DGaugeLong.once("jvm.cgroup.cpu.limit", new FixedGauge(limit)));
    }
    return Optional.empty();
  }

  GaugeLong createCGroupCpuRequests(FileLines cpuShares) {
    final long requests = convertSharesToRequests(cpuShares.single());
    return DGaugeLong.once("jvm.cgroup.cpu.requests", new FixedGauge(requests));
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

  private void createCGroupCpuUsage(MetricRegistry registry, FileLines cpu) {
    registry.gauge("jvm.cgroup.cpu.usageMicros", GaugeLong.incrementing(new CpuUsageMicros(cpu)));
  }

  private void createCGroupCpuThrottle(MetricRegistry registry, FileLines cpuStat, boolean reportChangesOnly, boolean withDetails) {
    final var source = new CpuStatsSource(cpuStat);
    registry.register(gauge("jvm.cgroup.cpu.throttleMicros", source::getThrottleMicros, reportChangesOnly));
    if (withDetails) {
      registry.register(gauge("jvm.cgroup.cpu.numPeriod", source::getNumPeriod, reportChangesOnly));
      registry.register(gauge("jvm.cgroup.cpu.numThrottle", source::getNumThrottle, reportChangesOnly));
      registry.register(gauge("jvm.cgroup.cpu.pctThrottle", source::getPctThrottle, reportChangesOnly));
    }
  }

  private GaugeLong gauge(String name, LongSupplier gauge, boolean reportChangesOnly) {
    return DGaugeLong.of(name, gauge, reportChangesOnly);
  }

  static final class CpuUsageMicros implements LongSupplier {

    private final FileLines source;

    CpuUsageMicros(FileLines source) {
      this.source = source;
    }

    @Override
    public long getAsLong() {
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
